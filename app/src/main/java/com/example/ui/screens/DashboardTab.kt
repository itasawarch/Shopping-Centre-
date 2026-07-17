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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.POSViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Sleek Design Tokens representing the theme.css variables
 */
object SleekTheme {
    val Primary = Color(0xFF059669)         // Emerald Green
    val PrimaryHover = Color(0xFF047857)
    val PrimaryLight = Color(0xFFE6F4EA)   // Soft Sage Green
    val Secondary = Color(0xFF10B981)      // Mint Accent
    val Success = Color(0xFF10B981)
    val Warning = Color(0xFFF59E0B)        // Low Stock / Warning
    val Danger = Color(0xFFEF4444)         // Out of Stock / Alert
    
    val BgMain = Color(0xFFF8FAFC)         // Slate 50 Cool Light White
    val BgSidebar = Color(0xFFFFFFFF)      // Pure White Sidebar
    val BgCard = Color(0xFFFFFFFF)         // Pure White Cards
    val BorderLight = Color(0xFFE2E8F0)    // Thin slate border
    val BorderGlass = Color(0x22059669)    // Delicate Emerald Border
    
    val TextPrimary = Color(0xFF1E293B)    // Slate 800 (Dark Charcoal)
    val TextSecondary = Color(0xFF64748B)  // Slate 500 (Muted Gray)
    val TextLight = Color(0xFF94A3B8)      // Slate 400 (De-emphasized)
    val TextOnPrimary = Color(0xFFFFFFFF)
    
    // Spacing (8px grid based)
    val SpacingXS = 4.dp
    val SpacingSM = 8.dp
    val SpacingMD = 16.dp
    val SpacingLG = 24.dp
    val SpacingXL = 32.dp
    
    // Radii
    val RadiusXS = 6.dp
    val RadiusSM = 10.dp
    val RadiusMD = 16.dp
    val RadiusLG = 24.dp
    val RadiusXL = 32.dp
}

@OptIn(ExperimentalMaterial3Api::class)
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
    
    val estimatedProfitFactor = 0.16 // average markup around 16%
    val todayProfitAmount = todaySalesAmount * estimatedProfitFactor

    val monthlySalesAmount = salesList.filter { it.status != "Returned" }.sumOf { it.totalAmount }
    val monthlyProfitAmount = monthlySalesAmount * estimatedProfitFactor

    val criticalProducts = productsList.filter { it.stockQuantity <= it.minStockAlert }

    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val isWideScreen = isTablet || isLandscape

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SleekTheme.BgMain)
            .testTag("dashboard_tab")
    ) {
        // B. Scrollable Workspace Content
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(SleekTheme.SpacingMD),
            verticalArrangement = Arrangement.spacedBy(SleekTheme.SpacingLG)
        ) {
                // 1. Sleek Welcome Header Sync Banner
                DashboardWelcomeBanner(
                    viewModel = viewModel,
                    todaySalesAmount = todaySalesAmount,
                    todaySalesCount = todaySalesList.size
                )

                // 2. Section Heading
                Text(
                    text = "Operational Analytics Grid",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekTheme.TextPrimary,
                    letterSpacing = 0.5.sp
                )

                // 3. Responsive Grid Layout housing placeholder/dynamic cards for Sales, Profit, and Inventory metrics
                if (isWideScreen) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(SleekTheme.SpacingMD)
                    ) {
                        SalesMetricCard(
                            todaySales = todaySalesAmount,
                            monthlySales = monthlySalesAmount,
                            currency = viewModel.shopCurrency,
                            modifier = Modifier.weight(1f)
                        )
                        ProfitAnalyticsCard(
                            todayProfit = todayProfitAmount,
                            monthlyProfit = monthlyProfitAmount,
                            currency = viewModel.shopCurrency,
                            modifier = Modifier.weight(1f)
                        )
                        InventoryStockCard(
                            products = productsList,
                            currency = viewModel.shopCurrency,
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(SleekTheme.SpacingMD)
                    ) {
                        SalesMetricCard(
                            todaySales = todaySalesAmount,
                            monthlySales = monthlySalesAmount,
                            currency = viewModel.shopCurrency
                        )
                        ProfitAnalyticsCard(
                            todayProfit = todayProfitAmount,
                            monthlyProfit = monthlyProfitAmount,
                            currency = viewModel.shopCurrency
                        )
                        InventoryStockCard(
                            products = productsList,
                            currency = viewModel.shopCurrency
                        )
                    }
                }

                // 4. Action Command Center Title
                Text(
                    text = "Command Center Actions",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekTheme.TextPrimary,
                    letterSpacing = 0.5.sp
                )

                // Action Grid with 4 beautiful colored buttons
                ActionGrid(viewModel)

                // 5. Interactive weekly trend chart & critical list
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(SleekTheme.SpacingLG)
                ) {
                    val chartWeight = if (isWideScreen) 1.2f else 1f
                    Box(modifier = Modifier.weight(chartWeight)) {
                        WeeklySalesTrendChart(salesList, viewModel.shopCurrency)
                    }
                    
                    if (isWideScreen && criticalProducts.isNotEmpty()) {
                        Box(modifier = Modifier.weight(0.8f)) {
                            InventoryAlertsCard(
                                criticalProducts = criticalProducts,
                                onOrderClick = { viewModel.refreshAlerts() }
                            )
                        }
                    }
                }
                
                if (!isWideScreen && criticalProducts.isNotEmpty()) {
                    InventoryAlertsCard(
                        criticalProducts = criticalProducts,
                        onOrderClick = { viewModel.refreshAlerts() }
                    )
                }

                // 6. Recent Sales list styled like HTML
                Text(
                    text = "Live Activity Ledger",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekTheme.TextPrimary,
                    letterSpacing = 0.5.sp
                )
                RecentTransactionsCard(salesList, viewModel)
            }
        }
    }



