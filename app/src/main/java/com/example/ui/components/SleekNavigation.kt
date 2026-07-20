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
import com.example.ui.AppColorScheme
import com.example.ui.MainTab
import com.example.ui.POSViewModel
import java.text.SimpleDateFormat
import java.util.*

// Dynamic Color Provider matching the custom theme settings
@Composable
fun getSchemeColors(scheme: AppColorScheme, isDark: Boolean): SchemeColors {
    return if (isDark) {
        when (scheme) {
            AppColorScheme.CLASSIC_BLUE -> SchemeColors(
                primary = Color(0xFF60A5FA), bgMain = Color(0xFF0F172A), surface = Color(0xFF1E293B), border = Color(0xFF334155),
                textPrimary = Color(0xFFF8FAFC), textSecondary = Color(0xFF94A3B8)
            )
            AppColorScheme.MIDNIGHT_NAVY -> SchemeColors(
                primary = Color(0xFF38BDF8), bgMain = Color(0xFF0B0F19), surface = Color(0xFF161E2E), border = Color(0xFF243049),
                textPrimary = Color(0xFFF1F5F9), textSecondary = Color(0xFF94A3B8)
            )
            AppColorScheme.ROYAL_SAPPHIRE -> SchemeColors(
                primary = Color(0xFF818CF8), bgMain = Color(0xFF090D1A), surface = Color(0xFF141A30), border = Color(0xFF25335C),
                textPrimary = Color(0xFFECEEFE), textSecondary = Color(0xFFA5B4FC)
            )
            AppColorScheme.EMERALD_MINT -> SchemeColors(
                primary = Color(0xFF34D399), bgMain = Color(0xFF061411), surface = Color(0xFF0F2620), border = Color(0xFF1A3E34),
                textPrimary = Color(0xFFECFDF5), textSecondary = Color(0xFF6EE7B7)
            )
            AppColorScheme.SLATE_MINIMALIST -> SchemeColors(
                primary = Color(0xFF94A3B8), bgMain = Color(0xFF020617), surface = Color(0xFF0F172A), border = Color(0xFF1E293B),
                textPrimary = Color(0xFFF1F5F9), textSecondary = Color(0xFF64748B)
            )
        }
    } else {
        when (scheme) {
            AppColorScheme.CLASSIC_BLUE -> SchemeColors(
                primary = Color(0xFF2563EB), bgMain = Color(0xFFF8FAFC), surface = Color(0xFFFFFFFF), border = Color(0xFFE2E8F0),
                textPrimary = Color(0xFF1E293B), textSecondary = Color(0xFF475569)
            )
            AppColorScheme.MIDNIGHT_NAVY -> SchemeColors(
                primary = Color(0xFF0284C7), bgMain = Color(0xFFF0F9FF), surface = Color(0xFFFFFFFF), border = Color(0xFFE0F2FE),
                textPrimary = Color(0xFF0F172A), textSecondary = Color(0xFF0284C7)
            )
            AppColorScheme.ROYAL_SAPPHIRE -> SchemeColors(
                primary = Color(0xFF4F46E5), bgMain = Color(0xFFEEF2FF), surface = Color(0xFFFFFFFF), border = Color(0xFFE0E7FF),
                textPrimary = Color(0xFF1E1B4B), textSecondary = Color(0xFF4F46E5)
            )
            AppColorScheme.EMERALD_MINT -> SchemeColors(
                primary = Color(0xFF059669), bgMain = Color(0xFFECFDF5), surface = Color(0xFFFFFFFF), border = Color(0xFFD1FAE5),
                textPrimary = Color(0xFF065F46), textSecondary = Color(0xFF059669)
            )
            AppColorScheme.SLATE_MINIMALIST -> SchemeColors(
                primary = Color(0xFF475569), bgMain = Color(0xFFF8FAFC), surface = Color(0xFFFFFFFF), border = Color(0xFFE2E8F0),
                textPrimary = Color(0xFF0F172A), textSecondary = Color(0xFF334155)
            )
        }
    }
}

data class SchemeColors(
    val primary: Color,
    val bgMain: Color,
    val surface: Color,
    val border: Color,
    val textPrimary: Color,
    val textSecondary: Color
)

data class SleekMenuItem(
    val title: String, 
    val icon: ImageVector, 
    val tab: MainTab, 
    val requiredRole: String
)

