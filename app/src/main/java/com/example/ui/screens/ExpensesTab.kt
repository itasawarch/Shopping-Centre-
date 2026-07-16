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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Expense
import com.example.ui.POSViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ExpensesTab(viewModel: POSViewModel) {
    val expensesList by viewModel.expenses.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    val totalExpense = expensesList.sumOf { it.amount }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("expenses_tab")
            .padding(16.dp)
    ) {
        // TOP Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total Business Expenses", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text("${totalExpense} PKR", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color(0xFF991B1B))
                }

                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("log_expense_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Log Expense", fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Expense ledger history", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        
        Spacer(modifier = Modifier.height(8.dp))

        if (expensesList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.MoneyOff, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.LightGray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No expenses logged. Tap Log Expense to begin.", color = Color.Gray, fontSize = 12.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(expensesList) { exp ->
                    ExpenseRowItem(expense = exp, onDelete = {
                        viewModel.deleteExpense(exp)
                    })
                }
            }
        }
    }

    if (showAddDialog) {
        AddExpenseDialog(
            onDismiss = { showAddDialog = false },
            onSave = { category, amount, description ->
                viewModel.addExpense(category, amount, description)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ExpenseRowItem(expense: Expense, onDelete: () -> Unit) {
    val dateStr = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(Date(expense.timestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1.5f)) {
                Text(expense.category, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(expense.description, fontSize = 11.sp, color = Color.Gray)
                Text(dateStr, fontSize = 10.sp, color = Color.LightGray)
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.weight(1f)
            ) {
                Text("${expense.amount} PKR", fontWeight = FontWeight.Black, color = Color(0xFFEF4444), fontSize = 13.sp)
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun AddExpenseDialog(onDismiss: () -> Unit, onSave: (String, Double, String) -> Unit) {
    var category by remember { mutableStateOf("Rent") }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val categories = listOf("Rent", "Electricity Bill", "Staff Salaries", "Water Bill", "Repairs", "Others")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Operational Expense", color = Color(0xFF991B1B)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                                .border(1.dp, if (isSel) Color(0xFFEF4444) else Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                                .background(if (isSel) Color(0xFFFEE2E2) else Color.White)
                                .clickable { category = cat }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(cat, fontSize = 10.sp, color = if (isSel) Color(0xFF991B1B) else Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Expense Amount (PKR) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_expense_amount")
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Short Details / Note *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_expense_desc")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amount.toDoubleOrNull() ?: 0.0
                    if (amt > 0 && description.isNotBlank()) {
                        onSave(category, amt, description)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                modifier = Modifier.testTag("save_expense_btn")
            ) {
                Text("Log Expense")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
