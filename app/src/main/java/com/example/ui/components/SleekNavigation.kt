package com.example.ui.components

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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.User
import com.example.ui.MainTab
import com.example.ui.POSViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Shared Design Tokens matching the variables in theme.css
 */
object SleekNavigationTheme {
    val Primary = Color(0xFF059669)         // Emerald Green
    val PrimaryHover = Color(0xFF047857)
    val PrimaryLight = Color(0xFFE6F4EA)   // Soft Sage Green
    val Secondary = Color(0xFF10B981)      // Mint Accent
    val Success = Color(0xFF10B981)
    val Warning = Color(0xFFF59E0B)        // Warning / Low Stock
    val Danger = Color(0xFFEF4444)         // Critical Stock / Out of Stock
    
    val BgMain = Color(0xFFF8FAFC)         // Slate 50 (Very light cool gray-white)
    val BgSidebar = Color(0xFFFFFFFF)      // Pure White for sidebar
    val BorderLight = Color(0xFFE2E8F0)    // Slate 200 thin border
    
    val TextPrimary = Color(0xFF1E293B)    // Slate 800 (Dark Charcoal)
    val TextSecondary = Color(0xFF64748B)  // Slate 500 (Muted Gray)
    val TextLight = Color(0xFF94A3B8)      // Slate 400 (De-emphasized Gray)
    val TextOnPrimary = Color(0xFFFFFFFF)
}

data class SleekMenuItem(
    val title: String, 
    val icon: ImageVector, 
    val tab: MainTab, 
    val requiredRole: String
)

/**
 * Master menu items with clear role restrictions
 */
val SleekMenuItemsList = listOf(
    SleekMenuItem("Dashboard", Icons.Default.Dashboard, MainTab.DASHBOARD, "CASHIER"),
    SleekMenuItem("POS Billing", Icons.Default.ShoppingCart, MainTab.POS, "CASHIER"),
    SleekMenuItem("Inventory", Icons.Default.Inventory, MainTab.PRODUCTS, "MANAGER"),
    SleekMenuItem("Ledger Partners", Icons.Default.People, MainTab.CUSTOMERS, "CASHIER"),
    SleekMenuItem("Expenses", Icons.Default.MoneyOff, MainTab.EXPENSES, "MANAGER"),
    SleekMenuItem("Audit & Reports", Icons.Default.Analytics, MainTab.REPORTS, "MANAGER"),
    SleekMenuItem("Settings", Icons.Default.Settings, MainTab.SETTINGS, "ADMIN")
)

/**
 * Highly polished responsive Top Navbar matching Sleek Interface
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleekTopNavbar(
    viewModel: POSViewModel,
    onMenuClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val isWideScreen = isTablet || isLandscape

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(2.dp, shape = RoundedCornerShape(0.dp)),
        colors = CardDefaults.cardColors(containerColor = SleekNavigationTheme.BgSidebar),
        shape = RoundedCornerShape(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Menu Icon (if mobile) & Shop Identity
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!isWideScreen) {
                    IconButton(
                        onClick = onMenuClick, 
                        modifier = Modifier
                            .testTag("sleek_menu_toggle")
                            .size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Toggle Drawer",
                            tint = SleekNavigationTheme.Primary
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                } else {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SleekNavigationTheme.Primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }

                Column {
                    Text(
                        text = viewModel.shopName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = SleekNavigationTheme.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "System Console Active",
                        fontSize = 10.sp,
                        color = SleekNavigationTheme.Secondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Right: Time, Sync Chip, Notifications & Logout
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Time indicator
                val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
                Text(
                    text = timeStr,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekNavigationTheme.TextSecondary,
                    modifier = Modifier
                        .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )

                // Network Status Indicator Chip
                SleekNetworkStatusIndicator(viewModel = viewModel)

                // Sync indicator chip
                val isOnline = viewModel.isSystemOnline
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(if (isOnline) SleekNavigationTheme.PrimaryLight else Color(0xFFF1F5F9))
                        .clickable(enabled = isOnline) { viewModel.syncWithCloud() }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(if (isOnline) SleekNavigationTheme.Primary else Color(0xFF94A3B8), CircleShape)
                    )
                    Text(
                        text = "Cloud Sync",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isOnline) SleekNavigationTheme.Primary else Color(0xFF94A3B8)
                    )
                }

                // Alerts Bell Indicator
                Box {
                    IconButton(
                        onClick = onNotificationsClick,
                        modifier = Modifier
                            .testTag("sleek_bell_icon")
                            .size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "System Alerts",
                            tint = SleekNavigationTheme.TextSecondary
                        )
                    }
                    if (viewModel.notifications.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(SleekNavigationTheme.Danger, CircleShape)
                                .align(Alignment.TopEnd)
                                .offset(x = (-8).dp, y = (8).dp)
                        )
                    }
                }

                // Standard Logout Trigger
                IconButton(
                    onClick = { viewModel.performLogout() },
                    modifier = Modifier
                        .testTag("sleek_logout_button")
                        .size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Log out",
                        tint = SleekNavigationTheme.Danger
                    )
                }
            }
        }
    }
}

/**
 * Highly polished responsive Sidebar matching Sleek Interface
 * Supports role-based link visibility and custom highlights
 */