/**
 * Welcome and Synchronize Header Banner
 */
@Composable
fun DashboardWelcomeBanner(
    viewModel: POSViewModel,
    todaySalesAmount: Double,
    todaySalesCount: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, shape = RoundedCornerShape(SleekTheme.RadiusLG)),
        shape = RoundedCornerShape(SleekTheme.RadiusLG),
        colors = CardDefaults.cardColors(containerColor = SleekTheme.Primary)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SleekTheme.SpacingLG),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "WELCOME BACK,",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = viewModel.loggedInUser?.displayName ?: "Console Operator",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    text = "Today: $todaySalesCount sales completed • PKR standards apply",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
            
            // Cloud syncing indicator
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .clickable { viewModel.syncWithCloud() },
                contentAlignment = Alignment.Center
            ) {
                if (viewModel.syncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.CloudQueue,
                        contentDescription = "Sync Cloud",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

/**
 * 1. Sales Performance Metric Card with custom Canvas Sparkline trend graph
 */
@Composable
fun SalesMetricCard(
    todaySales: Double,
    monthlySales: Double,
    currency: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .border(1.dp, SleekTheme.BorderLight, RoundedCornerShape(SleekTheme.RadiusLG)),
        shape = RoundedCornerShape(SleekTheme.RadiusLG),
        colors = CardDefaults.cardColors(containerColor = SleekTheme.BgCard)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SALES PERFORMANCE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekTheme.TextSecondary,
                    letterSpacing = 1.sp
                )
                Box(
                    modifier = Modifier
                        .background(SleekTheme.PrimaryLight, RoundedCornerShape(100.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "+14.8%",
                        color = SleekTheme.Primary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column {
                Text(
                    text = "$currency ${String.format("%,.0f", todaySales)}",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = SleekTheme.TextPrimary
                )
                Text(
                    text = "Cumulative Monthly: $currency ${String.format("%,.0f", monthlySales)}",
                    fontSize = 11.sp,
                    color = SleekTheme.TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }

            // High Fidelity Sparkline Trend Graph representing sales activity flow
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .padding(vertical = 4.dp)
            ) {
                val sparkWidth = size.width
                val sparkHeight = size.height
                val sparkPoints = listOf(0.2f, 0.4f, 0.3f, 0.7f, 0.5f, 0.9f, 0.8f)
                val spacing = sparkWidth / (sparkPoints.size - 1)
                
                val path = Path()
                sparkPoints.forEachIndexed { idx, value ->
                    val x = idx * spacing
                    val y = sparkHeight - (value * sparkHeight * 0.8f) - 2f
                    if (idx == 0) {
                        path.moveTo(x, y)
                    } else {
                        val prevX = (idx - 1) * spacing
                        val prevY = sparkHeight - (sparkPoints[idx - 1] * sparkHeight * 0.8f) - 2f
                        path.cubicTo(
                            (prevX + x) / 2f, prevY,
                            (prevX + x) / 2f, y,
                            x, y
                        )
                    }
                }

                // Sparkline path outline
                drawPath(
                    path = path,
                    color = SleekTheme.Primary,
                    style = Stroke(width = 4f)
                )

                // Translucent gradient fill beneath sparkline
                val fillPath = Path().apply {
                    addPath(path)
                    lineTo(sparkWidth, sparkHeight)
                    lineTo(0f, sparkHeight)
                    close()
                }
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(SleekTheme.Primary.copy(alpha = 0.25f), Color.Transparent)
                    )
                )
            }
        }
    }
}

