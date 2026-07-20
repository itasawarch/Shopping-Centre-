package com.example.ui

import android.app.Application
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class Screen {
    LOGIN, SIGNUP, MAIN
}

enum class MainTab {
    DASHBOARD, // Home Tab
    POS,       // Portfolio / Projects Tab
    PRODUCTS,  // Services Tab
    CUSTOMERS, // Skills Tab
    SUPPLIERS, // Experience Timeline Tab
    EXPENSES,  // Blog Tab
    REPORTS,   // Contact & Location Tab
    SETTINGS   // Admin Settings, SQL Diagnostics, and Theme Options
}

// Custom theme schemes supported
enum class AppColorScheme {
    CLASSIC_BLUE,    // Professional bright corporate blue
    MIDNIGHT_NAVY,   // Deep elegant navy
    ROYAL_SAPPHIRE,  // Premium dark rich sapphire
    EMERALD_MINT,    // Cool modern green-blue
    SLATE_MINIMALIST // High-contrast clean dark slate
}

// Chat message format for Live Assistant
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: String, // "Visitor" or "AI_Assistant"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

class POSViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = POSRepository(application)
    val sqlDatabaseController = SQLiteDatabaseController(application, repository)
    val firebaseAuthService = FirebaseAuthService(application, repository)

    // --- SQLite Database Controller Diagnostics State ---
    var dbSize by mutableStateOf(0L)
    val dbTableStats = mutableStateListOf<TableStat>()
    var rawQueryResult by mutableStateOf<List<Map<String, Any?>>?>(null)
    var rawQueryText by mutableStateOf("SELECT * FROM projects LIMIT 5")
    var rawQueryError by mutableStateOf<String?>(null)

    // --- Persistent Developer Profile States ---
    private val prefs = application.getSharedPreferences("developer_profile", android.content.Context.MODE_PRIVATE)

    var devName by mutableStateOf("")
    var devEmail by mutableStateOf("")
    var devPhone by mutableStateOf("")
    var devAddress by mutableStateOf("")

    init {
        var savedName = prefs.getString("dev_name", "Muhammad Tasawar") ?: "Muhammad Tasawar"
        var savedEmail = prefs.getString("dev_email", "itxtasawar786@gmail.com") ?: "itxtasawar786@gmail.com"
        var savedPhone = prefs.getString("dev_phone", "03116321786") ?: "03116321786"
        var savedAddress = prefs.getString("dev_address", "Sargodha, Punjab, Pakistan") ?: "Sargodha, Punjab, Pakistan"

        if (savedName == "Tasawar Abbas") {
            savedName = "Muhammad Tasawar"
            prefs.edit().putString("dev_name", "Muhammad Tasawar").apply()
        }
        if (savedPhone == "+92 300 1234567") {
            savedPhone = "03116321786"
            prefs.edit().putString("dev_phone", "03116321786").apply()
        }

        devName = savedName
        devEmail = savedEmail
        devPhone = savedPhone
        devAddress = savedAddress
    }

    fun saveDeveloperProfile(name: String, email: String, phone: String, address: String) {
        devName = name
        devEmail = email
        devPhone = phone
        devAddress = address
        prefs.edit()
            .putString("dev_name", name)
            .putString("dev_email", email)
            .putString("dev_phone", phone)
            .putString("dev_address", address)
            .apply()
    }

    fun refreshDbDiagnostics() {
        viewModelScope.launch {
            dbSize = sqlDatabaseController.getDatabaseSize()
            dbTableStats.clear()
            dbTableStats.addAll(sqlDatabaseController.getTableStats())
        }
    }

    fun runRawQuery() {
        viewModelScope.launch {
            rawQueryError = null
            if (rawQueryText.isBlank()) {
                rawQueryResult = listOf(mapOf("INFO" to "Please enter a non-empty SQL query"))
                return@launch
            }
            val result = sqlDatabaseController.executeRawQuery(rawQueryText)
            rawQueryResult = result
            refreshDbDiagnostics()
        }
    }

    // --- State Observables (Flows connected to Database) ---
    val projects: StateFlow<List<Project>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val services: StateFlow<List<Service>> = repository.allServices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val skills: StateFlow<List<Skill>> = repository.allSkills
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val experiences: StateFlow<List<Experience>> = repository.allExperiences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val educations: StateFlow<List<Education>> = repository.allEducations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val testimonials: StateFlow<List<Testimonial>> = repository.allTestimonials
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val blogs: StateFlow<List<BlogPost>> = repository.allBlogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val inquiries: StateFlow<List<Inquiry>> = repository.allInquiries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Navigation ---
    var currentScreen by mutableStateOf(Screen.MAIN) // Default to MAIN so visitors see the portfolio first
    var currentTab by mutableStateOf(MainTab.DASHBOARD)

    // --- Theme Settings & Appearance ---
    var isDarkMode by mutableStateOf(false)
    var activeColorScheme by mutableStateOf(AppColorScheme.CLASSIC_BLUE)
    var isStickyNavEnabled by mutableStateOf(true)
    var showCookieConsent by mutableStateOf(true)

    // --- Typing Header Text Animation State ---
    var typedText by mutableStateOf("")
    private val typingTitles = listOf(
        "Full-Stack Software Engineer",
        "WordPress Customizer & Architect",
        "Web Optimization Specialist",
        "Jetpack Compose Kotlin Developer"
    )

    // --- Active Logged Admin (For Editing Content) ---
    var loggedInUser by mutableStateOf<User?>(null)
    var usernameInput by mutableStateOf("")
    var passwordInput by mutableStateOf("")
    var rememberMe by mutableStateOf(true)
    var authError by mutableStateOf<String?>(null)

    // --- Contact Form Input States ---
    var contactName by mutableStateOf("")
    var contactEmail by mutableStateOf("")
    var contactPhone by mutableStateOf("")
    var contactMessage by mutableStateOf("")
    var contactSubmitSuccess by mutableStateOf(false)
    var contactSubmitMessage by mutableStateOf("")

    // --- Project Filtering State ---
    var activeProjectFilter by mutableStateOf("All") // All, WordPress, Web, Mobile

    // --- Blog Search State ---
    var blogSearchQuery by mutableStateOf("")
    var activeBlogCategory by mutableStateOf("All")

    // --- Live Chat Integration States ---
    var showLiveChatWindow by mutableStateOf(false)
    val chatMessages = mutableStateListOf<ChatMessage>()
    var chatInputText by mutableStateOf("")
    var isAiTyping by mutableStateOf(false)

    // --- Newsletter State ---
    var newsletterEmail by mutableStateOf("")
    var isNewsletterSubscribed by mutableStateOf(false)

    // --- Notification alert logs ---
    val notifications = mutableStateListOf<String>()

    init {
        // Trigger default seed data if database is fresh
        viewModelScope.launch {
            repository.populateSampleDataIfEmpty()
            addNotification("Developer Portfolio seeded with $devName's premium resume data.")
            refreshDbDiagnostics()
            startTypingAnimation()
            
            // Seed initial AI welcome messages
            chatMessages.add(
                ChatMessage(
                    sender = "AI_Assistant",
                    text = "Hello! I am $devName's Virtual Portfolio Assistant. 🚀 Feel free to ask me anything about my coding skills, services, recent projects, or estimated project pricing!"
                )
            )

            // See if any admin session is already active
            val user = firebaseAuthService.getCurrentUser()
            if (user != null) {
                loggedInUser = user
                addNotification("Admin session active: welcome back ${user.displayName}.")
            }
        }
    }

    fun addNotification(msg: String) {
        if (!notifications.contains(msg)) {
            notifications.add(0, msg)
        }
    }

    // --- Typing Animation Engine ---
    private fun startTypingAnimation() {
        viewModelScope.launch {
            var titleIndex = 0
            while (true) {
                val fullText = typingTitles[titleIndex]
                // Type character by character
                for (i in 0..fullText.length) {
                    typedText = fullText.substring(0, i)
                    delay(100)
                }
                delay(2000) // Hold at end of title
                // Erase character by character
                for (i in fullText.length downTo 0) {
                    typedText = fullText.substring(0, i)
                    delay(50)
                }
                delay(500) // Pause before next title
                titleIndex = (titleIndex + 1) % typingTitles.size
            }
        }
    }

    // --- Submit Contact Inquiry ---
    fun submitContactInquiry() {
        if (contactName.isBlank() || contactEmail.isBlank() || contactMessage.isBlank()) {
            contactSubmitSuccess = false
            contactSubmitMessage = "Please complete all required fields (Name, Email, and Message)."
            return
        }
        viewModelScope.launch {
            val inquiry = Inquiry(
                name = contactName,
                email = contactEmail,
                phone = contactPhone,
                message = contactMessage,
                timestamp = System.currentTimeMillis()
            )
            repository.saveInquiry(inquiry)
            contactSubmitSuccess = true
            contactSubmitMessage = "Thank you, ${contactName}! Your inquiry has been submitted securely. I'll get back to you shortly."
            addNotification("📬 New Contact Inquiry submitted by ${contactName}.")
            
            // Clear inputs
            contactName = ""
            contactEmail = ""
            contactPhone = ""
            contactMessage = ""
            
            delay(5000) // Clear response state after 5 seconds
            contactSubmitSuccess = false
            contactSubmitMessage = ""
            refreshDbDiagnostics()
        }
    }

    fun addInquiry(name: String, emailOrPhone: String, projectType: String, message: String) {
        viewModelScope.launch {
            val inquiry = Inquiry(
                name = name,
                email = emailOrPhone,
                phone = projectType, // Store category in phone field
                message = message,
                timestamp = System.currentTimeMillis()
            )
            repository.saveInquiry(inquiry)
            addNotification("New project inquiry from $name.")
            refreshDbDiagnostics()
        }
    }

    // --- Newsletter Subscription ---
    fun subscribeNewsletter() {
        if (newsletterEmail.isBlank() || !newsletterEmail.contains("@")) {
            addNotification("⚠️ Please enter a valid email address.")
            return
        }
        isNewsletterSubscribed = true
        addNotification("📬 Thank you! ${newsletterEmail} has been subscribed to my software newsletter.")
        newsletterEmail = ""
    }

    // --- Admin Authentication ---
    var regDisplayName by mutableStateOf("")
    var regUsername by mutableStateOf("")
    var regPassword by mutableStateOf("")
    var regRole by mutableStateOf("Admin")

    fun performLogin() {
        if (usernameInput.isBlank() || passwordInput.isBlank()) {
            authError = "Please enter both username and password."
            return
        }
        viewModelScope.launch {
            val result = firebaseAuthService.login(usernameInput, passwordInput)
            when (result) {
                is AuthResult.Success -> {
                    loggedInUser = result.user
                    authError = null
                    currentScreen = Screen.MAIN
                    usernameInput = ""
                    passwordInput = ""
                    addNotification("Admin dashboard unlocked successfully.")
                }
                is AuthResult.Error -> {
                    authError = result.message
                }
            }
        }
    }

    fun performSignup() {
        if (regUsername.isBlank() || regPassword.isBlank() || regDisplayName.isBlank()) {
            authError = "Please fill in all registration fields."
            return
        }
        viewModelScope.launch {
            val result = firebaseAuthService.signUp(regUsername, regPassword, regDisplayName, regRole)
            when (result) {
                is AuthResult.Success -> {
                    loggedInUser = result.user
                    authError = null
                    currentScreen = Screen.MAIN
                    regUsername = ""
                    regPassword = ""
                    regDisplayName = ""
                    addNotification("Admin registered & logged in successfully.")
                }
                is AuthResult.Error -> {
                    authError = result.message
                }
            }
        }
    }

    fun performLogout() {
        viewModelScope.launch {
            firebaseAuthService.signOut()
            loggedInUser = null
            addNotification("Admin dashboard secured. Logged out successfully.")
        }
    }

    // --- Live Chat Chatbot logic ---
    fun sendChatMessage() {
        val query = chatInputText.trim()
        if (query.isBlank()) return
        
        chatMessages.add(ChatMessage(sender = "Visitor", text = query))
        chatInputText = ""
        isAiTyping = true
        
        viewModelScope.launch {
            delay(1200) // Simulated AI thinking latency
            val reply = generateAssistantReply(query)
            chatMessages.add(ChatMessage(sender = "AI_Assistant", text = reply))
            isAiTyping = false
        }
    }

    private fun generateAssistantReply(query: String): String {
        val q = query.lowercase()
        return when {
            q.contains("hello") || q.contains("hi") || q.contains("hey") -> {
                "Hello there! How can $devName help you with your software development requirements today? Ask me about his services, tech skills, or portfolios!"
            }
            q.contains("skill") || q.contains("technolog") || q.contains("program") || q.contains("language") -> {
                "$devName is fluent in WordPress theme development, Custom PHP APIs, MySQL database indexing, Modern HTML/CSS grids, JavaScript interactivity, and Kotlin Jetpack Compose for mobile!"
            }
            q.contains("service") || q.contains("offer") || q.contains("what can you do") -> {
                "We offer fully responsive WordPress Websites, E-Commerce stores (WooCommerce), conversion Landing Pages, Core Web Vitals optimization, Database vacuum audits, and priority WordPress Bug Fixing."
            }
            q.contains("project") || q.contains("portfolio") || q.contains("recent work") -> {
                "Some of my recent projects include: EcoShop Storefront (a green-tech WooCommerce portal), MedHub (a custom PHP clinic scheduling directory), and Modern Agency (a 100/100 optimized Gutenberg lander)."
            }
            q.contains("price") || q.contains("cost") || q.contains("rate") || q.contains("budget") -> {
                "$devName's WordPress development starts around $500, Landing Pages from $250, Speed Optimization tasks for $150, and complex custom PHP dashboards typically start from $1200. Contact us directly to get a custom quote!"
            }
            q.contains("contact") || q.contains("hire") || q.contains("email") || q.contains("phone") -> {
                "You can hire $devName directly by submitting the form under the Contact Tab, emailing him at $devEmail, or messaging him on WhatsApp at $devPhone!"
            }
            q.contains("experience") || q.contains("work") || q.contains("history") -> {
                "$devName has 8+ years of total tech experience, working as a Senior Web Specialist, a corporate Software Architect, and a computer science programming mentor."
            }
            else -> {
                "I understand! $devName is highly specialized in solving custom WordPress and software requirements. Would you like me to record your message, or would you like to submit a formal inquiry under the 'Contact' tab so he can email you back?"
            }
        }
    }

    // --- Content Management Admin Actions ---
    fun addProject(title: String, desc: String, tech: String, img: String, demo: String, git: String, category: String) {
        viewModelScope.launch {
            val project = Project(
                title = title,
                description = desc,
                technologies = tech,
                imageUrl = img.ifBlank { "https://images.unsplash.com/photo-1531403009284-440f080d1e12?auto=format&fit=crop&q=80&w=400" },
                demoUrl = demo.ifBlank { "https://example.com" },
                githubUrl = git.ifBlank { "https://github.com" },
                category = category
            )
            repository.saveProject(project)
            addNotification("Project '${title}' added successfully.")
            refreshDbDiagnostics()
        }
    }

    fun deleteProject(project: Project) {
        viewModelScope.launch {
            repository.deleteProject(project)
            addNotification("Project '${project.title}' deleted.")
            refreshDbDiagnostics()
        }
    }

    fun addService(title: String, desc: String, icon: String) {
        viewModelScope.launch {
            val service = Service(title = title, description = desc, iconName = icon)
            repository.saveService(service)
            addNotification("Service '${title}' registered.")
            refreshDbDiagnostics()
        }
    }

    fun deleteService(service: Service) {
        viewModelScope.launch {
            repository.deleteService(service)
            addNotification("Service '${service.title}' removed.")
            refreshDbDiagnostics()
        }
    }

    fun addSkill(name: String, progress: Int, category: String) {
        viewModelScope.launch {
            val skill = Skill(name = name, progress = progress.coerceIn(0, 100), category = category)
            repository.saveSkill(skill)
            addNotification("Skill '${name}' updated to ${progress}%.")
            refreshDbDiagnostics()
        }
    }

    fun deleteSkill(skill: Skill) {
        viewModelScope.launch {
            repository.deleteSkill(skill)
            addNotification("Skill '${skill.name}' removed.")
            refreshDbDiagnostics()
        }
    }

    fun addBlog(title: String, content: String, category: String) {
        viewModelScope.launch {
            val blog = BlogPost(
                title = title,
                content = content,
                category = category,
                date = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date())
            )
            repository.saveBlog(blog)
            addNotification("Blog '${title}' published.")
            refreshDbDiagnostics()
        }
    }

    fun deleteBlog(blog: BlogPost) {
        viewModelScope.launch {
            repository.deleteBlog(blog)
            addNotification("Blog '${blog.title}' deleted.")
            refreshDbDiagnostics()
        }
    }

    fun deleteInquiry(inquiry: Inquiry) {
        viewModelScope.launch {
            repository.deleteInquiry(inquiry)
            addNotification("Inquiry from ${inquiry.name} deleted.")
            refreshDbDiagnostics()
        }
    }

    fun addExperience(role: String, company: String, period: String, description: String) {
        viewModelScope.launch {
            val exp = Experience(
                role = role,
                company = company,
                period = period,
                description = description,
                type = "Professional"
            )
            repository.saveExperience(exp)
            addNotification("Experience '${role}' added.")
            refreshDbDiagnostics()
        }
    }

    fun deleteExperience(experience: Experience) {
        viewModelScope.launch {
            repository.deleteExperience(experience)
            addNotification("Experience '${experience.role}' deleted.")
            refreshDbDiagnostics()
        }
    }

    fun addEducation(degree: String, institution: String, period: String, description: String) {
        viewModelScope.launch {
            val edu = Education(
                degree = degree,
                institution = institution,
                period = period,
                description = description
            )
            repository.saveEducation(edu)
            addNotification("Education milestone '${degree}' added.")
            refreshDbDiagnostics()
        }
    }

    fun deleteEducation(education: Education) {
        viewModelScope.launch {
            repository.deleteEducation(education)
            addNotification("Education milestone '${education.degree}' deleted.")
            refreshDbDiagnostics()
        }
    }

    // --- Admin Database Maintenance Commands ---
    fun runVacuumOptimization() {
        viewModelScope.launch {
            val success = sqlDatabaseController.runVacuum()
            if (success) {
                addNotification("🧹 SQLite VACUUM optimization executed! Database file defragmented.")
            } else {
                addNotification("❌ SQLite VACUUM execution failed.")
            }
            refreshDbDiagnostics()
        }
    }

    fun triggerOneClickDemoImport() {
        viewModelScope.launch {
            repository.clearAllData()
            repository.populateSampleDataIfEmpty()
            addNotification("🔄 Portfolio refreshed: Sample WordPress Demo imported successfully!")
            refreshDbDiagnostics()
        }
    }

    fun triggerBackup() {
        viewModelScope.launch {
            val file = sqlDatabaseController.backupDatabase()
            if (file != null) {
                addNotification("💾 Automatic Backup Successful! SQLite database backed up safely to internal storage.")
            } else {
                addNotification("❌ Database Backup Failed.")
            }
            refreshDbDiagnostics()
        }
    }
}
