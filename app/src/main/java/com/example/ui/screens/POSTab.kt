package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.Customer
import com.example.data.Product
import com.example.data.Sale
import com.example.data.SaleItem
import com.example.ui.POSViewModel
import com.example.ui.components.ReceiptPrinter
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun POSTab(viewModel: POSViewModel) {
    val productsList by viewModel.products.collectAsState()
    val customersList by viewModel.customers.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showReceiptDialog by remember { mutableStateOf(false) }
    var showCustomerPicker by remember { mutableStateOf(false) }

    // Filtered products list for faster POS picking
    val filteredProducts = productsList.filter { prod ->
        (prod.name.contains(searchQuery, ignoreCase = true) || prod.barcode == searchQuery) &&
        prod.stockQuantity > 0
    }

    if (viewModel.lastCheckoutReceipt != null) {
        showReceiptDialog = true
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .testTag("pos_tab"),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // LEFT COLUMN: Product Catalog Picker (Weight 3)
        Column(
            modifier = Modifier
                .weight(1.3f)
                .fillMaxHeight()
                .padding(start = 12.dp, top = 12.dp, bottom = 12.dp)
        ) {
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search product name or scan barcode...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF007A48)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = null)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF007A48),
                    unfocusedBorderColor = Color(0xFFCBD5E1)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pos_search_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Quick barcode scanner simulator
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEDF2F7)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = Color(0xFF007A48))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Barcode Scanner Trigger", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            // Pick a random product's barcode to simulate scan
                            val availableWithBarcode = productsList.filter { it.barcode.isNotEmpty() }
                            if (availableWithBarcode.isNotEmpty()) {
                                val randomProd = availableWithBarcode.random()
                                viewModel.addProductToCart(randomProd)
                                viewModel.refreshAlerts()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007A48)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("SIMULATE SCAN", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Products Grid
            if (filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Inbox, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.LightGray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No matching in-stock products found.", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(130.dp),
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredProducts) { product ->
                        POSProductCard(product, onSelect = {
                            viewModel.addProductToCart(product)
                        })
                    }
                }
            }
        }

        // RIGHT COLUMN: Billing Cart Summary (Weight 2)
        Card(
            modifier = Modifier
                .weight(1.1f)
                .fillMaxHeight()
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(0.dp)),
            shape = RoundedCornerShape(0.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Customer selector row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFEAF9F1), RoundedCornerShape(10.dp))
                        .clickable { showCustomerPicker = true }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF007A48))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = viewModel.selectedCustomer?.name ?: "Guest Customer",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F2D1F)
                            )
                            if (viewModel.selectedCustomer != null) {
                                Text(
                                    text = "Balance: ${viewModel.selectedCustomer?.balance} PKR",
                                    fontSize = 11.sp,
                                    color = Color(0xFF059669)
                                )
                            }
                        }
                    }
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF007A48))
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Cart list items
                Text("Shopping Cart (${viewModel.cart.size} items)", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (viewModel.cart.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Cart is empty", fontSize = 12.sp, color = Color.LightGray)
                            }
                        }
                    } else {
                        items(viewModel.cart) { (product, qty) ->
                            CartItemRow(product, qty, viewModel)
                        }
                    }
                }

                Divider(color = Color(0xFFCBD5E1), thickness = 1.dp)

                // POS Discount & Tax Custom Modifiers
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Discount text entry
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Disc (PKR):", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        var discountText by remember { mutableStateOf("") }
                        OutlinedTextField(
                            value = discountText,
                            onValueChange = {
                                discountText = it
                                viewModel.posDiscount = it.toDoubleOrNull() ?: 0.0
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF007A48)
                            ),
                            modifier = Modifier
                                .width(70.dp)
                                .height(40.dp)
                                .testTag("discount_input")
                        )
                    }

                    // Hold / Resume Sales row
                    Row {
                        IconButton(onClick = { viewModel.holdCurrentSale() }) {
                            Icon(Icons.Default.Pause, contentDescription = "Hold Sale", tint = Color(0xFF8B5CF6))
                        }
                        if (viewModel.holdSales.isNotEmpty()) {
                            Badge(containerColor = Color(0xFF8B5CF6)) {
                                Text(
                                    text = viewModel.holdSales.size.toString(),
                                    color = Color.White,
                                    modifier = Modifier.clickable {
                                        // Resume the first held sale
                                        val firstLabel = viewModel.holdSales.keys.first()
                                        viewModel.resumeHeldSale(firstLabel)
                                    }
                                )
                            }
                        }
                    }
                }

                // Billing calculations
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Subtotal", fontSize = 12.sp, color = Color.Gray)
                        Text("${viewModel.shopCurrency} ${String.format("%,.0f", viewModel.getCartSubtotal())}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Discount", fontSize = 12.sp, color = Color.Gray)
                        Text("- ${viewModel.shopCurrency} ${String.format("%,.0f", viewModel.posDiscount)}", fontSize = 12.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Tax (${viewModel.posTaxRate}%)", fontSize = 12.sp, color = Color.Gray)
                        Text("+ ${viewModel.shopCurrency} ${String.format("%,.0f", viewModel.getCartTax())}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Divider(color = Color(0x11000000), modifier = Modifier.padding(vertical = 4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Payable Amount", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F2D1F))
                        Text(
                            text = "${viewModel.shopCurrency} ${String.format("%,.0f", viewModel.getCartTotal())}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF007A48)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Payment selector
                Text("Payment Method:", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val methods = listOf("Cash", "Card", "EasyPaisa", "On Credit")
                    methods.forEach { m ->
                        val isSel = viewModel.posPaymentMethod == m
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) Color(0xFF007A48) else Color(0xFFEDF2F7))
                                .clickable { viewModel.posPaymentMethod = m }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = m,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.White else Color.Black
                            )
                        }
                    }
                }

                // CHECKOUT PROCESS
                Button(
                    onClick = { viewModel.checkout() },
                    enabled = viewModel.cart.isNotEmpty() && !viewModel.isCheckingOut,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007A48)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("checkout_button")
                ) {
                    if (viewModel.isCheckingOut) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("COMPLETE CHECKOUT", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Customer Picker Dialog
    if (showCustomerPicker) {
        Dialog(onDismissRequest = { showCustomerPicker = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight(0.7f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Select Customer for Order", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.selectedCustomer = null
                                        showCustomerPicker = false
                                    },
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
                            ) {
                                Text("Guest Customer (Default)", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                        items(customersList) { cust ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.selectedCustomer = cust
                                        showCustomerPicker = false
                                    },
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(cust.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(cust.phone, fontSize = 11.sp, color = Color.Gray)
                                    }
                                    Text("${cust.balance} PKR", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF007A48))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Receipt Invoice Dialog
    if (showReceiptDialog) {
        viewModel.lastCheckoutReceipt?.let { (sale, items) ->
            Dialog(onDismissRequest = {
                viewModel.lastCheckoutReceipt = null
                showReceiptDialog = false
            }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Haji Zam Zam Header
                        Text(
                            text = viewModel.shopName.uppercase(),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center,
                            color = Color.Black
                        )
                        Text(
                            text = viewModel.shopAddress,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center,
                            color = Color.Black
                        )
                        Text(
                            text = "Phone: ${viewModel.shopPhone}",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center,
                            color = Color.Black
                        )

                        Text(
                            text = "------------------------------------------",
                            fontFamily = FontFamily.Monospace,
                            color = Color.Black
                        )

                        // Invoice Details
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text("Invoice: ${sale.id.take(8).uppercase()}", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = Color.Black)
                            Text("Date: ${SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault()).format(Date(sale.timestamp))}", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = Color.Black)
                            Text("Cashier: ${sale.cashierName}", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = Color.Black)
                            Text("Customer: ${sale.customerName}", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = Color.Black)
                        }

                        Text(
                            text = "------------------------------------------",
                            fontFamily = FontFamily.Monospace,
                            color = Color.Black
                        )

                        // Table header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("ITEM", modifier = Modifier.weight(1.5f), fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = Color.Black)
                            Text("QTY", modifier = Modifier.weight(0.5f), fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = Color.Black, textAlign = TextAlign.Center)
                            Text("PRICE", modifier = Modifier.weight(0.8f), fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = Color.Black, textAlign = TextAlign.End)
                        }

                        Text(
                            text = "------------------------------------------",
                            fontFamily = FontFamily.Monospace,
                            color = Color.Black
                        )

                        // Line items
                        items.forEach { line ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(line.productName.take(18), modifier = Modifier.weight(1.5f), fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = Color.Black)
                                Text(line.quantity.toString(), modifier = Modifier.weight(0.5f), fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = Color.Black, textAlign = TextAlign.Center)
                                Text(String.format("%,.0f", line.totalLinePrice), modifier = Modifier.weight(0.8f), fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = Color.Black, textAlign = TextAlign.End)
                            }
                        }

                        Text(
                            text = "------------------------------------------",
                            fontFamily = FontFamily.Monospace,
                            color = Color.Black
                        )

                        // Financial totals
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("SUBTOTAL:", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = Color.Black)
                                Text(String.format("%,.0f", sale.subtotal), fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = Color.Black)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("DISCOUNT:", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = Color.Black)
                                Text(String.format("%,.0f", sale.discount), fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = Color.Black)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("TAX (${viewModel.posTaxRate}%):", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = Color.Black)
                                Text(String.format("%,.0f", sale.tax), fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = Color.Black)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("TOTAL PAID:", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = Color.Black)
                                Text(String.format("%,.0f %s", sale.totalAmount, viewModel.shopCurrency), fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                        }

                        Text(
                            text = "------------------------------------------",
                            fontFamily = FontFamily.Monospace,
                            color = Color.Black
                        )

                        Text(
                            text = "THANK YOU FOR SHOPPING!",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = Color.Black
                        )
                        Text(
                            text = "No Return or Exchange without Receipt.",
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Sharing & Action triggers
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.lastCheckoutReceipt = null
                                    showReceiptDialog = false
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Dismiss", fontSize = 12.sp)
                            }

                            val context = LocalContext.current
                            Button(
                                onClick = {
                                    ReceiptPrinter.printReceipt(
                                        context = context,
                                        sale = sale,
                                        items = items,
                                        viewModel = viewModel
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007A48)),
                                modifier = Modifier.weight(1.2f).testTag("print_pdf_button")
                            ) {
                                Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Print PDF", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun POSProductCard(product: Product, onSelect: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Category tag
            Box(
                modifier = Modifier
                    .background(Color(0xFFEAF9F1), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(product.category, fontSize = 8.sp, color = Color(0xFF007A48), fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = product.name,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${product.retailPrice} PKR",
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    color = Color(0xFF007A48)
                )
                Text(
                    text = "Stock: ${product.stockQuantity}",
                    fontSize = 10.sp,
                    color = if (product.stockQuantity <= product.minStockAlert) Color.Red else Color.Gray,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun CartItemRow(product: Product, quantity: Int, viewModel: POSViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1.2f)) {
            Text(product.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("Price: ${product.retailPrice} PKR", fontSize = 10.sp, color = Color.Gray)
        }

        // Adjusters
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1.1f),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(
                onClick = { viewModel.decreaseQuantityInCart(product); viewModel.refreshAlerts() },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
            }
            Text(
                text = quantity.toString(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            IconButton(
                onClick = { viewModel.addProductToCart(product); viewModel.refreshAlerts() },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF007A48))
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = { viewModel.removeProductFromCart(product); viewModel.refreshAlerts() },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Red)
            }
        }
    }
}
