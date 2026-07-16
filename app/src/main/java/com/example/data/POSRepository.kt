package com.example.data

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class POSRepository(context: Context) {

    val db = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "zam_zam_pos_db"
    )
    .fallbackToDestructiveMigration()
    .build()

    val dao = db.posDao()

    // --- Flows ---
    val allProducts: Flow<List<Product>> = dao.getAllProductsFlow()
    val allCustomers: Flow<List<Customer>> = dao.getAllCustomersFlow()
    val allSuppliers: Flow<List<Supplier>> = dao.getAllSuppliersFlow()
    val allSales: Flow<List<Sale>> = dao.getAllSalesFlow()
    val allPurchases: Flow<List<Purchase>> = dao.getAllPurchasesFlow()
    val allExpenses: Flow<List<Expense>> = dao.getAllExpensesFlow()

    // --- Users & Authentication ---
    suspend fun getLoggedInUser(): User? = withContext(Dispatchers.IO) {
        dao.getLoggedInUser()
    }

    suspend fun loginUser(username: String, passwordText: String, rememberMe: Boolean = false): User? = withContext(Dispatchers.IO) {
        val user = dao.getUserByUsername(username)
        // Match raw password as a simple, reliable mock for testing. In prod we'd hash.
        if (user != null && user.passwordHash == passwordText) {
            dao.logoutAllUsers()
            val loggedUser = user.copy(isLogged = true, rememberMe = rememberMe)
            dao.updateUser(loggedUser)
            loggedUser
        } else {
            null
        }
    }

    suspend fun logoutUser() = withContext(Dispatchers.IO) {
        dao.logoutAllUsers()
        dao.deactivateAllSessions()
    }

    // --- Auth Sessions ---
    suspend fun getActiveAuthSession(): AuthSession? = withContext(Dispatchers.IO) {
        dao.getActiveSession()
    }

    suspend fun saveAuthSession(session: AuthSession) = withContext(Dispatchers.IO) {
        dao.deactivateAllSessions()
        dao.insertSession(session)
    }

    suspend fun clearAuthSessions() = withContext(Dispatchers.IO) {
        dao.deactivateAllSessions()
        dao.clearAllSessions()
    }

    suspend fun registerUser(username: String, passwordText: String, role: String, displayName: String): Boolean = withContext(Dispatchers.IO) {
        val existing = dao.getUserByUsername(username)
        if (existing != null) {
            false
        } else {
            val newUser = User(
                username = username,
                passwordHash = passwordText,
                role = role,
                displayName = displayName
            )
            dao.insertUser(newUser)
            true
        }
    }

    // --- Product Operations ---
    suspend fun saveProduct(product: Product) = withContext(Dispatchers.IO) {
        dao.insertProduct(product.copy(isSynced = false))
    }

    suspend fun updateProduct(product: Product) = withContext(Dispatchers.IO) {
        dao.updateProduct(product.copy(isSynced = false))
    }

    suspend fun deleteProduct(product: Product) = withContext(Dispatchers.IO) {
        dao.deleteProduct(product)
    }

    suspend fun getProductByBarcode(barcode: String): Product? = withContext(Dispatchers.IO) {
        dao.getProductByBarcode(barcode)
    }

    // --- Customer Operations ---
    suspend fun saveCustomer(customer: Customer) = withContext(Dispatchers.IO) {
        dao.insertCustomer(customer.copy(isSynced = false))
    }

    suspend fun updateCustomer(customer: Customer) = withContext(Dispatchers.IO) {
        dao.updateCustomer(customer.copy(isSynced = false))
    }

    suspend fun deleteCustomer(customer: Customer) = withContext(Dispatchers.IO) {
        dao.deleteCustomer(customer)
    }

    // --- Supplier Operations ---
    suspend fun saveSupplier(supplier: Supplier) = withContext(Dispatchers.IO) {
        dao.insertSupplier(supplier.copy(isSynced = false))
    }

    suspend fun updateSupplier(supplier: Supplier) = withContext(Dispatchers.IO) {
        dao.updateSupplier(supplier.copy(isSynced = false))
    }

    suspend fun deleteSupplier(supplier: Supplier) = withContext(Dispatchers.IO) {
        dao.deleteSupplier(supplier)
    }

    // --- Expense Operations ---
    suspend fun saveExpense(expense: Expense) = withContext(Dispatchers.IO) {
        dao.insertExpense(expense.copy(isSynced = false))
    }

    suspend fun deleteExpense(expense: Expense) = withContext(Dispatchers.IO) {
        dao.deleteExpense(expense)
    }

    // --- Sale Billing ---
    suspend fun checkoutSale(sale: Sale, items: List<SaleItem>) = withContext(Dispatchers.IO) {
        dao.saveSaleWithItems(sale, items)
    }

    suspend fun returnSaleInvoice(saleId: String) = withContext(Dispatchers.IO) {
        dao.returnSale(saleId)
    }

    suspend fun getItemsForSale(saleId: String): List<SaleItem> = withContext(Dispatchers.IO) {
        dao.getItemsForSale(saleId)
    }

    // --- Purchase Operations ---
    suspend fun checkoutPurchase(purchase: Purchase, items: List<PurchaseItem>) = withContext(Dispatchers.IO) {
        dao.savePurchaseWithItems(purchase, items)
    }

    suspend fun getItemsForPurchase(purchaseId: String): List<PurchaseItem> = withContext(Dispatchers.IO) {
        dao.getItemsForPurchase(purchaseId)
    }

    // --- Simulated Cloud Sync with Firestore ---
    suspend fun performCloudSync(): SyncSummary = withContext(Dispatchers.IO) {
        // Find unsynced
        val upProducts = dao.getUnsyncedProducts().size
        val upCustomers = dao.getUnsyncedCustomers().size
        val upSuppliers = dao.getUnsyncedSuppliers().size
        val upSales = dao.getUnsyncedSales().size
        val upExpenses = dao.getUnsyncedExpenses().size

        // Simulated cloud roundtrip latency
        kotlinx.coroutines.delay(1800)

        // Sync local
        dao.markAllSynced()

        SyncSummary(
            productsSynced = upProducts,
            customersSynced = upCustomers,
            suppliersSynced = upSuppliers,
            salesSynced = upSales,
            expensesSynced = upExpenses,
            totalSynced = upProducts + upCustomers + upSuppliers + upSales + upExpenses,
            timestamp = System.currentTimeMillis()
        )
    }

    // --- Insert Sample Data Helper ---
    suspend fun populateSampleDataIfEmpty() = withContext(Dispatchers.IO) {
        // Insert Admin and Manager users if none exist
        if (dao.getUserByUsername("admin") == null) {
            dao.insertUser(User(username = "admin", passwordHash = "admin123", role = "Admin", displayName = "Haji Zam Zam Admin"))
            dao.insertUser(User(username = "cashier", passwordHash = "cashier123", role = "Cashier", displayName = "Abid Cashier"))
            dao.insertUser(User(username = "manager", passwordHash = "manager123", role = "Manager", displayName = "Zafar Manager"))
        }

        // Insert products if empty
        val currentProducts = dao.getAllProducts()
        if (currentProducts.isEmpty()) {
            val sampleProducts = listOf(
                Product(name = "Premium Basmati Rice 10kg", sku = "RICE-BAS-10", barcode = "890123456001", category = "Groceries", brand = "Super Kernel", unit = "Pack", purchasePrice = 2800.0, retailPrice = 3300.0, wholesalePrice = 3150.0, stockQuantity = 45, minStockAlert = 10, expiryDate = "2027-12-01"),
                Product(name = "Dal Chana (Premium) 1kg", sku = "DAL-CHANA-01", barcode = "890123456002", category = "Groceries", brand = "Zam Zam Foods", unit = "Kg", purchasePrice = 260.0, retailPrice = 320.0, wholesalePrice = 295.0, stockQuantity = 120, minStockAlert = 20, expiryDate = "2027-06-15"),
                Product(name = "Sufi Cooking Oil 5 Liter", sku = "OIL-SUFI-05", barcode = "890123456003", category = "Cooking Essentials", brand = "Sufi", unit = "Cane", purchasePrice = 2450.0, retailPrice = 2750.0, wholesalePrice = 2620.0, stockQuantity = 30, minStockAlert = 5, expiryDate = "2027-03-30"),
                Product(name = "Lipton Yellow Label Tea 950g", sku = "TEA-LIP-950", barcode = "890123456004", category = "Beverages", brand = "Unilever", unit = "Box", purchasePrice = 1450.0, retailPrice = 1680.0, wholesalePrice = 1550.0, stockQuantity = 8, minStockAlert = 12, expiryDate = "2028-01-01"), // trigger low stock alert
                Product(name = "Lux Velvet Touch Soap 150g", sku = "SOAP-LUX-150", barcode = "890123456005", category = "Personal Care", brand = "Lux", unit = "Bar", purchasePrice = 120.0, retailPrice = 150.0, wholesalePrice = 135.0, stockQuantity = 150, minStockAlert = 25, expiryDate = "2029-04-10"),
                Product(name = "Colgate MaxFresh Paste 150g", sku = "PASTE-COL-150", barcode = "890123456006", category = "Personal Care", brand = "Colgate", unit = "Tube", purchasePrice = 240.0, retailPrice = 300.0, wholesalePrice = 270.0, stockQuantity = 0, minStockAlert = 15, expiryDate = "2028-08-20") // Out of Stock alert
            )
            sampleProducts.forEach { dao.insertProduct(it) }
        }

        // Insert customers if empty
        val currentCustomers = dao.getAllCustomers()
        if (currentCustomers.isEmpty()) {
            val sampleCustomers = listOf(
                Customer(name = "Imran Khan Retailer", phone = "03001234567", email = "imran@gmail.com", address = "Main Bazaar Ghalanai", balance = -45000.0), // owes 45,000 PKR
                Customer(name = "Muhammad Ali", phone = "03219876543", email = "ali@yahoo.com", address = "Sector F-11 Islamabad", balance = 0.0),
                Customer(name = "Kashif Traders", phone = "03124567890", email = "kashiftraders@gmail.com", address = "Karkhano Market Peshawar", balance = -125000.0) // owes 125,000 PKR
            )
            sampleCustomers.forEach { dao.insertCustomer(it) }
        }

        // Insert suppliers if empty
        val currentSuppliers = dao.getAllSuppliers()
        if (currentSuppliers.isEmpty()) {
            val sampleSuppliers = listOf(
                Supplier(name = "Peshawar WholeSale House", phone = "03339112233", email = "pesh_wholesale@gmail.com", address = "Rampura Bazaar Peshawar", balance = 85000.0),
                Supplier(name = "Nestle Pakistan Distributors", phone = "042111637853", email = "nestledist@nestle.com.pk", address = "Quetta Highway Multan", balance = 0.0)
            )
            sampleSuppliers.forEach { dao.insertSupplier(it) }
        }

        // Insert expenses if empty
        val currentExpensesList = dao.getUnsyncedExpenses()
        if (currentExpensesList.isEmpty()) {
            val sampleExpenses = listOf(
                Expense(category = "Electricity Bill", amount = 14500.0, description = "Zam Zam Shop June Electricity Bill"),
                Expense(category = "Staff Salaries", amount = 45000.0, description = "Salary paid to Cashier Abid"),
                Expense(category = "Shop Rent", amount = 60000.0, description = "Monthly rent for Ground Floor")
            )
            sampleExpenses.forEach { dao.insertExpense(it) }
        }
    }
}

data class SyncSummary(
    val productsSynced: Int,
    val customersSynced: Int,
    val suppliersSynced: Int,
    val salesSynced: Int,
    val expensesSynced: Int,
    val totalSynced: Int,
    val timestamp: Long
)