val SleekMenuItemsList = listOf(
    SleekMenuItem("Home", Icons.Default.Home, MainTab.DASHBOARD, "GUEST"),
    SleekMenuItem("Portfolio", Icons.Default.Work, MainTab.POS, "GUEST"),
    SleekMenuItem("Services", Icons.Default.Layers, MainTab.PRODUCTS, "GUEST"),
    SleekMenuItem("Skills", Icons.Default.Grade, MainTab.CUSTOMERS, "GUEST"),
    SleekMenuItem("Timelines", Icons.Default.Timeline, MainTab.SUPPLIERS, "GUEST"),
    SleekMenuItem("Blog", Icons.Default.Article, MainTab.EXPENSES, "GUEST"),
    SleekMenuItem("Contact", Icons.Default.ContactMail, MainTab.REPORTS, "GUEST"),
    SleekMenuItem("Admin", Icons.Default.AdminPanelSettings, MainTab.SETTINGS, "ADMIN")
)

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

    val colors = getSchemeColors(viewModel.activeColorScheme, viewModel.isDarkMode)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(if (viewModel.isStickyNavEnabled) 4.dp else 0.dp, shape = RoundedCornerShape(0.dp)),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
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
                            tint = colors.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                } else {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LaptopMac,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }

                Column {
                    Text(
                        text = viewModel.devName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "WordPress Web Dev Portfolio",
                        fontSize = 10.sp,
                        color = colors.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
                Text(
                    text = timeStr,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textSecondary,
                    modifier = Modifier
                        .background(colors.bgMain, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )

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
                            tint = colors.textSecondary
                        )
                    }
                    if (viewModel.notifications.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(Color.Red, CircleShape)
                                .align(Alignment.TopEnd)
                                .offset(x = (-8).dp, y = (8).dp)
                        )
                    }
                }

                if (viewModel.loggedInUser != null) {
                    IconButton(
                        onClick = { viewModel.performLogout() },
                        modifier = Modifier
                            .testTag("sleek_logout_button")
                            .size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Log out",
                            tint = Color.Red
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SleekSidebar(
    viewModel: POSViewModel,
    onClose: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val loggedInUser = viewModel.loggedInUser
    val colors = getSchemeColors(viewModel.activeColorScheme, viewModel.isDarkMode)

    val filteredMenuItems = SleekMenuItemsList.filter { item ->
        if (item.requiredRole == "ADMIN") {
            viewModel.firebaseAuthService.isAuthorized(loggedInUser, "ADMIN")
        } else {
            true
        }
    }

    Box(
        modifier = modifier
            .width(260.dp)
            .fillMaxHeight()
            .background(colors.surface)
            .border(width = 1.dp, color = colors.border)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
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
                            .background(colors.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Laptop,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Portfolio Hub",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "WP Studio v2.0",
                            fontSize = 10.sp,
                            color = colors.textSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close navigation panel",
                        tint = colors.textSecondary
                    )
                }
            }

            Divider(color = colors.border, modifier = Modifier.padding(bottom = 8.dp))

            // User Profile Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.bgMain)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(colors.primary.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (loggedInUser != null) loggedInUser.displayName.take(1) else "V",
                        color = colors.primary,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (loggedInUser != null) loggedInUser.displayName else "Guest Visitor",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Box(
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .background(colors.primary.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = if (loggedInUser != null) "PORTFOLIO OWNER" else "PROSPECTIVE CLIENT",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = colors.primary,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "WEBSITE SECTIONS",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textSecondary,
                modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
            )

            // Dynamic links
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
                            .background(if (isSelected) colors.primary else Color.Transparent)
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
                            tint = if (isSelected) Color.White else colors.textPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = item.title,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else colors.textPrimary
                        )
                    }
                }
            }

            Divider(color = colors.border)
            
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
                            .background(Color.Green, CircleShape)
                    )
                    Text(
                        text = "Static Site Node",
                        fontSize = 10.sp,
                        color = colors.textSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = "READY",
                    fontSize = 9.sp,
                    color = colors.textSecondary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SleekBottomBar(
    viewModel: POSViewModel,
    modifier: Modifier = Modifier
) {
    val loggedInUser = viewModel.loggedInUser
    val colors = getSchemeColors(viewModel.activeColorScheme, viewModel.isDarkMode)
    
    val bottomMenuItems = listOf(
        SleekMenuItem("Home", Icons.Default.Home, MainTab.DASHBOARD, "GUEST"),
        SleekMenuItem("Portfolio", Icons.Default.Work, MainTab.POS, "GUEST"),
        SleekMenuItem("Services", Icons.Default.Layers, MainTab.PRODUCTS, "GUEST"),
        SleekMenuItem("Skills", Icons.Default.Grade, MainTab.CUSTOMERS, "GUEST"),
        SleekMenuItem("Contact", Icons.Default.ContactMail, MainTab.REPORTS, "GUEST")
    )

    NavigationBar(
        containerColor = colors.surface,
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
                        tint = if (isSelected) Color.White else colors.textSecondary
                    ) 
                },
                label = { 
                    Text(
                        text = item.title, 
                        fontSize = 10.sp,
                        color = if (isSelected) colors.primary else colors.textSecondary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    ) 
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = colors.primary,
                    indicatorColor = colors.primary,
                    unselectedIconColor = colors.textSecondary,
                    unselectedTextColor = colors.textSecondary
                ),
                modifier = Modifier.testTag("sleek_bottom_tab_${item.title.replace(" ", "_").lowercase()}")
            )
        }
    }
}
