/**
 * ZamZam ERP - 'Firebase Authentication' Core Module (authService.js)
 * 
 * High-performance, secure authentication service integrating Firebase Authentication
 * with Cloud Firestore to manage role-based access control (RBAC).
 * 
 * Supports user registration (creating user in Firebase Auth and profile in Firestore),
 * login (authenticating and retrieving active roles), logout, and password recovery.
 */

// Import necessary modules from the modern Firebase Web SDK (v9+ modular style)
import { 
    getAuth, 
    createUserWithEmailAndPassword, 
    signInWithEmailAndPassword, 
    signOut, 
    sendPasswordResetEmail,
    updateProfile,
    onAuthStateChanged
} from "https://www.gstatic.com/firebasejs/9.x.x/firebase-auth.js";

import { 
    getFirestore, 
    doc, 
    setDoc, 
    getDoc, 
    updateDoc,
    serverTimestamp 
} from "https://www.gstatic.com/firebasejs/9.x.x/firebase-firestore.js";

class AuthService {
    /**
     * Initializes the Auth Service with a configured Firebase App instance
     * @param {Object} firebaseApp - The initialized Firebase App instance
     */
    constructor(firebaseApp) {
        if (!firebaseApp) {
            throw new Error("[AuthService] Firebase App instance is required for initialization.");
        }
        this.auth = getAuth(firebaseApp);
        this.db = getFirestore(firebaseApp);
        console.log("[AuthService] Successfully initialized Firebase Auth & Firestore RBAC.");
    }

    /**
     * Registers a new user in Firebase Auth and provisions their role-based profile in Firestore
     * 
     * @param {string} email - The user's email address
     * @param {string} password - The account password
     * @param {string} displayName - The user's full name / display name
     * @param {string} role - The designated workspace role (e.g., "Admin", "Cashier")
     * @returns {Promise<Object>} The registered user profile and Firestore record
     */
    async register(email, password, displayName, role = "Cashier") {
        console.log(`[AuthService] Attempting registration for user: ${email} with role: ${role}`);
        
        // 1. Validate inputs
        if (!email || !password || !displayName) {
            throw new Error("Email, password, and display name are required for registration.");
        }
        
        const normalizedRole = role.trim();
        const allowedRoles = ["Admin", "Cashier"];
        if (!allowedRoles.includes(normalizedRole)) {
            throw new Error(`Invalid role assignment: "${normalizedRole}". Allowed roles are: ${allowedRoles.join(", ")}`);
        }

        try {
            // 2. Create the user in Firebase Auth
            const userCredential = await createUserWithEmailAndPassword(this.auth, email, password);
            const user = userCredential.user;

            // 3. Set the user's displayName in their Auth Profile
            await updateProfile(user, { displayName });
            console.log(`[AuthService] Auth account created successfully (UID: ${user.uid})`);

            // 4. Provision the role-based profile document inside Firestore 'users' collection
            const userProfile = {
                uid: user.uid,
                email: email.toLowerCase(),
                displayName: displayName,
                role: normalizedRole,
                permissions: this.getPermissionsForRole(normalizedRole),
                isActive: true,
                createdAt: serverTimestamp(),
                lastLogin: null
            };

            const userRef = doc(this.db, "users", user.uid);
            await setDoc(userRef, userProfile);
            console.log(`[AuthService] Firestore profile provisioned for user UID: ${user.uid}`);

            return {
                user: {
                    uid: user.uid,
                    email: user.email,
                    displayName: displayName
                },
                profile: userProfile
            };
        } catch (error) {
            console.error("[AuthService] Registration failed:", error);
            throw this.sanitizeFirebaseError(error);
        }
    }

    /**
     * Authenticates a user with email and password, and retrieves their Firestore RBAC profile
     * 
     * @param {string} email - The registered email address
     * @param {string} password - The account password
     * @returns {Promise<Object>} Authenticated user and role-based permissions payload
     */
    async login(email, password) {
        console.log(`[AuthService] Attempting login for user: ${email}`);

        if (!email || !password) {
            throw new Error("Email and password are required for login.");
        }

        try {
            // 1. Sign in with Firebase Auth
            const userCredential = await signInWithEmailAndPassword(this.auth, email, password);
            const user = userCredential.user;
            console.log(`[AuthService] Firebase Auth login success (UID: ${user.uid})`);

            // 2. Fetch the corresponding user profile document from Firestore
            const userRef = doc(this.db, "users", user.uid);
            const userSnap = await getDoc(userRef);

            if (!userSnap.exists()) {
                console.warn(`[AuthService] User profile doc not found for UID: ${user.uid}. Creating fallback Cashier profile.`);
                // Graceful fallback for desynced database users
                const fallbackProfile = {
                    uid: user.uid,
                    email: user.email,
                    displayName: user.displayName || email.split("@")[0],
                    role: "Cashier",
                    permissions: this.getPermissionsForRole("Cashier"),
                    isActive: true,
                    createdAt: serverTimestamp(),
                    lastLogin: serverTimestamp()
                };
                await setDoc(userRef, fallbackProfile);
                return { user, profile: fallbackProfile };
            }

            const profile = userSnap.data();

            // 3. Verify user is active
            if (!profile.isActive) {
                await signOut(this.auth);
                throw new Error("This account has been disabled. Please contact your system administrator.");
            }

            // 4. Update the lastLogin timestamp in Firestore
            await updateDoc(userRef, {
                lastLogin: serverTimestamp()
            });

            console.log(`[AuthService] Successful login. Welcome, ${profile.displayName} (${profile.role})`);
            return { user, profile };
        } catch (error) {
            console.error("[AuthService] Login failed:", error);
            throw this.sanitizeFirebaseError(error);
        }
    }

