/**
 * ZamZam ERP - 'Transaction Queue' Core Module (transactionQueue.js)
 * 
 * An advanced, highly reliable transaction queue manager that intercepts and buffers
 * outgoing database/API requests when offline, stores them inside local SQLite storage,
 * and replays them sequentially to Firestore upon network reconnection to ensure zero data loss.
 * 
 * Features:
 * - Automatically initializes its own schema in SQLite ('pending_transactions').
 * - High-fidelity "FIFO" (First-In, First-Out) replay sequence to preserve causality (preventing race conditions).
 * - Transparent interceptor wrappers for outgoing database/API operations.
 * - Automatic exponential backoff and retry scheduling upon reconnection.
 * - Production-ready transaction-safe execution and resilient error quarantine.
 */

const { db } = require('./betterSqlite3Config');
const crypto = require('crypto');

class TransactionQueue {
    /**
     * @param {Object} config
     * @param {Object} [config.sqliteDb] - Local SQLite adapter / instance (defaults to betterSqlite3Config's db)
     * @param {Object} [config.firestore] - Active Firestore web or admin SDK instance
     * @param {Function} [config.onStatusChange] - Status notification hook for UI feedback
     */
    constructor(config = {}) {
        this.sqlite = config.sqliteDb || db;
        this.firestore = config.firestore || null;
        this.onStatusChange = config.onStatusChange || (() => {});
        
        // Connection tracking state
        this.isOnlineState = typeof navigator !== 'undefined' ? navigator.onLine : true;
        this.isProcessing = false;
        this.retryTimeout = null;
        this.retryDelay = 2000; // 2 seconds starting backoff
        this.maxRetryDelay = 60000; // 1 minute maximum

        this.initializeQueueTable();
        this.setupNetworkListeners();
    }

    /**
     * Set up the necessary SQLite tables for persisting transactions
     */
    initializeQueueTable() {
        try {
            this.sqlite.prepare(`
                CREATE TABLE IF NOT EXISTS pending_transactions (
                    id TEXT PRIMARY KEY,
                    timestamp INTEGER NOT NULL,
                    action TEXT NOT NULL,          -- 'CREATE', 'UPDATE', 'DELETE'
                    collection TEXT NOT NULL,      -- e.g., 'products', 'sales', 'customers'
                    docId TEXT NOT NULL,           -- Target document/record ID
                    payload TEXT NOT NULL,         -- JSON stringified request body / data
                    attempts INTEGER DEFAULT 0,    -- Sync attempts count
                    lastAttemptError TEXT,         -- Last recorded failure reason
                    quarantined INTEGER DEFAULT 0  -- 1 if bad record (skipped to prevent queue blocking)
                )
            `).run();

            // Create timestamp index for fast FIFO chronological sorting
            this.sqlite.prepare(`
                CREATE INDEX IF NOT EXISTS idx_pending_tx_timestamp 
                ON pending_transactions(timestamp)
            `).run();

            // Index to look up pending txs for a specific document
            this.sqlite.prepare(`
                CREATE INDEX IF NOT EXISTS idx_pending_tx_doc 
                ON pending_transactions(collection, docId)
            `).run();

            console.log('[TransactionQueue] SQLite schema and indices initialized successfully.');
        } catch (error) {
            console.error('[TransactionQueue] Failed to initialize SQLite schema:', error);
            throw error;
        }
    }

    /**
     * Setup environment/browser network listeners if running in a window environment
     */
    setupNetworkListeners() {
        if (typeof window !== 'undefined' && window.addEventListener) {
            window.addEventListener('online', () => this.handleNetworkStateChange(true));
            window.addEventListener('offline', () => this.handleNetworkStateChange(false));
            this.isOnlineState = navigator.onLine;
        }
    }

    /**
     * Update internal connection state and trigger playback if returning online
     * @param {boolean} online 
     */
    async handleNetworkStateChange(online) {
        this.isOnlineState = online;
        console.log(`[TransactionQueue] Network state transition detected: ${online ? 'ONLINE' : 'OFFLINE'}`);
        
        this.onStatusChange({
            status: online ? 'online' : 'offline',
            message: online ? 'Network reconnected. Syncing transaction queue.' : 'Terminal went offline. Buffering mutations.'
        });

        if (online) {
            this.retryDelay = 2000; // Reset backoff
            await this.processQueue();
        }
    }

