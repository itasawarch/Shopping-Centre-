package com.example.ui

import android.app.Application
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
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
    DASHBOARD, POS, PRODUCTS, CUSTOMERS, SUPPLIERS, EXPENSES, REPORTS, SETTINGS
}

class POSViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = POSRepository(application)
    val sqlDatabaseController = SQLiteDatabaseController(application, repository)
    val firebaseAuthService = FirebaseAuthService(application, repository)

    // --- SQLite Database Controller Diagnostics State ---
    var dbSize by mutableStateOf(0L)
    val dbTableStats = mutableStateListOf<TableStat>()
    var rawQueryResult by mutableStateOf<List<Map<String, Any?>>?>(null)
    var rawQueryText by mutableStateOf("SELECT * FROM products LIMIT 5")
    var rawQueryError by mutableStateOf<String?>(null)

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
            // Check if it's a mutation query
            val isMutation = rawQueryText.trim().uppercase().startsWith("INSERT") ||
                    rawQueryText.trim().uppercase().startsWith("UPDATE") ||
                    rawQueryText.trim().uppercase().startsWith("DELETE")
            
            val result = sqlDatabaseController.executeRawQuery(rawQueryText)
            rawQueryResult = result
            
            if (isMutation) {
                refreshDbDiagnostics()
                refreshAlerts()
            }
        }
    }

    // --- State Observables (Flows connected to Database) ---
    val products: StateFlow<List<Product>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customers: StateFlow<List<Customer>> = repository.allCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val suppliers: StateFlow<List<Supplier>> = repository.allSuppliers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sales: StateFlow<List<Sale>> = repository.allSales
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val purchases: StateFlow<List<Purchase>> = repository.allPurchases
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenses: StateFlow<List<Expense>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Navigation & Flow State ---
    var currentScreen by mutableStateOf(Screen.LOGIN)
    var currentTab by mutableStateOf(MainTab.DASHBOARD)

    // --- Auth Input State ---
    var usernameInput by mutableStateOf("")
    var passwordInput by mutableStateOf("")
    var rememberMe by mutableStateOf(true)
    var authError by mutableStateOf<String?>(null)

    // --- User Registration State ---
    var regUsername by mutableStateOf("")
    var regPassword by mutableStateOf("")
    var regDisplayName by mutableStateOf("")
    var regRole by mutableStateOf("Cashier") // Admin, Manager, Cashier, Employee

    // --- Active Logged User ---
    var loggedInUser by mutableStateOf<User?>(null)

    // --- POS Billing Cart State ---
    val cart = mutableStateListOf<Pair<Product, Int>>() // Pair of Product to Quantity
    var posDiscount by mutableStateOf(0.0) // Discount amount in PKR
    var posTaxRate by mutableStateOf(2.0) // Tax percentage (e.g. 2% FBR standard)
    var selectedCustomer by mutableStateOf<Customer?>(null)
    var posPaymentMethod by mutableStateOf("Cash") // Cash, Card, JazzCash, EasyPaisa, Bank Transfer, On Credit
    var isCheckingOut by mutableStateOf(false)
    var lastCheckoutReceipt by mutableStateOf<Pair<Sale, List<SaleItem>>?>(null)

    // On Hold Sales: Key is hold timestamp/label, value is list of cart items
    val holdSales = mutableStateMapOf<String, List<Pair<Product, Int>>>()

    // --- Product Search & Filters ---
    var productSearchQuery by mutableStateOf("")
    var productFilterCategory by mutableStateOf("All")
    var productLowStockFilter by mutableStateOf(false)

    // --- Customer Search ---
    var customerSearchQuery by mutableStateOf("")

    // --- Supplier Search ---
    var supplierSearchQuery by mutableStateOf("")

    // --- Settings State (Saved dynamically or reset) ---
    var shopName by mutableStateOf("Zam Zam Whole Sale & Shopping Centre")
    var shopAddress by mutableStateOf("Main Bazaar Ghalanai, Mohmand")
    var shopPhone by mutableStateOf("0300-9876543")
    var shopCurrency by mutableStateOf("PKR")
    var appLanguage by mutableStateOf("English") // "English" or "Urdu"
    var isDarkMode by mutableStateOf(false)
    var receiptDesign by mutableStateOf("Thermal 80mm") // Thermal 80mm, A4 Standard

    // --- Sync State ---
    var syncing by mutableStateOf(false)
    var lastSyncSummary by mutableStateOf<SyncSummary?>(null)

    // --- Notifications Alert Feed ---
    val notifications = mutableStateListOf<String>()

    init {
        viewModelScope.launch {
            // Load sample data on startup
            repository.populateSampleDataIfEmpty()
            
            // Check if user is already logged in (Firebase state or fallback to local rememberMe session)
            val user = firebaseAuthService.getCurrentUser()
            if (user != null) {
                loggedInUser = user
                currentScreen = Screen.MAIN
                addNotification("Welcome back, ${user.displayName}! (Secured Session)")
            }
            refreshAlerts()
            refreshDbDiagnostics()
        }
    }

    // --- Refresh System Notifications (Stock alerts, Expiries) ---
    fun refreshAlerts() {
        viewModelScope.launch {
            val allProds = repository.dao.getAllProducts()
            notifications.clear()
            var lowStockCount = 0
            var outOfStockCount = 0
            
            allProds.forEach { prod ->
                if (prod.stockQuantity == 0) {
                    outOfStockCount++
                    notifications.add("🚨 OUT OF STOCK: ${prod.name} has 0 left!")
                } else if (prod.stockQuantity <= prod.minStockAlert) {
                    lowStockCount++
                    notifications.add("⚠️ LOW STOCK: ${prod.name} has only ${prod.stockQuantity} left!")
                }
            }
            if (lowStockCount > 0 || outOfStockCount > 0) {
                notifications.add("📊 Alert summary: $outOfStockCount out of stock, $lowStockCount low stock products require reorder.")
            }
        }
    }

    private fun addNotification(msg: String) {
        if (!notifications.contains(msg)) {
            notifications.add(0, msg)
        }
    }

    // --- Authentication Actions ---
    fun performLogin() {
        if (usernameInput.isBlank() || passwordInput.isBlank()) {
            authError = "Username and password cannot be empty."
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
                    val source = if (result.isFirebase) "Firebase Cloud" else "Offline Database"
                    addNotification("Secured login via $source: ${result.user.displayName} (${result.user.role}).")
                    refreshAlerts()
                    refreshDbDiagnostics()
                }
                is AuthResult.Error -> {
                    authError = result.message
                }
            }
        }
    }

    fun performSignup() {
        if (regUsername.isBlank() || regPassword.isBlank() || regDisplayName.isBlank()) {
            authError = "All fields are required."
            return
        }
        viewModelScope.launch {
            val result = firebaseAuthService.signUp(regUsername, regPassword, regDisplayName, regRole)
            when (result) {
                is AuthResult.Success -> {
                    authError = null
                    usernameInput = regUsername
                    regUsername = ""
                    regPassword = ""
                    regDisplayName = ""
                    currentScreen = Screen.LOGIN
                    val source = if (result.isFirebase) "Firebase Cloud" else "Offline Database"
                    addNotification("Account registered on $source successfully! Please login.")
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
            currentScreen = Screen.LOGIN
            currentTab = MainTab.DASHBOARD
            cart.clear()
            selectedCustomer = null
            addNotification("Logged out successfully.")
        }
    }

    // --- POS Billing Cart Actions ---
    fun addProductToCart(product: Product) {
        val existingIndex = cart.indexOfFirst { it.first.id == product.id }
        if (existingIndex >= 0) {
            val currentQty = cart[existingIndex].second
            if (currentQty < product.stockQuantity) {
                cart[existingIndex] = Pair(product, currentQty + 1)
            } else {
                addNotification("Cannot exceed available stock of ${product.stockQuantity} for ${product.name}")
            }
        } else {
            if (product.stockQuantity > 0) {
                cart.add(Pair(product, 1))
            } else {
                addNotification("${product.name} is out of stock.")
            }
        }
    }

    fun decreaseQuantityInCart(product: Product) {
        val index = cart.indexOfFirst { it.first.id == product.id }
        if (index >= 0) {
            val currentQty = cart[index].second
            if (currentQty > 1) {
                cart[index] = Pair(product, currentQty - 1)
            } else {
                cart.removeAt(index)
            }
        }
    }

    fun removeProductFromCart(product: Product) {
        cart.removeAll { it.first.id == product.id }
    }

    fun getCartSubtotal(): Double {
        return cart.sumOf { it.first.retailPrice * it.second }
    }

    fun getCartTax(): Double {
        return (getCartSubtotal() - posDiscount) * (posTaxRate / 100.0)
    }

    fun getCartTotal(): Double {
        return (getCartSubtotal() - posDiscount + getCartTax()).coerceAtLeast(0.0)
    }

    fun clearCart() {
        cart.clear()
        posDiscount = 0.0
        selectedCustomer = null
    }

    // Hold Sale
    fun holdCurrentSale() {
        if (cart.isEmpty()) return
        val timestamp = SimpleDateFormat("HH:mm:ss (dd-MMM)", Locale.getDefault()).format(Date())
        val holdLabel = "Hold # ${holdSales.size + 1} - $timestamp"
        holdSales[holdLabel] = cart.toList()
        cart.clear()
        addNotification("Sale held successfully: $holdLabel")
    }

    // Resume Sale
    fun resumeHeldSale(label: String) {
        val items = holdSales[label] ?: return
        cart.clear()
        cart.addAll(items)
        holdSales.remove(label)
        addNotification("Resumed sale: $label")
    }

    // POS Checkout
    fun checkout() {
        if (cart.isEmpty()) return
        isCheckingOut = true
        viewModelScope.launch {
            val saleId = UUID.randomUUID().toString()
            val sub = getCartSubtotal()
            val disc = posDiscount
            val tax = getCartTax()
            val tot = getCartTotal()
            
            val sale = Sale(
                id = saleId,
                customerId = selectedCustomer?.id ?: "GUEST",
                customerName = selectedCustomer?.name ?: "Guest Customer",
                subtotal = sub,
                discount = disc,
                tax = tax,
                totalAmount = tot,
                paymentMethod = posPaymentMethod,
                cashierName = loggedInUser?.displayName ?: "Cashier",
                status = "Completed",
                isSynced = false
            )

            val saleItems = cart.map { (prod, qty) ->
                SaleItem(
                    saleId = saleId,
                    productId = prod.id,
                    productName = prod.name,
                    quantity = qty,
                    purchasePrice = prod.purchasePrice,
                    salePrice = prod.retailPrice,
                    totalLinePrice = prod.retailPrice * qty
                )
            }

            repository.checkoutSale(sale, saleItems)
            lastCheckoutReceipt = Pair(sale, saleItems)
            
            // Notification
            addNotification("🛒 POS Invoice Generated: ${sale.customerName} - Total PKR ${sale.totalAmount}")
            
            // Reset states
            clearCart()
            refreshAlerts()
            isCheckingOut = false
        }
    }

    // Invoice Return
    fun performInvoiceReturn(saleId: String) {
        viewModelScope.launch {
            repository.returnSaleInvoice(saleId)
            addNotification("🔄 Invoice Return Processed: Sale ID $saleId stock restored")
            refreshAlerts()
        }
    }

    // --- Product Management CRUD ---
    fun addProduct(
        name: String,
        sku: String,
        barcode: String,
        category: String,
        brand: String,
        unit: String,
        purchasePrice: Double,
        retailPrice: Double,
        wholesalePrice: Double,
        stockQuantity: Int,
        minStockAlert: Int,
        expiryDate: String
    ) {
        viewModelScope.launch {
            val p = Product(
                name = name,
                sku = sku.ifBlank { "SKU-" + (1000..9999).random() },
                barcode = barcode.ifBlank { (100000000000..999999999999).random().toString() },
                category = category,
                brand = brand,
                unit = unit,
                purchasePrice = purchasePrice,
                retailPrice = retailPrice,
                wholesalePrice = wholesalePrice,
                stockQuantity = stockQuantity,
                minStockAlert = minStockAlert,
                expiryDate = expiryDate.ifBlank { "2027-12-31" }
            )
            repository.saveProduct(p)
            addNotification("📦 Product Added: $name")
            refreshAlerts()
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            addNotification("❌ Product Deleted: ${product.name}")
            refreshAlerts()
        }
    }

    // --- Customer Operations ---
    fun addCustomer(name: String, phone: String, email: String, address: String) {
        viewModelScope.launch {
            val c = Customer(name = name, phone = phone, email = email, address = address, balance = 0.0)
            repository.saveCustomer(c)
            addNotification("👤 Customer Registered: $name")
        }
    }

    fun updateCustomerBalance(customerId: String, adjustAmount: Double, isCredit: Boolean) {
        viewModelScope.launch {
            val customer = repository.dao.getCustomerById(customerId) ?: return@launch
            val adjustment = if (isCredit) adjustAmount else -adjustAmount
            val updated = customer.copy(
                balance = customer.balance + adjustment,
                lastUpdated = System.currentTimeMillis()
            )
            repository.updateCustomer(updated)
            addNotification("💳 Customer Balance Adjusted: ${customer.name} PKR $adjustAmount")
        }
    }

    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
            addNotification("❌ Customer Record Deleted: ${customer.name}")
        }
    }

    // --- Supplier Operations ---
    fun addSupplier(name: String, phone: String, email: String, address: String) {
        viewModelScope.launch {
            val s = Supplier(name = name, phone = phone, email = email, address = address, balance = 0.0)
            repository.saveSupplier(s)
            addNotification("🏭 Supplier Registered: $name")
        }
    }

    fun updateSupplierBalance(supplierId: String, amount: Double) {
        viewModelScope.launch {
            val s = repository.dao.getAllSuppliers().find { it.id == supplierId } ?: return@launch
            val updated = s.copy(balance = s.balance + amount, lastUpdated = System.currentTimeMillis())
            repository.updateSupplier(updated)
            addNotification("💳 Supplier Balance Updated: ${s.name} balance is now PKR ${updated.balance}")
        }
    }

    fun deleteSupplier(supplier: Supplier) {
        viewModelScope.launch {
            repository.deleteSupplier(supplier)
            addNotification("❌ Supplier Record Deleted: ${supplier.name}")
        }
    }

    // --- Expense Operations ---
    fun addExpense(category: String, amount: Double, desc: String) {
        if (amount <= 0.0) return
        viewModelScope.launch {
            val e = Expense(category = category, amount = amount, description = desc)
            repository.saveExpense(e)
            addNotification("💸 Expense Logged: $category - PKR $amount")
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
            addNotification("💸 Expense Deleted: ${expense.category}")
        }
    }

    // --- Bulk Stock Purchase (Supplier Purchase Flow) ---
    fun logBulkPurchase(supplierId: String, supplierName: String, productId: String, productName: String, quantity: Int, costPrice: Double) {
        viewModelScope.launch {
            val purchaseId = UUID.randomUUID().toString()
            val total = quantity * costPrice
            val p = Purchase(
                id = purchaseId,
                supplierId = supplierId,
                supplierName = supplierName,
                totalAmount = total
            )
            val pItem = PurchaseItem(
                purchaseId = purchaseId,
                productId = productId,
                productName = productName,
                quantity = quantity,
                purchasePrice = costPrice,
                totalLinePrice = total
            )
            repository.checkoutPurchase(p, listOf(pItem))
            addNotification("📦 Supplier Purchase Logged: $productName ($quantity pack) added from $supplierName")
            refreshAlerts()
        }
    }

    // --- Simulated Firebase Cloud Synced Indicator ---
    fun syncWithCloud() {
        if (syncing) return
        syncing = true
        addNotification("🔄 Syncing SQLite data with Firebase Cloud Firestore...")
        viewModelScope.launch {
            val summary = repository.performCloudSync()
            lastSyncSummary = summary
            syncing = false
            addNotification("✅ Sync Complete: ${summary.totalSynced} records safely synced to Firestore cloud!")
        }
    }

    // --- Settings Preferences Action ---
    fun saveShopSettings(name: String, addr: String, ph: String, curr: String, lang: String, receipt: String) {
        shopName = name
        shopAddress = addr
        shopPhone = ph
        shopCurrency = curr
        appLanguage = lang
        receiptDesign = receipt
        addNotification("⚙️ Shop configuration saved successfully.")
    }
}