    /**
     * Signs out the current user and clears any active sessions
     */
    async logout() {
        try {
            console.log("[AuthService] Signing user out from system...");
            await signOut(this.auth);
            console.log("[AuthService] Sign out completed successfully.");
        } catch (error) {
            console.error("[AuthService] Sign out failed:", error);
            throw this.sanitizeFirebaseError(error);
        }
    }

    /**
     * Sends a secure password reset / recovery email to the user
     * 
     * @param {string} email - The registered email address
     */
    async resetPassword(email) {
        console.log(`[AuthService] Triggering password recovery flow for: ${email}`);
        if (!email) {
            throw new Error("Email address is required to trigger password recovery.");
        }

        try {
            await sendPasswordResetEmail(this.auth, email);
            console.log(`[AuthService] Password reset verification email dispatched to: ${email}`);
        } catch (error) {
            console.error("[AuthService] Password reset trigger failed:", error);
            throw this.sanitizeFirebaseError(error);
        }
    }

    /**
     * Listens to Firebase Authentication state transitions and retrieves active user profile data
     * 
     * @param {Function} callback - Triggered when Auth state changes. Receives { user, profile } or null.
     * @returns {Function} Unsubscribe listener function
     */
    onAuthStateChanged(callback) {
        return onAuthStateChanged(this.auth, async (user) => {
            if (user) {
                try {
                    const userRef = doc(this.db, "users", user.uid);
                    const userSnap = await getDoc(userRef);
                    const profile = userSnap.exists() ? userSnap.data() : null;
                    callback({ user, profile });
                } catch (error) {
                    console.error("[AuthService] Auth state change profile fetch failed:", error);
                    callback({ user, profile: null });
                }
            } else {
                callback(null);
            }
        });
    }

    /**
     * Returns the permissions array associated with each application role
     * @param {string} role 
     * @returns {Array<string>} List of permitted actions
     */
    getPermissionsForRole(role) {
        switch (role.toUpperCase()) {
            case "ADMIN":
                return [
                    "POS_CHECKOUT",
                    "WRITE_PRODUCTS",
                    "MANAGE_SETTINGS",
                    "READ_REPORTS",
                    "MANAGE_USERS",
                    "MANAGE_LEDGER_PARTNERS",
                    "CLOUD_SYNC"
                ];
            case "CASHIER":
                return [
                    "POS_CHECKOUT",
                    "MANAGE_LEDGER_PARTNERS"
                ];
            default:
                return [];
        }
    }

    /**
     * Translates system Firebase raw errors into readable, user-friendly feedback strings
     * @param {Error} error 
     * @returns {Error} Sanatized Error with readable message
     */
    sanitizeFirebaseError(error) {
        let userMessage = error.message;

        if (error.code) {
            switch (error.code) {
                case "auth/invalid-email":
                    userMessage = "The email address is formatted incorrectly.";
                    break;
                case "auth/user-disabled":
                    userMessage = "This user account has been disabled by an administrator.";
                    break;
                case "auth/user-not-found":
                case "auth/wrong-password":
                case "auth/invalid-credential":
                    userMessage = "Invalid email or password. Please verify your credentials.";
                    break;
                case "auth/email-already-in-use":
                    userMessage = "An account with this email address already exists.";
                    break;
                case "auth/weak-password":
                    userMessage = "The chosen password is too weak. Please use at least 6 characters.";
                    break;
                case "auth/network-request-failed":
                    userMessage = "Network error. Please check your internet connection and try again.";
                    break;
                default:
                    userMessage = error.message.replace("Firebase: ", "");
                    break;
            }
        }
        
        return new Error(userMessage);
    }
}

export default AuthService;
