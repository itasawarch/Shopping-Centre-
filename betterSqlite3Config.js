/**
 * Better-SQLite3 Database Configuration and Initialization Module
 * 
 * This module initializes a high-performance SQLite database connection using 'better-sqlite3'.
 * It configures modern database pragmas (like Write-Ahead Logging and Foreign Keys) and
 * defines the foundational tables (products, customers, suppliers, sales) and performance-optimizing
 * indices as specified in the schema requirements.
 */

const Database = require('better-sqlite3');
const path = require('path');

// Initialize database connection
const dbPath = process.env.DATABASE_PATH || path.join(__dirname, 'zam_zam_pos.db');
const db = new Database(dbPath, { verbose: console.log });

// Configure performance-optimizing SQLite PRAGMAs
db.pragma('journal_mode = WAL');       // Write-Ahead Logging for concurrent read/write operations
db.pragma('foreign_keys = ON');       // Enable Foreign Key constraints
db.pragma('synchronous = NORMAL');     // Optimize disk write performance while remaining safe
db.pragma('cache_size = -16000');      // Set cache size to ~16MB (negative means kibibytes)
db.pragma('temp_store = MEMORY');      // Store temporary tables in memory

console.log(`[Database] Initialized connection to: ${dbPath}`);

/**
 * Define and create the foundational database structures
 */
function initializeSchema() {
    // Enable transactional schema execution
    const initTransaction = db.transaction(() => {
        
        // --- 1. Products Table ---
        db.prepare(`
            CREATE TABLE IF NOT EXISTS products (
                id TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                sku TEXT NOT NULL UNIQUE,
                barcode TEXT,
                category TEXT,
                brand TEXT,
                unit TEXT,
                purchasePrice REAL DEFAULT 0.0,
                retailPrice REAL DEFAULT 0.0,
                wholesalePrice REAL DEFAULT 0.0,
                stockQuantity INTEGER DEFAULT 0,
                minStockAlert INTEGER DEFAULT 0,
                expiryDate TEXT,
                imagePath TEXT,
                isSynced INTEGER DEFAULT 0,
                lastUpdated INTEGER NOT NULL
            )
        `).run();

        // High-traffic indices for product queries
        db.prepare(`CREATE INDEX IF NOT EXISTS idx_products_sku ON products(sku)`).run();
        db.prepare(`CREATE INDEX IF NOT EXISTS idx_products_barcode ON products(barcode)`).run();
        db.prepare(`CREATE INDEX IF NOT EXISTS idx_products_category ON products(category)`).run();


        // --- 2. Customers Table ---
        db.prepare(`
            CREATE TABLE IF NOT EXISTS customers (
                id TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                phone TEXT NOT NULL,
                email TEXT,
                address TEXT,
                balance REAL DEFAULT 0.0,
                isSynced INTEGER DEFAULT 0,
                lastUpdated INTEGER NOT NULL
            )
        `).run();

        // High-traffic index for phone-based lookups
        db.prepare(`CREATE INDEX IF NOT EXISTS idx_customers_phone ON customers(phone)`).run();


        // --- 3. Suppliers Table ---
        db.prepare(`
            CREATE TABLE IF NOT EXISTS suppliers (
                id TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                phone TEXT NOT NULL,
                email TEXT,
                address TEXT,
                balance REAL DEFAULT 0.0,
                isSynced INTEGER DEFAULT 0,
                lastUpdated INTEGER NOT NULL
            )
        `).run();

        // High-traffic index for supplier lookups
        db.prepare(`CREATE INDEX IF NOT EXISTS idx_suppliers_phone ON suppliers(phone)`).run();


        // --- 4. Sales Table ---
        db.prepare(`
            CREATE TABLE IF NOT EXISTS sales (
                id TEXT PRIMARY KEY,
                timestamp INTEGER NOT NULL,
                customerId TEXT DEFAULT 'GUEST',
                customerName TEXT DEFAULT 'Guest Customer',
                subtotal REAL NOT NULL,
                discount REAL DEFAULT 0.0,
                tax REAL DEFAULT 0.0,
                totalAmount REAL NOT NULL,
                paymentMethod TEXT NOT NULL,
                cashierName TEXT NOT NULL,
                status TEXT DEFAULT 'Completed',
                isSynced INTEGER DEFAULT 0,
                FOREIGN KEY(customerId) REFERENCES customers(id) ON DELETE SET DEFAULT
            )
        `).run();

        // High-traffic indices for offline operations and analytics
        db.prepare(`CREATE INDEX IF NOT EXISTS idx_sales_timestamp ON sales(timestamp)`).run();
        db.prepare(`CREATE INDEX IF NOT EXISTS idx_sales_customerId ON sales(customerId)`).run();

        console.log('[Database] Foundational table structures and indices initialized successfully.');
    });

    try {
        initTransaction();
    } catch (error) {
        console.error('[Database] Failed to initialize schemas:', error);
        throw error;
    }
}

// Run schema initialization
initializeSchema();

// Export initialized db instance and schemas helper
module.exports = {
    db,
    initializeSchema
};