    /**
     * Enqueues a write transaction to SQLite for offline buffering
     * 
     * @param {string} action - 'CREATE' | 'UPDATE' | 'DELETE'
     * @param {string} collection - The Firestore target collection
     * @param {string} docId - The document's primary key ID
     * @param {Object} payload - The transaction request body / data payload
     * @returns {string} Generated transaction ID
     */
    enqueue(action, collection, docId, payload) {
        const id = crypto.randomUUID ? crypto.randomUUID() : crypto.randomBytes(16).toString('hex');
        const timestamp = Date.now();
        const serializedPayload = JSON.stringify(payload || {});

        console.log(`[TransactionQueue] Enqueuing ${action} for ${collection}/${docId} (Tx ID: ${id})`);

        try {
            const stmt = this.sqlite.prepare(`
                INSERT INTO pending_transactions (
                    id, timestamp, action, collection, docId, payload, attempts, quarantined
                ) VALUES (?, ?, ?, ?, ?, ?, 0, 0)
            `);
            stmt.run(id, timestamp, action.toUpperCase(), collection, docId, serializedPayload);

            // Dynamically flag the corresponding local table record as unsynced
            this.markLocalRecordSyncedStatus(collection, docId, 0);

            this.onStatusChange({
                status: 'buffered',
                message: `Offline mode: ${action} request for ${collection} cached locally.`,
                txId: id
            });

            // If we are currently online, trigger background playback immediately
            if (this.isOnlineState) {
                this.processQueue().catch(err => {
                    console.error('[TransactionQueue] Background auto-processing failed:', err);
                });
            }

            return id;
        } catch (error) {
            console.error('[TransactionQueue] Failed to enqueue transaction:', error);
            throw error;
        }
    }

    /**
     * Intercepts an outgoing API / database mutation request.
     * If the system is online, it executes the operation directly.
     * If it fails due to network/offline conditions, it automatically enqueues
     * the transaction to SQLite for offline preservation.
     * 
     * @param {Function} outgoingRequestFn - An async function representing the Firestore or external API operation
     * @param {string} action - 'CREATE' | 'UPDATE' | 'DELETE'
     * @param {string} collection - Target collection/endpoint
     * @param {string} docId - Record primary key identifier
     * @param {Object} payload - The data payload of the operation
     * @returns {Promise<any>} The result of the direct call or the generated queued transaction ID
     */
    async intercept(outgoingRequestFn, action, collection, docId, payload) {
        if (this.isOnlineState && this.firestore) {
            try {
                console.log(`[TransactionQueue] Interceptor: Online. Routing directly to backend for ${collection}/${docId}`);
                const result = await outgoingRequestFn();
                
                // On direct success, verify local is marked as synced
                this.markLocalRecordSyncedStatus(collection, docId, 1);
                return result;
            } catch (error) {
                // If it is a real network/timeout error, intercept and fallback to SQLite queue
                if (this.isNetworkError(error)) {
                    console.warn(`[TransactionQueue] Direct API write failed due to network error. Falling back to offline queue.`);
                    return this.enqueue(action, collection, docId, payload);
                } else {
                    // Throw validation or authentication errors directly back to the caller
                    console.error(`[TransactionQueue] Direct write aborted due to terminal database/validation error:`, error);
                    throw error;
                }
            }
        } else {
            // System is offline, capture transaction directly inside SQLite
            console.log(`[TransactionQueue] Interceptor: Offline. Queuing ${action} mutation.`);
            return this.enqueue(action, collection, docId, payload);
        }
    }

    /**
     * Sequentially processes all pending transactions stored in SQLite.
     * Ensures strict FIFO order to guarantee state causality.
     */
    async processQueue() {
        if (this.isProcessing) return;
        if (!this.isOnlineState) {
            console.log('[TransactionQueue] Playback requested, but terminal is OFFLINE. Aborting.');
            return;
        }

        // Lock queue processing to prevent concurrent replay collisions
        this.isProcessing = true;
        console.log('[TransactionQueue] Starting transaction playback queue processing...');

        try {
            // Retrieve all unquarantined transactions sorted oldest to newest (FIFO)
            const transactions = this.sqlite.prepare(`
                SELECT * FROM pending_transactions 
                WHERE quarantined = 0 
                ORDER BY timestamp ASC
            `).all();

            console.log(`[TransactionQueue] Found ${transactions.length} pending transactions to replay.`);

            for (const tx of transactions) {
                // Check connectivity before each execution to react instantly to sudden drops
                if (!this.isOnlineState) {
                    console.log('[TransactionQueue] Network connection severed during playbacks. Halting execution.');
                    break;
                }

                console.log(`[TransactionQueue] Replaying Tx [${tx.id}] - ${tx.action} on ${tx.collection}/${tx.docId}`);
                const success = await this.replayTransaction(tx);

                if (!success) {
                    // Halting queue processing on network failures. Exponential backoff will retry later.
                    break;
                }
            }
        } catch (error) {
            console.error('[TransactionQueue] Critical queue loop error:', error);
        } finally {
            this.isProcessing = false;
            console.log('[TransactionQueue] Finished transaction playback execution cycle.');
        }
    }