@Composable
fun SleekSidebar(
    viewModel: POSViewModel,
    onClose: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val loggedInUser = viewModel.loggedInUser
    val userRole = loggedInUser?.role ?: "Cashier"

    // Filter menu items dynamically according to the active role check
    val filteredMenuItems = SleekMenuItemsList.filter { item ->
        viewModel.firebaseAuthService.isAuthorized(loggedInUser, item.requiredRole)
    }

    Box(
        modifier = modifier
            .width(260.dp)
            .fillMaxHeight()
            .background(SleekNavigationTheme.BgSidebar)
            .border(width = 1.dp, color = SleekNavigationTheme.BorderLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // A. Brand Header (With Close Menu button if on mobile)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SleekNavigationTheme.Primary),
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
                            color = SleekNavigationTheme.TextPrimary
                        )
                        Text(
                            text = "Terminal v1.2",
                            fontSize = 10.sp,
                            color = SleekNavigationTheme.TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // If mobile onClose is supplied, render a nice close arrow/cross
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close navigation panel",
                        tint = SleekNavigationTheme.TextSecondary
                    )
                }
            }

            Divider(color = SleekNavigationTheme.BorderLight, modifier = Modifier.padding(bottom = 8.dp))

            // B. User Profile Section with custom role badges
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF8FAFC))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(SleekNavigationTheme.PrimaryLight, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = loggedInUser?.displayName?.take(1) ?: "U",
                        color = SleekNavigationTheme.Primary,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = loggedInUser?.displayName ?: "Operator",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekNavigationTheme.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    // Styled Role Badges
                    val (badgeBg, badgeText) = when (userRole.uppercase()) {
                        "ADMIN" -> Color(0xFFECFDF5) to Color(0xFF047857)      // Emerald Green
                        "MANAGER" -> Color(0xFFEFF6FF) to Color(0xFF1D4ED8)    // Royal Blue
                        else -> Color(0xFFF1F5F9) to Color(0xFF475569)         // Cool Gray
                    }
                    
                    Box(
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .background(badgeBg, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = userRole.uppercase(),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = badgeText,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "SYSTEM WORKSPACES",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = SleekNavigationTheme.TextLight,
                modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
            )

            // C. Dynamic Role-filtered Links
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(filteredMenuItems) { item ->
                    val isSelected = viewModel.currentTab == item.tab
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) SleekNavigationTheme.Primary else Color.Transparent)
                            .clickable {
                                viewModel.currentTab = item.tab
                                onClose()
                            }
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                            .testTag("sleek_sidebar_tab_${item.title.replace(" ", "_").lowercase()}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = if (isSelected) Color.White else SleekNavigationTheme.TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = item.title,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else SleekNavigationTheme.TextPrimary
                        )
                    }
                }
            }

            // D. System Environment Status Indicator
            Divider(color = SleekNavigationTheme.BorderLight)
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(SleekNavigationTheme.Success, CircleShape)
                    )
                    Text(
                        text = "POS Terminal Node",
                        fontSize = 10.sp,
                        color = SleekNavigationTheme.TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = "STANDALONE",
                    fontSize = 9.sp,
                    color = SleekNavigationTheme.TextLight,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Beautiful responsive Bottom Bar for mobile devices supporting role filter
 */
@Composable
fun SleekBottomBar(
    viewModel: POSViewModel,
    modifier: Modifier = Modifier
) {
    val loggedInUser = viewModel.loggedInUser
    
    // Filter bottom items based on roles
    val bottomMenuItems = listOf(
        SleekMenuItem("Dashboard", Icons.Default.Dashboard, MainTab.DASHBOARD, "CASHIER"),
        SleekMenuItem("POS", Icons.Default.ShoppingCart, MainTab.POS, "CASHIER"),
        SleekMenuItem("Stock", Icons.Default.Inventory, MainTab.PRODUCTS, "MANAGER"),
        SleekMenuItem("Ledgers", Icons.Default.People, MainTab.CUSTOMERS, "CASHIER"),
        SleekMenuItem("Reports", Icons.Default.Analytics, MainTab.REPORTS, "MANAGER")
    ).filter { item ->
        viewModel.firebaseAuthService.isAuthorized(loggedInUser, item.requiredRole)
    }

    NavigationBar(
        containerColor = SleekNavigationTheme.BgSidebar,
        modifier = modifier.windowInsetsPadding(WindowInsets.navigationBars),
        tonalElevation = 0.dp
    ) {
        bottomMenuItems.forEach { item ->
            val isSelected = viewModel.currentTab == item.tab
            NavigationBarItem(
                selected = isSelected,
                onClick = { viewModel.currentTab = item.tab },
                icon = { 
                    Icon(
                        imageVector = item.icon, 
                        contentDescription = item.title,
                        tint = if (isSelected) Color.White else SleekNavigationTheme.TextSecondary
                    ) 
                },
                label = { 
                    Text(
                        text = item.title, 
                        fontSize = 10.sp,
                        color = if (isSelected) SleekNavigationTheme.Primary else SleekNavigationTheme.TextSecondary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    ) 
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = SleekNavigationTheme.Primary,
                    indicatorColor = SleekNavigationTheme.Primary,
                    unselectedIconColor = SleekNavigationTheme.TextSecondary,
                    unselectedTextColor = SleekNavigationTheme.TextSecondary
                ),
                modifier = Modifier.testTag("sleek_bottom_tab_${item.title.replace(" ", "_").lowercase()}")
            )
        }
    }
}

/**
 * A beautiful, highly visible, real-time Network and Sync Status Indicator Component.
 * Displays Online/Offline with a pulsating dot, relevant icons, and supports manual override
 * clicking to easily simulate network environments and test local/cloud sync behaviors.
 */
@Composable
fun SleekNetworkStatusIndicator(
    viewModel: POSViewModel,
    modifier: Modifier = Modifier
) {
    val isOnline = viewModel.isSystemOnline
    val statusText = if (isOnline) "Online" else "Offline"
    val badgeBg = if (isOnline) Color(0xFFECFDF5) else Color(0xFFFEF2F2)
    val contentColor = if (isOnline) Color(0xFF047857) else Color(0xFFB91C1C)
    val dotColor = if (isOnline) Color(0xFF10B981) else Color(0xFFEF4444)

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(100.dp))
            .background(badgeBg)
            .clickable {
                viewModel.isManualOffline = !viewModel.isManualOffline
            }
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("network_status_indicator"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Live Status Dot
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(dotColor, CircleShape)
        )
        
        // Status Icon
        Icon(
            imageVector = Icons.Default.Cloud,
            contentDescription = "Sync state is $statusText",
            tint = contentColor,
            modifier = Modifier.size(14.dp)
        )

        // Status Text label
        Text(
            text = statusText,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
    }
}

