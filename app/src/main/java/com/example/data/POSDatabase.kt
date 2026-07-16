package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.util.UUID

// ==========================================
// 1. DATABASE ENTITIES (String UUIDs for Sync)
// ==========================================

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val username: String,
    val passwordHash: String, // Simulated hashing
    val role: String, // "Admin", "Manager", "Cashier", "Employee"
    val displayName: String,
    val rememberMe: Boolean = false,
    val isLogged: Boolean = false
)

@Entity(tableName = "auth_sessions")
data class AuthSession(
    @PrimaryKey val userId: String,
    val token: String,
    val role: String,
    val username: String,
    val displayName: String,
    val permissions: String, // Comma-separated permissions list (e.g., "READ_REPORTS,WRITE_PRODUCTS,MANAGE_SETTINGS")
    val loginTimestamp: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + (30 * 24 * 60 * 60 * 1000L), // 30 Days expiration
    val isActive: Boolean = true
)

@Entity(tableName = "products")
data class Product(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val sku: String,
    val barcode: String,
    val category: String,
    val brand: String,
    val unit: String,
    val purchasePrice: Double,
    val retailPrice: Double,
    val wholesalePrice: Double,
    val stockQuantity: Int,
    val minStockAlert: Int,
    val expiryDate: String, // "YYYY-MM-DD"
    val imagePath: String = "",
    val isSynced: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    // --- Business Analytics & Metrics ---
    fun isLowStock(): Boolean = stockQuantity <= minStockAlert && stockQuantity > 0
    fun isOutOfStock(): Boolean = stockQuantity <= 0
    
    fun getRetailMarginAmount(): Double = retailPrice - purchasePrice
    fun getWholesaleMarginAmount(): Double = wholesalePrice - purchasePrice
    
    fun getRetailMarginPercentage(): Double {
        return if (purchasePrice > 0) (getRetailMarginAmount() / purchasePrice) * 100.0 else 0.0
    }
    
    fun getWholesaleMarginPercentage(): Double {
        return if (purchasePrice > 0) (getWholesaleMarginAmount() / purchasePrice) * 100.0 else 0.0
    }
    
    fun getWholesaleStockValue(): Double = stockQuantity * wholesalePrice
    fun getRetailStockValue(): Double = stockQuantity * retailPrice
    fun getPurchaseStockValue(): Double = stockQuantity * purchasePrice

    // --- Data Sync Mappers (Local SQLite Room Entity <-> Cloud Firestore Document Map) ---
    fun toFirestoreMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "name" to name,
            "sku" to sku,
            "barcode" to barcode,
            "category" to category,
            "brand" to brand,
            "unit" to unit,
            "purchasePrice" to purchasePrice,
            "retailPrice" to retailPrice,
            "wholesalePrice" to wholesalePrice,
            "stockQuantity" to stockQuantity,
            "minStockAlert" to minStockAlert,
            "expiryDate" to expiryDate,
            "imagePath" to imagePath,
            "lastUpdated" to lastUpdated
        )
    }

    companion object {
        fun fromFirestoreMap(id: String, map: Map<String, Any?>): Product {
            return Product(
                id = id,
                name = map["name"] as? String ?: "",
                sku = map["sku"] as? String ?: "",
                barcode = map["barcode"] as? String ?: "",
                category = map["category"] as? String ?: "",
                brand = map["brand"] as? String ?: "",
                unit = map["unit"] as? String ?: "",
                purchasePrice = (map["purchasePrice"] as? Number)?.toDouble() ?: 0.0,
                retailPrice = (map["retailPrice"] as? Number)?.toDouble() ?: 0.0,
                wholesalePrice = (map["wholesalePrice"] as? Number)?.toDouble() ?: 0.0,
                stockQuantity = (map["stockQuantity"] as? Number)?.toInt() ?: 0,
                minStockAlert = (map["minStockAlert"] as? Number)?.toInt() ?: 0,
                expiryDate = map["expiryDate"] as? String ?: "",
                imagePath = map["imagePath"] as? String ?: "",
                isSynced = true, // Received from server, so initially synced
                lastUpdated = (map["lastUpdated"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
        
        // --- Validation Engine ---
        fun validate(
            name: String,
            sku: String,
            purchasePrice: Double,
            retailPrice: Double,
            wholesalePrice: Double,
            stockQuantity: Int
        ): ProductValidationResult {
            if (name.isBlank()) return ProductValidationResult.Error("Product name cannot be empty")
            if (sku.isBlank()) return ProductValidationResult.Error("SKU code cannot be empty")
            if (purchasePrice < 0) return ProductValidationResult.Error("Purchase price cannot be negative")
            if (retailPrice < 0) return ProductValidationResult.Error("Retail price cannot be negative")
            if (wholesalePrice < 0) return ProductValidationResult.Error("Wholesale price cannot be negative")
            if (stockQuantity < 0) return ProductValidationResult.Error("Stock level cannot be negative")
            if (retailPrice < purchasePrice) return ProductValidationResult.Error("Warning: Retail price is lower than purchase cost")
            return ProductValidationResult.Valid
        }
    }
}

sealed class ProductValidationResult {
    object Valid : ProductValidationResult()
    data class Error(val message: String) : ProductValidationResult()
}

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val phone: String,
    val email: String,
    val address: String,
    val balance: Double = 0.0, // negative is debit, positive is credit (or vice versa)
    val isSynced: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "suppliers")
data class Supplier(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val phone: String,
    val email: String,
    val address: String,
    val balance: Double = 0.0,
    val isSynced: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "sales")
data class Sale(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val customerId: String,
    val customerName: String,
    val subtotal: Double,
    val discount: Double,
    val tax: Double,
    val totalAmount: Double,
    val paymentMethod: String, // "Cash", "Card", "JazzCash", "EasyPaisa", "Bank Transfer"
    val cashierName: String,
    val status: String = "Completed", // "Completed", "On Hold", "Returned"
    val isSynced: Boolean = false
)

@Entity(tableName = "sale_items")
data class SaleItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val saleId: String,
    val productId: String,
    val productName: String,
    val quantity: Int,
    val purchasePrice: Double, // Cost price at time of sale
    val salePrice: Double, // Selling price at time of sale
    val totalLinePrice: Double
)

@Entity(tableName = "purchases")
data class Purchase(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val supplierId: String,
    val supplierName: String,
    val totalAmount: Double,
    val status: String = "Completed", // "Completed", "Returned"
    val isSynced: Boolean = false
)

@Entity(tableName = "purchase_items")
data class PurchaseItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val purchaseId: String,
    val productId: String,
    val productName: String,
    val quantity: Int,
    val purchasePrice: Double,
    val totalLinePrice: Double
)

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val category: String,
    val amount: Double,
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)

