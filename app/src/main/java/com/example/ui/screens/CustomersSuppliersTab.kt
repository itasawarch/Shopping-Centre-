package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.Customer
import com.example.data.Supplier
import com.example.ui.POSViewModel

@Composable
fun CustomersSuppliersTab(viewModel: POSViewModel) {
    val customersList by viewModel.customers.collectAsState()
    val suppliersList by viewModel.suppliers.collectAsState()

    var activeSubTab by remember { mutableStateOf("Customers") } // "Customers" or "Suppliers"
    
    var showAddCustDialog by remember { mutableStateOf(false) }
    var showAddSuppDialog by remember { mutableStateOf(false) }
    var showLedgerAdjustDialog by remember { mutableStateOf<Customer?>(null) }

    val filteredCustomers = customersList.filter {
        it.name.contains(viewModel.customerSearchQuery, ignoreCase = true) ||
        it.phone.contains(viewModel.customerSearchQuery, ignoreCase = true)
    }

    val filteredSuppliers = suppliersList.filter {
        it.name.contains(viewModel.supplierSearchQuery, ignoreCase = true) ||
        it.phone.contains(viewModel.supplierSearchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("customers_suppliers_tab")
            .padding(16.dp)
    ) {
        // Switch Header Tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .background(Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                    .padding(4.dp)
            ) {
                listOf("Customers", "Suppliers").forEach { sub ->
                    val isSel = activeSubTab == sub
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) Color(0xFF007A48) else Color.Transparent)
                            .clickable { activeSubTab = sub }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = sub,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSel) Color.White else Color.Black
                        )
                    }
                }
            }

            // Quick registration button
            Button(
                onClick = {
                    if (activeSubTab == "Customers") showAddCustDialog = true
                    else showAddSuppDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007A48)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("add_partner_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Register $activeSubTab", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search panel
        OutlinedTextField(
            value = if (activeSubTab == "Customers") viewModel.customerSearchQuery else viewModel.supplierSearchQuery,
            onValueChange = {
                if (activeSubTab == "Customers") viewModel.customerSearchQuery = it
                else viewModel.supplierSearchQuery = it
            },
            placeholder = { Text("Search by name, phone or shop address...", fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF007A48)) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF007A48),
                unfocusedBorderColor = Color(0xFFCBD5E1)
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().testTag("partner_search_field")
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Main List Content
        if (activeSubTab == "Customers") {
            if (filteredCustomers.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No customer records found. Tap Register to add.", color = Color.Gray, fontSize = 13.sp)
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(filteredCustomers) { cust ->
                        CustomerCardRow(
                            customer = cust,
                            onAdjustLedger = { showLedgerAdjustDialog = cust },
                            onDelete = { viewModel.deleteCustomer(cust) }
                        )
                    }
                }
            }
        } else {
            if (filteredSuppliers.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No supplier records found. Tap Register to add.", color = Color.Gray, fontSize = 13.sp)
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(filteredSuppliers) { supp ->
                        SupplierCardRow(
                            supplier = supp,
                            onPaySupplier = { viewModel.updateSupplierBalance(supp.id, -10000.0) }, // pay 10,000 pkr simulation
                            onDelete = { viewModel.deleteSupplier(supp) }
                        )
                    }
                }
            }
        }
    }

    // Register Customer Dialog
    if (showAddCustDialog) {
        PartnerDialog(
            title = "Register New Customer",
            labelName = "Customer Name *",
            onDismiss = { showAddCustDialog = false },
            onSave = { name, phone, email, address ->
                viewModel.addCustomer(name, phone, email, address)
                showAddCustDialog = false
            }
        )
    }

    // Register Supplier Dialog
    if (showAddSuppDialog) {
        PartnerDialog(
            title = "Register New Supplier",
            labelName = "Supplier Business Name *",
            onDismiss = { showAddSuppDialog = false },
            onSave = { name, phone, email, address ->
                viewModel.addSupplier(name, phone, email, address)
                showAddSuppDialog = false
            }
        )
    }

    // Ledger Adjustment Dialog
    showLedgerAdjustDialog?.let { cust ->
        var amountText by remember { mutableStateOf("") }
        var isCredit by remember { mutableStateOf(true) } // Credit increases balance, Debit decreases it

        Dialog(onDismissRequest = { showLedgerAdjustDialog = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Adjust Customer Ledger", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF007A48))
                    Text("Customer: ${cust.name}", fontSize = 12.sp, color = Color.Gray)
                    Text("Current Outstanding: ${cust.balance} PKR", fontSize = 13.sp, fontWeight = FontWeight.Bold)

                    Divider(color = Color(0xFFEDF2F7))

                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Adjustment Amount (PKR)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("ledger_adjust_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { isCredit = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isCredit) Color(0xFF10B981) else Color(0xFFE2E8F0)
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("CREDIT (Pay)", color = if (isCredit) Color.White else Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { isCredit = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isCredit) Color(0xFFEF4444) else Color(0xFFE2E8F0)
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("DEBIT (Charge)", color = if (!isCredit) Color.White else Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(onClick = { showLedgerAdjustDialog = null }, modifier = Modifier.weight(1f)) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                val amt = amountText.toDoubleOrNull() ?: 0.0
                                if (amt > 0) {
                                    viewModel.updateCustomerBalance(cust.id, amt, isCredit)
                                }
                                showLedgerAdjustDialog = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007A48)),
                            modifier = Modifier.weight(1.2f).testTag("save_ledger_btn")
                        ) {
                            Text("Save Ledger")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerCardRow(customer: Customer, onAdjustLedger: () -> Unit, onDelete: () -> Unit) {
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
                    Text(customer.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Phone: ${customer.phone} • Address: ${customer.address}", fontSize = 11.sp, color = Color.Gray)
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text("Outstanding Balance", fontSize = 10.sp, color = Color.Gray)
                    Text(
                        text = "${customer.balance} PKR",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = if (customer.balance < 0) Color(0xFFEF4444) else Color(0xFF10B981)
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFEDF2F7))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Adjust Balance Button
                Button(
                    onClick = onAdjustLedger,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007A48)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text("Adjust Ledger Balance", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun SupplierCardRow(supplier: Supplier, onPaySupplier: () -> Unit, onDelete: () -> Unit) {
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
                    Text(supplier.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Phone: ${supplier.phone} • Address: ${supplier.address}", fontSize = 11.sp, color = Color.Gray)
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text("Payable Balance", fontSize = 10.sp, color = Color.Gray)
                    Text(
                        text = "${supplier.balance} PKR",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = if (supplier.balance > 0) Color(0xFFFBBF24) else Color(0xFF10B981)
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFEDF2F7))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Simulated Supplier Payment trigger
                Button(
                    onClick = onPaySupplier,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text("Simulate Supplier Payment (10k PKR)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun PartnerDialog(
    title: String,
    labelName: String,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(title, fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF007A48))
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(labelName) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("partner_input_name")
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Contact Phone *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("partner_input_phone")
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Shop Address / Area *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            if (name.isNotBlank() && phone.isNotBlank()) {
                                onSave(name, phone, email, address)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007A48)),
                        modifier = Modifier.weight(1.2f).testTag("partner_save_btn")
                    ) {
                        Text("Save Partner")
                    }
                }
            }
        }
    }
}
