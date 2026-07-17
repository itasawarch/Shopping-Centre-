package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.Product
import com.example.ui.POSViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsTab(viewModel: POSViewModel) {
    val productsList by viewModel.products.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialogForProduct by remember { mutableStateOf<Product?>(null) }

    // Dropdown list categories
    val categories = listOf("All", "Groceries", "Beverages", "Cooking Essentials", "Personal Care", "Snacks")

    // Dynamic filtering
    val filteredList = productsList.filter { prod ->
        val matchesSearch = prod.name.contains(viewModel.productSearchQuery, ignoreCase = true) ||
                prod.sku.contains(viewModel.productSearchQuery, ignoreCase = true) ||
                prod.barcode == viewModel.productSearchQuery
        val matchesCategory = viewModel.productFilterCategory == "All" || prod.category == viewModel.productFilterCategory
        val matchesLowStock = !viewModel.productLowStockFilter || (prod.stockQuantity <= prod.minStockAlert)
        matchesSearch && matchesCategory && matchesLowStock
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("products_tab")
            .padding(16.dp)
    ) {
        // TOP Header actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Product Inventory", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Text("${productsList.size} items listed total", fontSize = 12.sp, color = Color.Gray)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Excel Import/Export mock actions
                Button(
                    onClick = {
                        // Simulated excel report download
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE2E8F0)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Black)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Excel Export", fontSize = 11.sp, color = Color.Black)
                }

                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007A48)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("add_product_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Product", fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search & Low Stock Toggle Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = viewModel.productSearchQuery,
                onValueChange = { viewModel.productSearchQuery = it },
                placeholder = { Text("Search product name, SKU or barcode...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF007A48)) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF007A48),
                    unfocusedBorderColor = Color(0xFFCBD5E1)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1.5f)
                    .testTag("product_query_field")
            )

            // Low Stock Toggle filter
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(
                        if (viewModel.productLowStockFilter) Color(0xFFFEE2E2) else Color(0xFFF1F5F9),
                        RoundedCornerShape(10.dp)
                    )
                    .clickable { viewModel.productLowStockFilter = !viewModel.productLowStockFilter }
                    .padding(horizontal = 12.dp, vertical = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (viewModel.productLowStockFilter) Color.Red else Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Low Stock",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (viewModel.productLowStockFilter) Color.Red else Color.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Category Row Pill selections
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { cat ->
                val isSelected = viewModel.productFilterCategory == cat
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) Color(0xFF007A48) else Color(0xFFE2E8F0))
                        .clickable { viewModel.productFilterCategory = cat }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = cat,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else Color.Black
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Inventory list
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Inventory, contentDescription = null, modifier = Modifier.size(60.dp), tint = Color.LightGray)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No products match selected filters.", fontSize = 13.sp, color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredList) { product ->
                    ProductRowCard(
                        product = product,
                        onEdit = { showEditDialogForProduct = product },
                        onDelete = { viewModel.deleteProduct(product) }
                    )
                }
            }
        }
    }

    // Add Product Modal Dialog
    if (showAddDialog) {
        AddProductDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, sku, barcode, cat, brand, unit, purchase, retail, wholesale, stock, alert, expiry ->
                viewModel.addProduct(
                    name = name,
                    sku = sku,
                    barcode = barcode,
                    category = cat,
                    brand = brand,
                    unit = unit,
                    purchasePrice = purchase,
                    retailPrice = retail,
                    wholesalePrice = wholesale,
                    stockQuantity = stock,
                    minStockAlert = alert,
                    expiryDate = expiry
                )
                showAddDialog = false
            }
        )
    }

    // Edit Product Modal Dialog
    showEditDialogForProduct?.let { productToEdit ->
        EditProductDialog(
            product = productToEdit,
            onDismiss = { showEditDialogForProduct = null },
            onSave = { updatedProduct ->
                viewModel.editProduct(updatedProduct)
                showEditDialogForProduct = null
            }
        )
    }
}