    /**
     * Executes a single transaction on Firestore.
     * 
     * @param {Object} tx - The transaction row from SQLite
     * @returns {Promise<boolean>} True if transaction processed successfully or quarantined; False if network error (halt queue)
     */
    async replayTransaction(tx) {
        if (!this.firestore) {
            console.warn('[TransactionQueue] Firestore SDK instance is not registered. Cannot process queue playback.');
            return false;
        }

        const payload = JSON.parse(tx.payload);
        const docRef = this.firestore.collection(tx.collection).doc(tx.docId);

        try {
            // 1. Replay the operation on Firebase Firestore
            if (tx.action === 'CREATE' || tx.action === 'UPDATE') {
                await docRef.set(payload, { merge: true });
            } else if (tx.action === 'DELETE') {
                await docRef.delete();
            }

            // 2. Success: Delete transaction from the pending queue
            this.sqlite.prepare('DELETE FROM pending_transactions WHERE id = ?').run(tx.id);
            
            // 3. Keep local replica's sync flag in sync
            this.markLocalRecordSyncedStatus(tx.collection, tx.docId, 1);
            
            console.log(`[TransactionQueue] Tx [${tx.id}] replayed and purged successfully.`);
            
            this.onStatusChange({
                status: 'replayed',
                message: `Successfully synced ${tx.action} operation for ${tx.collection}.`,
                txId: tx.id
            });

            return true;
        } catch (error) {
            console.error(`[TransactionQueue] Error replaying Tx [${tx.id}]:`, error);

            if (this.isNetworkError(error)) {
                // Increment attempts count in SQLite
                this.sqlite.prepare(`
                    UPDATE pending_transactions 
                    SET attempts = attempts + 1, 
                        lastAttemptError = ? 
                    WHERE id = ?
                `).run(error.message, tx.id);

                // Schedule exponential backoff retry
                this.scheduleRetry();
                return false; // Tells the loop to halt queue processing
            } else {
                // Non-retriable failure (e.g. Permission Denied, validation error on backend)
                // We quarantine the transaction so it doesn't block remaining queues forever
                console.error(`[TransactionQueue] Non-retriable failure. Quarantine Tx [${tx.id}] to prevent queue blockage.`);
                
                this.sqlite.prepare(`
                    UPDATE pending_transactions 
                    SET quarantined = 1, 
                        attempts = attempts + 1, 
                        lastAttemptError = ? 
                    WHERE id = ?
                `).run(`QUARANTINED: ${error.message}`, tx.id);

                this.onStatusChange({
                    status: 'quarantined',
                    message: `Transaction quarantined due to validation error: ${error.message}`,
                    txId: tx.id
                });

                return true; // Return true to continue processing next transactions in line
            }
        }
    }

    /**
     * Schedules a background retry attempt with exponential backoff
     */
    scheduleRetry() {
        if (this.retryTimeout) {
            clearTimeout(this.retryTimeout);
        }

        console.log(`[TransactionQueue] Playback failed. Scheduling retry in ${this.retryDelay / 1000}s...`);
        
        this.retryTimeout = setTimeout(() => {
            this.processQueue().catch(err => {
                console.error('[TransactionQueue] Scheduled retry error:', err);
            });
            // Double delay up to the maximum limit
            this.retryDelay = Math.min(this.retryDelay * 2, this.maxRetryDelay);
        }, this.retryDelay);
    }

    /**
     * Helper to mark a local SQLite table's isSynced flag for consistency
     */
    markLocalRecordSyncedStatus(collection, id, syncedValue) {
        const validTables = ['products', 'customers', 'suppliers', 'sales', 'expenses'];
        const targetTable = collection.toLowerCase();

        if (validTables.includes(targetTable)) {
            try {
                // Update local SQLite record's sync flag
                const stmt = this.sqlite.prepare(`
                    UPDATE ${targetTable} 
                    SET isSynced = ? 
                    WHERE id = ?
                `);
                const result = stmt.run(syncedValue, id);
                if (result.changes > 0) {
                    console.log(`[TransactionQueue] Synced local table ${targetTable} record ${id} to isSynced = ${syncedValue}`);
                }
            } catch (err) {
                // Table might not exist or column missing, log silently or warn
                console.warn(`[TransactionQueue] Local record sync status sync skipped for table '${targetTable}':`, err.message);
            }
        }
    }

    /**
     * Helper to detect if an error is network-related (offline, timeout, etc.)
     * @param {Error} error 
     * @returns {boolean}
     */
    isNetworkError(error) {
        const msg = error.message ? error.message.toLowerCase() : '';
        return (
            msg.includes('network') ||
            msg.includes('offline') ||
            msg.includes('timeout') ||
            msg.includes('failed to fetch') ||
            msg.includes('connection') ||
            error.code === 'unavailable' || // Firestore offline code
            error.code === 'deadline-exceeded'
        );
    }

    /**
     * Force connection status change manually (highly useful for testing offline/online simulations)
     * @param {boolean} online 
     */
    simulateConnectionState(online) {
        this.handleNetworkStateChange(online);
    }

    /**
     * Retrieves all currently pending queue entries for inspection or logging
     * @returns {Array<Object>} Pending transactions
     */
    getPendingTransactions() {
        try {
            return this.sqlite.prepare('SELECT * FROM pending_transactions ORDER BY timestamp ASC').all();
        } catch (error) {
            console.error('[TransactionQueue] Failed to query pending transactions list:', error);
            return [];
        }
    }
}

module.exports = TransactionQueue;
