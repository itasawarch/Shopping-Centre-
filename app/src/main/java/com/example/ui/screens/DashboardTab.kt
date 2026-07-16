package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.POSViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardTab(viewModel: POSViewModel) {
    val productsList by viewModel.products.collectAsState()
    val customersList by viewModel.customers.collectAsState()
    val salesList by viewModel.sales.collectAsState()
    val expensesList by viewModel.expenses.collectAsState()

    // Calculations based on actual local Room DB data
    val todayMillis = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val todaySalesList = salesList.filter { it.timestamp >= todayMillis && it.status != "Returned" }
    val todaySalesAmount = todaySalesList.sumOf { it.totalAmount }
    
    // In a real database we calculate profit as retailPrice - purchasePrice for all sold items.
    val estimatedProfitFactor = 0.16 // average markup of basmati, oils is around 16%
    val todayProfitAmount = todaySalesAmount * estimatedProfitFactor

    val monthlySalesAmount = salesList.filter { it.status != "Returned" }.sumOf { it.totalAmount }
    val monthlyProfitAmount = monthlySalesAmount * estimatedProfitFactor

    val totalExpensesAmount = expensesList.sumOf { it.amount }

    // Low stock counts
    val lowStockCount = productsList.count { it.stockQuantity > 0 && it.stockQuantity <= it.minStockAlert }
    val outOfStockCount = productsList.count { it.stockQuantity == 0 }
    val criticalProducts = productsList.filter { it.stockQuantity <= it.minStockAlert }

    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val isWideScreen = isTablet || isLandscape

    if (isWideScreen) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF2F7F2))
                .testTag("dashboard_tab")
                .verticalScroll(rememberScrollState())
        ) {
            // 1. Sleek Interface Rounded Header (Full Bleed)
            WelcomeSyncHeader(
                viewModel = viewModel,
                todaySalesAmount = todaySalesAmount,
                todaySalesCount = todaySalesList.size
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Responsive columns (CSS Grid / Flexbox equivalent)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Left Column (60% width equivalent)
                Column(
                    modifier = Modifier.weight(1.1f),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text(
                        text = "Quick Command Center",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    ActionGrid(viewModel)

                    Spacer(modifier = Modifier.height(4.dp))

                    WeeklySalesTrendChart(salesList, viewModel.shopCurrency)
                }

                // Right Column (40% width equivalent)
                Column(
                    modifier = Modifier.weight(0.9f),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Business Metrics
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Business Metrics",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            MetricCard(
                                title = "Today's Sales",
                                value = "${viewModel.shopCurrency} ${String.format("%,.0f", todaySalesAmount)}",
                                icon = Icons.Default.TrendingUp,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.weight(1f)
                            )
                            MetricCard(
                                title = "Today's Profit",
                                value = "${viewModel.shopCurrency} ${String.format("%,.0f", todayProfitAmount)}",
                                icon = Icons.Default.Payments,
                                tint = Color(0xFF059669),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            MetricCard(
                                title = "Monthly Sales",
                                value = "${viewModel.shopCurrency} ${String.format("%,.0f", monthlySalesAmount)}",
                                icon = Icons.Default.AccountBalanceWallet,
                                tint = Color(0xFF3B82F6),
                                modifier = Modifier.weight(1f)
                            )
                            MetricCard(
                                title = "Monthly Profit",
                                value = "${viewModel.shopCurrency} ${String.format("%,.0f", monthlyProfitAmount)}",
                                icon = Icons.Default.Savings,
                                tint = Color(0xFF8B5CF6),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            MetricCard(
                                title = "Total Customers",
                                value = customersList.size.toString(),
                                icon = Icons.Default.People,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.weight(1f)
                            )
                            MetricCard(
                                title = "Total Expenses",
                                value = "${viewModel.shopCurrency} ${String.format("%,.0f", totalExpensesAmount)}",
                                icon = Icons.Default.MoneyOff,
                                tint = Color(0xFFFBBF24),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    if (criticalProducts.isNotEmpty()) {
                        InventoryAlertsCard(
                            criticalProducts = criticalProducts,
                            onOrderClick = { product ->
                                viewModel.refreshAlerts()
                            }
                        )
                    }

                    // Recent Transactions List
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Recent Transactions",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                                IconButton(
                                    onClick = { viewModel.currentTab = com.example.ui.MainTab.REPORTS },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.ArrowForward,
                                        contentDescription = "View all report sales",
                                        tint = Color(0xFF059669),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFEDF2F7))

                            if (salesList.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No transactions logged yet today.", color = Color.Gray, fontSize = 13.sp)
                                }
                            } else {
                                salesList.take(5).forEachIndexed { index, sale ->
                                    RecentSaleRow(index + 1, sale, viewModel.shopCurrency, viewModel)
                                    if (index < salesList.take(5).size - 1) {
                                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFF1F5F9))
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF2F7F2))
                .testTag("dashboard_tab"),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Sleek Interface Rounded Header (Full Bleed)
            item {
                WelcomeSyncHeader(
                    viewModel = viewModel,
                    todaySalesAmount = todaySalesAmount,
                    todaySalesCount = todaySalesList.size
                )
            }

            // 2. Action Grid with 4 beautiful colored buttons
            item {
                ActionGrid(viewModel)
            }

            // 3. Inventory Alerts Card matching HTML exactly
            if (criticalProducts.isNotEmpty()) {
                item {
                    InventoryAlertsCard(
                        criticalProducts = criticalProducts,
                        onOrderClick = { product ->
                            // Simulate Ordering
                            viewModel.refreshAlerts()
                        }
                    )
                }
            }

            // 4. Business Metrics Headers & Cards
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Business Metrics",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B) // Slate-800
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricCard(
                            title = "Today's Sales",
                            value = "${viewModel.shopCurrency} ${String.format("%,.0f", todaySalesAmount)}",
                            icon = Icons.Default.TrendingUp,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "Today's Profit",
                            value = "${viewModel.shopCurrency} ${String.format("%,.0f", todayProfitAmount)}",
                            icon = Icons.Default.Payments,
                            tint = Color(0xFF059669),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricCard(
                            title = "Monthly Sales",
                            value = "${viewModel.shopCurrency} ${String.format("%,.0f", monthlySalesAmount)}",
                            icon = Icons.Default.AccountBalanceWallet,
                            tint = Color(0xFF3B82F6),
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "Monthly Profit",
                            value = "${viewModel.shopCurrency} ${String.format("%,.0f", monthlyProfitAmount)}",
                            icon = Icons.Default.Savings,
                            tint = Color(0xFF8B5CF6),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricCard(
                            title = "Total Customers",
                            value = customersList.size.toString(),
                            icon = Icons.Default.People,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "Total Expenses",
                            value = "${viewModel.shopCurrency} ${String.format("%,.0f", totalExpensesAmount)}",
                            icon = Icons.Default.MoneyOff,
                            tint = Color(0xFFFBBF24),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 5. Custom Elegant Canvas Chart showing Weekly Sales Trend
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    WeeklySalesTrendChart(salesList, viewModel.shopCurrency)
                }
            }

            // 6. Recent Sales list styled like HTML
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Recent Transactions",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B) // Slate-800
                                )
                                IconButton(
                                    onClick = { viewModel.currentTab = com.example.ui.MainTab.REPORTS },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.ArrowForward,
                                        contentDescription = "View all report sales",
                                        tint = Color(0xFF059669),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFEDF2F7))

                            if (salesList.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No transactions logged yet today.", color = Color.Gray, fontSize = 13.sp)
                                }
                            } else {
                                salesList.take(5).forEachIndexed { index, sale ->
                                    RecentSaleRow(index + 1, sale, viewModel.shopCurrency, viewModel)
                                    if (index < salesList.take(5).size - 1) {
                                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFF1F5F9))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WelcomeSyncHeader(
    viewModel: POSViewModel,
    todaySalesAmount: Double,
    todaySalesCount: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp)),
        shape = RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF059669) // Emerald-600
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header Top Bar Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Business Center",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = viewModel.shopName,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Sync icon button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .clickable { viewModel.syncWithCloud() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (viewModel.syncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "Sync Cloud",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Avatar placeholder
                    val initials = (viewModel.loggedInUser?.displayName ?: "AZ")
                        .split(" ")
                        .mapNotNull { it.firstOrNull() }
                        .joinToString("")
                        .take(2)
                        .uppercase()

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .clickable { viewModel.performLogout() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Quick Stats Card (Glassmorphic look)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(24.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(24.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Today's Net Sales",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${viewModel.shopCurrency} ${String.format("%,.0f", todaySalesAmount)}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = (-0.5).sp
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Color(0x4D064E3B), RoundedCornerShape(100.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "+12.5% Today",
                                color = Color(0xFFA7F3D0),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "$todaySalesCount Invoices",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActionGrid(viewModel: POSViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ActionButton(
            icon = Icons.Default.Calculate,
            label = "POS",
            bgColor = Color(0xFFD1FAE5),
            iconColor = Color(0xFF047857),
            onClick = { viewModel.currentTab = com.example.ui.MainTab.POS },
            modifier = Modifier.weight(1f)
        )
        ActionButton(
            icon = Icons.Default.Inventory,
            label = "Stock",
            bgColor = Color(0xFFEFF6FF),
            iconColor = Color(0xFF2563EB),
            onClick = { viewModel.currentTab = com.example.ui.MainTab.PRODUCTS },
            modifier = Modifier.weight(1f)
        )
        ActionButton(
            icon = Icons.Default.People,
            label = "Clients",
            bgColor = Color(0xFFFEF3C7),
            iconColor = Color(0xFFD97706),
            onClick = { viewModel.currentTab = com.example.ui.MainTab.CUSTOMERS },
            modifier = Modifier.weight(1f)
        )
        ActionButton(
            icon = Icons.Default.InsertChart,
            label = "Reports",
            bgColor = Color(0xFFFFE4E6),
            iconColor = Color(0xFFE11D48),
            onClick = { viewModel.currentTab = com.example.ui.MainTab.REPORTS },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ActionButton(
    icon: ImageVector,
    label: String,
    bgColor: Color,
    iconColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(bgColor, RoundedCornerShape(16.dp))
                .shadow(elevation = 1.dp, shape = RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(26.dp)
            )
        }
        Text(
            text = label.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF475569) // Slate-600
        )
    }
}

@Composable
fun InventoryAlertsCard(
    criticalProducts: List<Product>,
    onOrderClick: (Product) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .border(1.dp, Color(0xFFD1FAE5), RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Inventory Alerts",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B) // Slate-800
                )
                Box(
                    modifier = Modifier
                        .background(Color(0xFFFEF2F2), RoundedCornerShape(100.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${criticalProducts.size} Critical",
                        color = Color(0xFFEF4444),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                criticalProducts.take(3).forEach { product ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            val emoji = if (product.category.contains("Oil", ignoreCase = true)) "📦" else "🧴"
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(emoji, fontSize = 18.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = product.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF334155), // Slate-700
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (product.stockQuantity == 0) "Out of Stock" else "Low Stock: ${product.stockQuantity} units left",
                                    fontSize = 11.sp,
                                    color = Color(0xFFEF4444),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Button(
                            onClick = { onOrderClick(product) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("ORDER", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF1E293B) // Slate-800
            )
        }
    }
}

@Composable
fun WeeklySalesTrendChart(sales: List<Sale>, currency: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Weekly Revenue Trend (PKR)",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B) // Slate-800
            )
            Spacer(modifier = Modifier.height(16.dp))

            val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            val points = listOf(45000f, 65000f, 32000f, 98000f, 120000f, 155000f, 85000f)
            val maxPoint = points.maxOrNull() ?: 1f

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .padding(horizontal = 8.dp)
            ) {
                val width = size.width
                val height = size.height
                val spacing = width / (points.size - 1)

                for (i in 1..3) {
                    val y = height * (i / 4f)
                    drawLine(
                        color = Color(0x11000000),
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1f
                    )
                }

                val path = Path()
                points.forEachIndexed { idx, point ->
                    val x = idx * spacing
                    val y = height - (point / maxPoint) * (height * 0.8f) - 10f
                    if (idx == 0) {
                        path.moveTo(x, y)
                    } else {
                        val prevX = (idx - 1) * spacing
                        val prevY = height - (points[idx - 1] / maxPoint) * (height * 0.8f) - 10f
                        path.cubicTo(
                            (prevX + x) / 2f, prevY,
                            (prevX + x) / 2f, y,
                            x, y
                        )
                    }
                }

                val fillPath = Path().apply {
                    addPath(path)
                    lineTo(width, height)
                    lineTo(0f, height)
                    close()
                }

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0x33059669), Color.Transparent)
                    )
                )

                drawPath(
                    path = path,
                    color = Color(0xFF059669),
                    style = Stroke(width = 5f)
                )

                points.forEachIndexed { idx, point ->
                    val x = idx * spacing
                    val y = height - (point / maxPoint) * (height * 0.8f) - 10f
                    drawCircle(
                        color = Color(0xFF10B981),
                        radius = 8f,
                        center = Offset(x, y)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 4f,
                        center = Offset(x, y)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                days.forEach { day ->
                    Text(text = day, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun RecentSaleRow(index: Int, sale: Sale, currency: String, viewModel: POSViewModel) {
    val dateStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(sale.timestamp))
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Detail Click */ }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(
                        if (sale.status == "Returned") Color(0xFFF1F5F9) else Color(0xFFECFDF5),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = String.format("#%02d", index),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (sale.status == "Returned") Color(0xFF64748B) else Color(0xFF059669)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = if (sale.status == "Returned") "Returned Sale - ${sale.customerName}" else "Cash Sale - ${sale.customerName}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF334155), // Slate-700
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$dateStr • Walk-in Client",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }

        Text(
            text = "${if (sale.status == "Returned") "-" else "+"}₨ ${String.format("%,.0f", sale.totalAmount)}",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (sale.status == "Returned") Color(0xFFEF4444) else Color(0xFF059669)
        )
    }
}