@Entity(tableName = "audit_logs")
data class AuditLog(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val userId: String,
    val userName: String,
    val action: String, // "ADD_PRODUCT", "POS_SALE", "SYNC", etc.
    val details: String
)

// ==========================================
// 2. DATA ACCESS OBJECTS (DAOs)
// ==========================================

@Dao
interface POSDao {

    // --- Users ---
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM users WHERE isLogged = 1 LIMIT 1")
    suspend fun getLoggedInUser(): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Query("UPDATE users SET isLogged = 0")
    suspend fun logoutAllUsers()

    // --- Auth Sessions ---
    @Query("SELECT * FROM auth_sessions WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveSession(): AuthSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: AuthSession)

    @Query("UPDATE auth_sessions SET isActive = 0")
    suspend fun deactivateAllSessions()

    @Query("DELETE FROM auth_sessions")
    suspend fun clearAllSessions()

    // --- Products ---
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProductsFlow(): Flow<List<Product>>

    @Query("SELECT * FROM products ORDER BY name ASC")
    suspend fun getAllProducts(): List<Product>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: String): Product?

    @Query("SELECT * FROM products WHERE barcode = :barcode LIMIT 1")
    suspend fun getProductByBarcode(barcode: String): Product?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    // --- Customers ---
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomersFlow(): Flow<List<Customer>>