@Composable
fun ProductRowCard(product: Product, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(product.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("SKU: ${product.sku} • Barcode: ${product.barcode}", fontSize = 11.sp, color = Color.Gray)
                }
                
                // Expiry or Sync Badge
                Box(
                    modifier = Modifier
                        .background(Color(0xFFEAF9F1), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(product.category, fontSize = 10.sp, color = Color(0xFF007A48), fontWeight = FontWeight.Bold)
                }
            }

            Divider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFEDF2F7))

            // Pricing & Stock details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Cost Price", fontSize = 10.sp, color = Color.Gray)
                    Text("${product.purchasePrice} PKR", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("Retail Price", fontSize = 10.sp, color = Color.Gray)
                    Text("${product.retailPrice} PKR", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF007A48))
                }
                Column {
                    Text("Wholesale", fontSize = 10.sp, color = Color.Gray)
                    Text("${product.wholesalePrice} PKR", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Stock Level", fontSize = 10.sp, color = Color.Gray)
                    Text(
                        text = "${product.stockQuantity} ${product.unit}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = if (product.stockQuantity <= product.minStockAlert) Color.Red else Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Expiry: ${product.expiryDate}", fontSize = 11.sp, color = Color.Gray)
                
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Edit Trigger
                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp).testTag("edit_product_button")) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Product", tint = Color(0xFF007A48), modifier = Modifier.size(18.dp))
                    }
                    // Delete Trigger
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp).testTag("delete_product_button")) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Product", tint = Color.Red, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AddProductDialog(onDismiss: () -> Unit, onSave: (String, String, String, String, String, String, Double, Double, Double, Int, Int, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var sku by remember { mutableStateOf("") }
    var barcode by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Groceries") }
    var brand by remember { mutableStateOf("Zam Zam") }
    var unit by remember { mutableStateOf("Kg") }
    var purchasePrice by remember { mutableStateOf("") }
    var retailPrice by remember { mutableStateOf("") }
    var wholesalePrice by remember { mutableStateOf("") }
    var stockQty by remember { mutableStateOf("") }
    var alertLevel by remember { mutableStateOf("10") }
    var expiry by remember { mutableStateOf("") }

    val categories = listOf("Groceries", "Beverages", "Cooking Essentials", "Personal Care", "Snacks")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text("Add Inventory Product", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF007A48))
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Product Name *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("add_prod_name")
                    )
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = sku,
                            onValueChange = { sku = it },
                            label = { Text("SKU (Optional)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = barcode,
                            onValueChange = { barcode = it },
                            label = { Text("Barcode / Code") },
                            singleLine = true,
                            modifier = Modifier.weight(1.2f).testTag("add_prod_barcode")
                        )
                    }
                }

                item {
                    // Category selection dropdown mock
                    Text("Select Category:", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.take(3).forEach { cat ->
                            val isSel = category == cat
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) Color(0xFF007A48) else Color(0xFFEDF2F7))
                                    .clickable { category = cat }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(cat, fontSize = 10.sp, color = if (isSel) Color.White else Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = brand,
                            onValueChange = { brand = it },
                            label = { Text("Brand") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = unit,
                            onValueChange = { unit = it },
                            label = { Text("Unit (Kg, Pack)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = purchasePrice,
                            onValueChange = { purchasePrice = it },
                            label = { Text("Purchase Price") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("add_prod_purchase")
                        )
                        OutlinedTextField(
                            value = retailPrice,
                            onValueChange = { retailPrice = it },
                            label = { Text("Retail Price *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("add_prod_retail")
                        )
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = wholesalePrice,
                            onValueChange = { wholesalePrice = it },
                            label = { Text("Wholesale Price") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = stockQty,
                            onValueChange = { stockQty = it },
                            label = { Text("Stock Qty *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("add_prod_stock")
                        )
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = alertLevel,
                            onValueChange = { alertLevel = it },
                            label = { Text("Min Stock Alert") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = expiry,
                            onValueChange = { expiry = it },
                            placeholder = { Text("YYYY-MM-DD") },
                            label = { Text("Expiry Date") },
                            singleLine = true,
                            modifier = Modifier.weight(1.2f)
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                if (name.isNotBlank() && retailPrice.isNotBlank() && stockQty.isNotBlank()) {
                                    onSave(
                                        name,
                                        sku,
                                        barcode,
                                        category,
                                        brand,
                                        unit,
                                        purchasePrice.toDoubleOrNull() ?: 0.0,
                                        retailPrice.toDoubleOrNull() ?: 0.0,
                                        wholesalePrice.toDoubleOrNull() ?: 0.0,
                                        stockQty.toIntOrNull() ?: 0,
                                        alertLevel.toIntOrNull() ?: 5,
                                        expiry
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007A48)),
                            modifier = Modifier.weight(1.2f).testTag("save_product_btn")
                        ) {
                            Text("Save Product")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EditProductDialog(
    product: Product,
    onDismiss: () -> Unit,
    onSave: (Product) -> Unit
) {
    var name by remember { mutableStateOf(product.name) }
    var sku by remember { mutableStateOf(product.sku) }
    var barcode by remember { mutableStateOf(product.barcode) }
    var category by remember { mutableStateOf(product.category) }
    var brand by remember { mutableStateOf(product.brand) }
    var unit by remember { mutableStateOf(product.unit) }
    var purchasePrice by remember { mutableStateOf(product.purchasePrice.toString()) }
    var retailPrice by remember { mutableStateOf(product.retailPrice.toString()) }
    var wholesalePrice by remember { mutableStateOf(product.wholesalePrice.toString()) }
    var stockQty by remember { mutableStateOf(product.stockQuantity.toString()) }
    var alertLevel by remember { mutableStateOf(product.minStockAlert.toString()) }
    var expiry by remember { mutableStateOf(product.expiryDate) }

    val categories = listOf("Groceries", "Beverages", "Cooking Essentials", "Personal Care", "Snacks")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text("Edit Inventory Product", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF007A48))
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Product Name *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("edit_prod_name")
                    )
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = sku,
                            onValueChange = { sku = it },
                            label = { Text("SKU *") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = barcode,
                            onValueChange = { barcode = it },
                            label = { Text("Barcode") },
                            singleLine = true,
                            modifier = Modifier.weight(1.2f)
                        )
                    }
                }

                item {
                    Text("Select Category:", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.take(3).forEach { cat ->
                            val isSel = category == cat
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) Color(0xFF007A48) else Color(0xFFEDF2F7))
                                    .clickable { category = cat }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(cat, fontSize = 10.sp, color = if (isSel) Color.White else Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = brand,
                            onValueChange = { brand = it },
                            label = { Text("Brand") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = unit,
                            onValueChange = { unit = it },
                            label = { Text("Unit (Kg, Pack)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = purchasePrice,
                            onValueChange = { purchasePrice = it },
                            label = { Text("Purchase Price") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = retailPrice,
                            onValueChange = { retailPrice = it },
                            label = { Text("Retail Price *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("edit_prod_retail")
                        )
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = wholesalePrice,
                            onValueChange = { wholesalePrice = it },
                            label = { Text("Wholesale Price") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = stockQty,
                            onValueChange = { stockQty = it },
                            label = { Text("Stock Qty *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("edit_prod_stock")
                        )
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = alertLevel,
                            onValueChange = { alertLevel = it },
                            label = { Text("Min Stock Alert") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = expiry,
                            onValueChange = { expiry = it },
                            placeholder = { Text("YYYY-MM-DD") },
                            label = { Text("Expiry Date") },
                            singleLine = true,
                            modifier = Modifier.weight(1.2f)
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                if (name.isNotBlank() && retailPrice.isNotBlank() && stockQty.isNotBlank()) {
                                    val updated = product.copy(
                                        name = name,
                                        sku = sku,
                                        barcode = barcode,
                                        category = category,
                                        brand = brand,
                                        unit = unit,
                                        purchasePrice = purchasePrice.toDoubleOrNull() ?: 0.0,
                                        retailPrice = retailPrice.toDoubleOrNull() ?: 0.0,
                                        wholesalePrice = wholesalePrice.toDoubleOrNull() ?: 0.0,
                                        stockQuantity = stockQty.toIntOrNull() ?: 0,
                                        minStockAlert = alertLevel.toIntOrNull() ?: 5,
                                        expiryDate = expiry,
                                        lastUpdated = System.currentTimeMillis()
                                    )
                                    onSave(updated)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007A48)),
                            modifier = Modifier.weight(1.2f).testTag("save_edited_product_btn")
                        ) {
                            Text("Save Changes")
                        }
                    }
                }
            }
        }
    }
}
