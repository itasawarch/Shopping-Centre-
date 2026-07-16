package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Sale
import com.example.ui.POSViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReportsTab(viewModel: POSViewModel) {
    val salesList by viewModel.sales.collectAsState()
    val expensesList by viewModel.expenses.collectAsState()
    val productsList by viewModel.products.collectAsState()

    var activePeriod by remember { mutableStateOf("Monthly") } // Daily, Weekly, Monthly
    var isGeneratingPDF by remember { mutableStateOf(false) }
    var isBackingUp by remember { mutableStateOf(false) }

    // Filter sales by status & period
    val completedSales = salesList.filter { it.status == "Completed" }
    
    val totalRevenue = completedSales.sumOf { it.totalAmount }
    val estimatedCostOfGoods = completedSales.sumOf { it.subtotal * 0.83 } // estimated purchase price
    val totalExpenses = expensesList.sumOf { it.amount }
    
    val grossProfit = totalRevenue - estimatedCostOfGoods
    val netProfit = grossProfit - totalExpenses

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("reports_tab")
            .padding(16.dp)
    ) {
        // Switch Header Periods
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
                listOf("Daily", "Weekly", "Monthly").forEach { period ->
                    val isSel = activePeriod == period
                    Box(
                        modifier = Modifier
                            .background(
                                if (isSel) Color(0xFF007A48) else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { activePeriod = period }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = period,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSel) Color.White else Color.Black
                        )
                    }
                }
            }

            // Export Actions row
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        isGeneratingPDF = true
                        // simulate pdf delay
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE2E8F0)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Black)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export PDF", fontSize = 10.sp, color = Color.Black)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Simulated Generating PDF Progress
        if (isGeneratingPDF) {
            LaunchedEffect(key1 = true) {
                kotlinx.coroutines.delay(1500)
                isGeneratingPDF = false
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .border(1.dp, Color(0xFF3B82F6), RoundedCornerShape(10.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Generating enterprise audit report PDF. Please wait...", fontSize = 12.sp, color = Color(0xFF1E40AF))
                }
            }
        }

        // P&L Statement Grid
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Profit & Loss (P&L) Statement",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF0F2D1F)
                )
                Text(text = "Aggregated calculations from active SQLite instance", fontSize = 11.sp, color = Color.Gray)

                Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFEDF2F7))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PLRow("Total Revenue / Sales (+)", "${String.format("%,.0f", totalRevenue)} PKR", Color(0xFF10B981))
                    PLRow("Cost of Goods Sold (COGS) (-)", "${String.format("%,.0f", estimatedCostOfGoods)} PKR", Color.Gray)
                    Divider(color = Color(0xFFF1F5F9))
                    PLRow("Gross Profit Margin", "${String.format("%,.0f", grossProfit)} PKR", Color(0xFF007A48))
                    PLRow("Log Business Expenses (-)", "${String.format("%,.0f", totalExpenses)} PKR", Color(0xFFEF4444))
                    Divider(color = Color(0xFFE2E8F0), thickness = 1.dp)
                    PLRow(
                        label = "Net Business Profit",
                        value = "${String.format("%,.0f", netProfit)} PKR",
                        color = if (netProfit >= 0) Color(0xFF007A48) else Color(0xFFEF4444),
                        isBold = true
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Local & Cloud Backup Card section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Backup, contentDescription = null, tint = Color(0xFF007A48))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("System State Database Backup", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Save SQLite state binary locally & cloud sync", fontSize = 11.sp, color = Color.Gray)
                    }
                }

                Button(
                    onClick = { isBackingUp = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007A48)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("backup_button")
                ) {
                    if (isBackingUp) {
                        LaunchedEffect(key1 = true) {
                            kotlinx.coroutines.delay(1200)
                            isBackingUp = false
                        }
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                    } else {
                        Text("Backup Now", fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sales Log Header
        Text("Transaction Invoice Log (${completedSales.size} completed)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(8.dp))

        // History list with Return Invoice trigger
        if (completedSales.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No transaction history listed yet.", color = Color.Gray, fontSize = 12.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(completedSales) { sale ->
                    ReportSaleRow(sale, viewModel)
                }
            }
        }
    }
}

@Composable
fun PLRow(label: String, value: String, color: Color, isBold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = if (isBold) 14.sp else 12.sp,
            fontWeight = if (isBold) FontWeight.Black else FontWeight.Medium,
            color = if (isBold) Color(0xFF0F2D1F) else Color.DarkGray
        )
        Text(
            text = value,
            fontSize = if (isBold) 14.sp else 12.sp,
            fontWeight = if (isBold) FontWeight.Black else FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun ReportSaleRow(sale: Sale, viewModel: POSViewModel) {
    val dateStr = SimpleDateFormat("dd-MMM hh:mm a", Locale.getDefault()).format(Date(sale.timestamp))

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
                Text(sale.customerName, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("Cashier: ${sale.cashierName} • Method: ${sale.paymentMethod}", fontSize = 11.sp, color = Color.Gray)
                Text(dateStr, fontSize = 10.sp, color = Color.LightGray)
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.weight(1.2f)
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text("${sale.totalAmount} PKR", fontWeight = FontWeight.Black, color = Color(0xFF007A48), fontSize = 13.sp)
                    // Return Item trigger
                    Text(
                        text = "Refund Return",
                        color = Color.Red,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { viewModel.performInvoiceReturn(sale.id) }
                            .padding(top = 2.dp)
                    )
                }
            }
        }
    }
}
