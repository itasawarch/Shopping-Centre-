package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.*
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: POSViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = viewModel.isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (viewModel.currentScreen) {
                        Screen.LOGIN, Screen.SIGNUP -> {
                            AuthScreen(viewModel)
                        }
                        Screen.MAIN -> {
                            MainTerminalLayout(viewModel)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTerminalLayout(viewModel: POSViewModel) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val isTablet = configuration.screenWidthDp >= 600

    var showNotificationsDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            if (viewModel.currentTab != com.example.ui.MainTab.DASHBOARD) {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF10B981)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Storefront, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                            Column {
                                Text(
                                    text = viewModel.shopName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "POS Terminal Active • PKR Currency",
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    },
                    actions = {
                        // System online / offline sync status indicator
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFFECFDF5))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(0xFF10B981), CircleShape)
                            )
                            Text("Cloud Synced", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF047857))
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Notifications bell trigger showing low-stock warnings
                        Box {
                            IconButton(onClick = { showNotificationsDialog = true }, modifier = Modifier.testTag("bell_icon")) {
                                Icon(Icons.Default.Notifications, contentDescription = "Alerts")
                            }
                            if (viewModel.notifications.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(Color.Red, CircleShape)
                                        .align(Alignment.TopEnd)
                                        .offset(x = (-6).dp, y = (6).dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Logout Button
                        IconButton(onClick = { viewModel.performLogout() }, modifier = Modifier.testTag("logout_button")) {
                            Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = Color.Red)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.border(0.dp, Color(0xFFE2E8F0)).testTag("top_app_bar")
                )
            }
        },
        bottomBar = {
            // Only draw standard bottom navigation bar if in portrait/mobile view
            if (!isLandscape && !isTablet) {
                BottomNavigationBar(viewModel)
            }
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Draw left-hand Navigation Rail/Sidebar if wide screen or landscape
            if (isLandscape || isTablet) {
                NavigationSidebar(viewModel)
            }

            // Central tab-based screen rendering frame (With Firebase Protected Route Check)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                val hasAccess = when (viewModel.currentTab) {
                    MainTab.REPORTS, MainTab.SETTINGS -> {
                        viewModel.firebaseAuthService.isAuthorized(viewModel.loggedInUser, "ADMIN")
                    }
                    else -> true
                }

                if (hasAccess) {
                    when (viewModel.currentTab) {
                        MainTab.DASHBOARD -> DashboardTab(viewModel)
                        MainTab.POS -> POSTab(viewModel)
                        MainTab.PRODUCTS -> ProductsTab(viewModel)
                        MainTab.CUSTOMERS -> CustomersSuppliersTab(viewModel)
                        MainTab.EXPENSES -> ExpensesTab(viewModel)
                        MainTab.REPORTS -> ReportsTab(viewModel)
                        MainTab.SETTINGS -> SettingsTab(viewModel)
                        else -> DashboardTab(viewModel)
                    }
                } else {
                    SecurityAccessDeniedScreen(viewModel)
                }
            }
        }
    }

    // Low Stock / Expiry Alerts Modal Dialog
    if (showNotificationsDialog) {
        Dialog(onDismissRequest = { showNotificationsDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.7f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = Color(0xFF059669))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("System Notifications & Stock Alerts", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        IconButton(onClick = { showNotificationsDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    if (viewModel.notifications.isEmpty()) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("All inventory levels are fully optimal. No current warnings.", color = Color.Gray, fontSize = 12.sp)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(viewModel.notifications) { note ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (note.contains("🚨") || note.contains("OUT")) Color(0xFFFEF2F2) else Color(0xFFFFFBEB)
                                    )
                                ) {
                                    Text(
                                        text = note,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(12.dp),
                                        fontWeight = FontWeight.Medium
                                    )
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
fun NavigationSidebar(viewModel: POSViewModel) {
    Box(
        modifier = Modifier
            .width(240.dp)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surface)
            .border(width = (0.5).dp, color = Color(0xFFE2E8F0), shape = RoundedCornerShape(0.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Elegant Sidebar Brand Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF059669)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Storefront,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "ZamZam ERP",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        text = "System Console",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Divider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(bottom = 8.dp))

            Text(
                text = "TERMINAL NAVIGATION",
                fontSize = 10.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 12.dp, bottom = 8.dp)
            )

            val menuItems = listOf(
                SidebarMenuItem("Dashboard", Icons.Default.Dashboard, MainTab.DASHBOARD),
                SidebarMenuItem("POS Billing", Icons.Default.ShoppingCart, MainTab.POS),
                SidebarMenuItem("Inventory", Icons.Default.Inventory, MainTab.PRODUCTS),
                SidebarMenuItem("Ledger Partners", Icons.Default.People, MainTab.CUSTOMERS),
                SidebarMenuItem("Expenses", Icons.Default.MoneyOff, MainTab.EXPENSES),
                SidebarMenuItem("Audit & Reports", Icons.Default.Analytics, MainTab.REPORTS),
                SidebarMenuItem("Settings", Icons.Default.Settings, MainTab.SETTINGS)
            )

            menuItems.forEach { item ->
                val isSelected = viewModel.currentTab == item.tab
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) Color(0xFF059669) else Color.Transparent)
                        .clickable { viewModel.currentTab = item.tab }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = if (isSelected) Color.White else Color.DarkGray,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = item.title,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else Color.DarkGray
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Footer user info
            Divider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFF10B981), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = viewModel.loggedInUser?.displayName?.take(1) ?: "U",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(viewModel.loggedInUser?.displayName ?: "User", fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(viewModel.loggedInUser?.role ?: "Cashier", fontSize = 10.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(viewModel: POSViewModel) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        val items = listOf(
            BottomMenuItem("Dashboard", Icons.Default.Dashboard, MainTab.DASHBOARD),
            BottomMenuItem("POS", Icons.Default.ShoppingCart, MainTab.POS),
            BottomMenuItem("Stock", Icons.Default.Inventory, MainTab.PRODUCTS),
            BottomMenuItem("Ledgers", Icons.Default.People, MainTab.CUSTOMERS),
            BottomMenuItem("Reports", Icons.Default.Analytics, MainTab.REPORTS)
        )

        items.forEach { item ->
            val isSelected = viewModel.currentTab == item.tab
            NavigationBarItem(
                selected = isSelected,
                onClick = { viewModel.currentTab = item.tab },
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title, fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = Color(0xFF059669),
                    indicatorColor = Color(0xFF059669),
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray
                )
            )
        }
    }
}

data class SidebarMenuItem(val title: String, val icon: ImageVector, val tab: MainTab)
data class BottomMenuItem(val title: String, val icon: ImageVector, val tab: MainTab)

@Composable
fun SecurityAccessDeniedScreen(viewModel: POSViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
                .testTag("security_access_denied_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // High contrast security lock shield
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFFEE2E2)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = "Shield Guard",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Role-Based Access Protected",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Firebase Auth Security Enforcement",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF059669),
                    modifier = Modifier
                        .background(Color(0xFFE6F4EA), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "The console panel you selected (${viewModel.currentTab.name}) is protected by rule-based database authorization. Only users logged in with 'Admin' credentials are authorized to view reports, system logs, or change system settings.",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                Divider(color = Color(0xFFF1F5F9))

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        viewModel.performLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Logout & Switch Account", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        viewModel.currentTab = MainTab.DASHBOARD
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth(),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Text("Return to Safety Dashboard", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