/**
 * 2. Profit Analytics Metric Card with gorgeous gradient background and target gauge Progress Arc
 */
@Composable
fun ProfitAnalyticsCard(
    todayProfit: Double,
    monthlyProfit: Double,
    currency: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .border(1.dp, SleekTheme.BorderLight, RoundedCornerShape(SleekTheme.RadiusLG)),
        shape = RoundedCornerShape(SleekTheme.RadiusLG),
        colors = CardDefaults.cardColors(containerColor = SleekTheme.BgCard)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PROFIT ANALYTICS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekTheme.TextSecondary,
                    letterSpacing = 1.sp
                )
                Box(
                    modifier = Modifier
                        .background(Color(0xFFEFF6FF), RoundedCornerShape(100.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "16.5% Net",
                        color = Color(0xFF2563EB),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1.2f)) {
                    Text(
                        text = "$currency ${String.format("%,.0f", todayProfit)}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = SleekTheme.TextPrimary
                    )
                    Text(
                        text = "Monthly net: $currency ${String.format("%,.0f", monthlyProfit)}",
                        fontSize = 11.sp,
                        color = SleekTheme.TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Interactive circular target gauge drawn via Canvas
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .weight(0.8f),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Track background circle
                        drawCircle(
                            color = Color(0xFFF1F5F9),
                            radius = size.width / 2f,
                            style = Stroke(width = 6f)
                        )
                        // Progress ring representing daily profit goal percentage
                        drawArc(
                            color = SleekTheme.Secondary,
                            startAngle = -90f,
                            sweepAngle = 265f, // represents ~74% goal completion
                            useCenter = false,
                            style = Stroke(width = 6f)
                        )
                    }
                    Text(
                        text = "74%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekTheme.TextPrimary
                    )
                }
            }
        }
    }
}

/**
 * 3. Inventory Stock Metric Card with warning and danger status indicators and real DB-driven values
 */
