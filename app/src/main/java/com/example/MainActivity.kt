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
import com.example.ui.components.*
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
    val isWideScreen = isTablet || isLandscape

    var showNotificationsDialog by remember { mutableStateOf(false) }
    var isDrawerOpen by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                SleekTopNavbar(
                    viewModel = viewModel,
                    onMenuClick = { isDrawerOpen = !isDrawerOpen },
                    onNotificationsClick = { showNotificationsDialog = true }
                )
            },
            bottomBar = {
                // Only draw standard bottom navigation bar if in portrait/mobile view
                if (!isWideScreen) {
                    SleekBottomBar(viewModel)
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Draw left-hand Navigation Rail/Sidebar if wide screen or landscape
                if (isWideScreen) {
                    SleekSidebar(viewModel)
                }

                // Central tab-based screen rendering frame (With Firebase Protected Route Check)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    val hasAccess = when (viewModel.currentTab) {
                        MainTab.SETTINGS -> {
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
                            MainTab.SUPPLIERS -> TimelinesTab(viewModel)
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

        // Slide-out Drawer Sidebar on Mobile
        AnimatedVisibility(
            visible = !isWideScreen && isDrawerOpen,
            enter = fadeIn() + slideInHorizontally(initialOffsetX = { -it }),
            exit = fadeOut() + slideOutHorizontally(targetOffsetX = { -it })
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { isDrawerOpen = false }
            ) {
                val colors = getSchemeColors(viewModel.activeColorScheme, viewModel.isDarkMode)
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(260.dp)
                        .background(colors.surface)
                        .clickable(enabled = false) {} // prevent clicking behind
                        .align(Alignment.CenterStart)
                ) {
                    SleekSidebar(
                        viewModel = viewModel,
                        onClose = { isDrawerOpen = false }
                    )
                }
            }
        }
    }

    // Low Stock / Expiry Alerts Modal Dialog
    if (showNotificationsDialog) {
        val colors = getSchemeColors(viewModel.activeColorScheme, viewModel.isDarkMode)
        Dialog(onDismissRequest = { showNotificationsDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.7f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = colors.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("System Notifications & Alert Center", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                        }
                        IconButton(onClick = { showNotificationsDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = colors.textSecondary)
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = colors.border)

                    if (viewModel.notifications.isEmpty()) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("No current notifications.", color = colors.textSecondary, fontSize = 12.sp)
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
                                        containerColor = if (note.contains("🚨") || note.contains("OUT")) Color(0xFFFEF2F2) else colors.bgMain
                                    )
                                ) {
                                    Text(
                                        text = note,
                                        fontSize = 12.sp,
                                        color = colors.textPrimary,
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
fun SecurityAccessDeniedScreen(viewModel: POSViewModel) {
    val colors = getSchemeColors(viewModel.activeColorScheme, viewModel.isDarkMode)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgMain)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .border(1.dp, colors.border, RoundedCornerShape(20.dp))
                .testTag("security_access_denied_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
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
                        .background(Color.Red.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = "Shield Guard",
                        tint = Color.Red,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Role-Based Access Protected",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Firebase Auth Security Enforcement",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary,
                    modifier = Modifier
                        .background(colors.primary.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "The console panel you selected (${viewModel.currentTab.name}) is protected by rule-based database authorization. Only users logged in with 'Admin' credentials are authorized to view reports, system logs, or change system settings.",
                    fontSize = 12.sp,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                Divider(color = colors.border)

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        viewModel.performLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
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
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textPrimary),
                    modifier = Modifier.fillMaxWidth(),
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.border)
                ) {
                    Text("Return to Safety Dashboard", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