    @Query("SELECT * FROM customers ORDER BY name ASC")
    suspend fun getAllCustomers(): List<Customer>

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    suspend fun getCustomerById(id: String): Customer?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer)

    @Update
    suspend fun updateCustomer(customer: Customer)

    @Delete
    suspend fun deleteCustomer(customer: Customer)

    // --- Suppliers ---
    @Query("SELECT * FROM suppliers ORDER BY name ASC")
    fun getAllSuppliersFlow(): Flow<List<Supplier>>

    @Query("SELECT * FROM suppliers ORDER BY name ASC")
    suspend fun getAllSuppliers(): List<Supplier>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplier(supplier: Supplier)

    @Update
    suspend fun updateSupplier(supplier: Supplier)

    @Delete
    suspend fun deleteSupplier(supplier: Supplier)

    // --- Sales & Sale Items ---
    @Query("SELECT * FROM sales ORDER BY timestamp DESC")
    fun getAllSalesFlow(): Flow<List<Sale>>

    @Query("SELECT * FROM sales ORDER BY timestamp DESC")
    suspend fun getAllSales(): List<Sale>

    @Query("SELECT * FROM sales WHERE id = :id LIMIT 1")
    suspend fun getSaleById(id: String): Sale?

    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    suspend fun getItemsForSale(saleId: String): List<SaleItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: Sale)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleItem(item: SaleItem)

    @Transaction
    suspend fun saveSaleWithItems(sale: Sale, items: List<SaleItem>) {
        insertSale(sale)
        items.forEach { item ->
            insertSaleItem(item)
            // Deduct stock
            val product = getProductById(item.productId)
            if (product != null) {
                val newStock = (product.stockQuantity - item.quantity).coerceAtLeast(0)
                updateProduct(product.copy(stockQuantity = newStock, lastUpdated = System.currentTimeMillis()))
            }
        }
        // Update Customer Balance if credit (i.e. Not cash payment or guest)
        if (sale.customerId != "GUEST" && sale.paymentMethod == "On Credit") {
            val customer = getCustomerById(sale.customerId)
            if (customer != null) {
                updateCustomer(customer.copy(
                    balance = customer.balance - sale.totalAmount, // Negative balance indicates outstanding dues
                    lastUpdated = System.currentTimeMillis()
                ))
            }
        }
    }

    @Transaction
    suspend fun returnSale(saleId: String) {
        val sale = getSaleById(saleId) ?: return
        if (sale.status == "Returned") return // already returned
        
        // Update sale status
        updateSale(sale.copy(status = "Returned", isSynced = false))

        // Return stock
        val items = getItemsForSale(saleId)
        items.forEach { item ->
            val product = getProductById(item.productId)
            if (product != null) {
                val newStock = product.stockQuantity + item.quantity
                updateProduct(product.copy(stockQuantity = newStock, lastUpdated = System.currentTimeMillis()))
            }
        }

        // Refund customer credit balance if applicable
        if (sale.customerId != "GUEST" && sale.paymentMethod == "On Credit") {
            val customer = getCustomerById(sale.customerId)
            if (customer != null) {
                updateCustomer(customer.copy(
                    balance = customer.balance + sale.totalAmount,
                    lastUpdated = System.currentTimeMillis()
                ))
            }
        }
    }

    @Update
    suspend fun updateSale(sale: Sale)

    // --- Purchases & Purchase Items ---
    @Query("SELECT * FROM purchases ORDER BY timestamp DESC")
    fun getAllPurchasesFlow(): Flow<List<Purchase>>

    @Query("SELECT * FROM purchase_items WHERE purchaseId = :purchaseId")
    suspend fun getItemsForPurchase(purchaseId: String): List<PurchaseItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(purchase: Purchase)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchaseItem(item: PurchaseItem)

    @Transaction
    suspend fun savePurchaseWithItems(purchase: Purchase, items: List<PurchaseItem>) {
        insertPurchase(purchase)
        items.forEach { item ->
            insertPurchaseItem(item)
            // Add stock
            val product = getProductById(item.productId)
            if (product != null) {
                val newStock = product.stockQuantity + item.quantity
                updateProduct(product.copy(
                    stockQuantity = newStock, 
                    purchasePrice = item.purchasePrice, // update cost price to newest purchase price
                    lastUpdated = System.currentTimeMillis()
                ))
            }
        }
    }

    // --- Expenses ---
    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    fun getAllExpensesFlow(): Flow<List<Expense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    // --- Sync Operations ---
    @Query("SELECT * FROM products WHERE isSynced = 0")
    suspend fun getUnsyncedProducts(): List<Product>

    @Query("SELECT * FROM customers WHERE isSynced = 0")
    suspend fun getUnsyncedCustomers(): List<Customer>

    @Query("SELECT * FROM suppliers WHERE isSynced = 0")
    suspend fun getUnsyncedSuppliers(): List<Supplier>

    @Query("SELECT * FROM sales WHERE isSynced = 0")
    suspend fun getUnsyncedSales(): List<Sale>

    @Query("SELECT * FROM expenses WHERE isSynced = 0")
    suspend fun getUnsyncedExpenses(): List<Expense>

    @Transaction
    suspend fun markAllSynced() {
        // In a real app we'd upload each to Firestore. Here we bulk-mark them synced.
        // This is a robust mock simulation of Firebase sync.
        val products = getUnsyncedProducts()
        products.forEach { updateProduct(it.copy(isSynced = true)) }

        val customers = getUnsyncedCustomers()
        customers.forEach { updateCustomer(it.copy(isSynced = true)) }

        val suppliers = getUnsyncedSuppliers()
        suppliers.forEach { updateSupplier(it.copy(isSynced = true)) }

        val sales = getUnsyncedSales()
        sales.forEach { updateSale(it.copy(isSynced = true)) }

        val expenses = getUnsyncedExpenses()
        expenses.forEach { insertExpense(it.copy(isSynced = true)) }
    }
}

// ==========================================
// 3. ROOM DATABASE CLASS
// ==========================================

@Database(
    entities = [
        User::class,
        AuthSession::class,
        Product::class,
        Customer::class,
        Supplier::class,
        Sale::class,
        SaleItem::class,
        Purchase::class,
        PurchaseItem::class,
        Expense::class,
        AuditLog::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun posDao(): POSDao
}