@Composable
fun InventoryStockCard(
    products: List<Product>,
    currency: String,
    modifier: Modifier = Modifier
) {
    val totalStockUnits = products.sumOf { it.stockQuantity }
    val lowStockCount = products.count { it.isLowStock() }
    val outOfStockCount = products.count { it.isOutOfStock() }
    val totalRetailValue = products.sumOf { it.getRetailStockValue() }

    Card(
        modifier = modifier
            .border(1.dp, SleekTheme.BorderLight, RoundedCornerShape(SleekTheme.RadiusLG)),
        shape = RoundedCornerShape(SleekTheme.RadiusLG),
        colors = CardDefaults.cardColors(containerColor = SleekTheme.BgCard)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "INVENTORY HEALTH",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekTheme.TextSecondary,
                    letterSpacing = 1.sp
                )
                
                // Status badges based on database conditions
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (outOfStockCount > 0) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFFEE2E2), RoundedCornerShape(100.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "🚨 $outOfStockCount Empty",
                                color = SleekTheme.Danger,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    if (lowStockCount > 0) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFFEF3C7), RoundedCornerShape(100.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "⚠️ $lowStockCount Low",
                                color = SleekTheme.Warning,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Column {
                Text(
                    text = "$totalStockUnits Units",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = SleekTheme.TextPrimary
                )
                Text(
                    text = "Retail Valuation: $currency ${String.format("%,.0f", totalRetailValue)}",
                    fontSize = 11.sp,
                    color = SleekTheme.TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }

            // Real-time Stock Safety Level Progress Bar
            val fillPercent = if (products.isEmpty()) 0.8f else {
                val healthyCount = products.count { !it.isLowStock() && !it.isOutOfStock() }.toFloat()
                healthyCount / products.size.toFloat()
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Stock Health Factor", fontSize = 10.sp, color = SleekTheme.TextSecondary, fontWeight = FontWeight.Medium)
                    Text("${(fillPercent * 100).toInt()}%", fontSize = 10.sp, color = SleekTheme.TextPrimary, fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF1F5F9))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fillPercent)
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .background(
                                when {
                                    fillPercent < 0.4f -> SleekTheme.Danger
                                    fillPercent < 0.75f -> SleekTheme.Warning
                                    else -> SleekTheme.Primary
                                }
                            )
                    )
                }
            }
        }
    }
}

/**
 * 4 beautiful, responsive grid action buttons
 */
@Composable
fun ActionGrid(viewModel: POSViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
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

/**
 * Inventory Alerts section displaying critical low-stock thresholds
 */
@Composable
fun InventoryAlertsCard(
    criticalProducts: List<Product>,
    onOrderClick: (Product) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFFEF2F2), RoundedCornerShape(24.dp)),
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
                    text = "Inventory Security Warnings",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Box(
                    modifier = Modifier
                        .background(Color(0xFFFEE2E2), RoundedCornerShape(100.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${criticalProducts.size} Alert",
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
                                    color = Color(0xFF334155),
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
                            colors = ButtonDefaults.buttonColors(containerColor = SleekTheme.Primary),
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

/**
 * Metric Card for display generic values
 */
@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.border(1.dp, SleekTheme.BorderLight, RoundedCornerShape(16.dp)),
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
                color = Color(0xFF1E293B)
            )
        }
    }
}

/**
 * Weekly trend Canvas Chart
 */
@Composable
fun WeeklySalesTrendChart(sales: List<Sale>, currency: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SleekTheme.BorderLight, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Weekly Revenue Trend ($currency)",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
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
                        colors = listOf(SleekTheme.Primary.copy(alpha = 0.2f), Color.Transparent)
                    )
                )

                drawPath(
                    path = path,
                    color = SleekTheme.Primary,
                    style = Stroke(width = 5f)
                )

                points.forEachIndexed { idx, point ->
                    val x = idx * spacing
                    val y = height - (point / maxPoint) * (height * 0.8f) - 10f
                    drawCircle(
                        color = SleekTheme.Secondary,
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

/**
 * Recent sale item row matching original specifications
 */
@Composable
fun RecentSaleRow(index: Int, sale: Sale, currency: String, viewModel: POSViewModel) {
    val dateStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(sale.timestamp))
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Detail View click */ }
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
                    color = if (sale.status == "Returned") Color(0xFF64748B) else SleekTheme.Primary
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = if (sale.status == "Returned") "Returned Sale - ${sale.customerName}" else "Cash Sale - ${sale.customerName}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF334155),
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
            color = if (sale.status == "Returned") Color(0xFFEF4444) else SleekTheme.Primary
        )
    }
}

/**
 * Container of Recent Transactions List
 */
@Composable
fun RecentTransactionsCard(
    salesList: List<Sale>,
    viewModel: POSViewModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SleekTheme.BorderLight, RoundedCornerShape(SleekTheme.RadiusLG)),
        shape = RoundedCornerShape(SleekTheme.RadiusLG),
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
                    text = "Recent Transactions Ledger",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                IconButton(
                    onClick = { viewModel.currentTab = com.example.ui.MainTab.REPORTS },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "View all report sales",
                        tint = SleekTheme.Primary,
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
                    Text("No transactions logged in current session.", color = Color.Gray, fontSize = 13.sp)
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
