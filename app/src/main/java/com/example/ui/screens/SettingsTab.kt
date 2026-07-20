package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppColorScheme
import com.example.ui.POSViewModel
import com.example.ui.components.getSchemeColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTab(viewModel: POSViewModel) {
    val colors = getSchemeColors(viewModel.activeColorScheme, viewModel.isDarkMode)
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf(viewModel.devName) }
    var email by remember { mutableStateOf(viewModel.devEmail) }
    var address by remember { mutableStateOf(viewModel.devAddress) }
    var phone by remember { mutableStateOf(viewModel.devPhone) }
    var isManualOffline by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgMain)
            .testTag("settings_tab")
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Title section
        item {
            Column {
                Text(
                    text = "System Admin Controls",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = colors.textPrimary
                )
                Text(
                    text = "Manage SQLite persistence, active visual theme, and localization assets.",
                    fontSize = 11.sp,
                    color = colors.textSecondary
                )
            }
        }

        // 2. Identity Config Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.border, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "General App & Developer Identity",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary
                    )
                    
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Developer Display Name") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.primary),
                        modifier = Modifier.fillMaxWidth().testTag("settings_shop_name")
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Developer Contact Email") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.primary),
                        modifier = Modifier.fillMaxWidth().testTag("settings_shop_email")
                    )

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Office Address / Base") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.primary),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Client Hot-line Phone") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.primary),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // 3. Theme & Visual Engine Config Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.border, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Dynamic UI & Theme Configuration",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary
                    )

                    // Scheme Selector (Classic Blue, Midnight Navy, etc.)
                    Text(
                        text = "Active Palette Selection:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textSecondary
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppColorScheme.values().forEach { scheme ->
                            val isSel = viewModel.activeColorScheme == scheme
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) colors.primary else colors.bgMain)
                                    .clickable { viewModel.activeColorScheme = scheme }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = scheme.name.replace("_", " "),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color.White else colors.textSecondary
                                )
                            }
                        }
                    }

                    Divider(color = colors.border)

                    // Theme Selector toggler (Dark / Light Mode)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Aesthetic Dark Mode", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                            Text("Switch system to warm eye-safe canvas", fontSize = 11.sp, color = colors.textSecondary)
                        }

                        Switch(
                            checked = viewModel.isDarkMode,
                            onCheckedChange = { viewModel.isDarkMode = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = colors.primary,
                                checkedTrackColor = colors.primary.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.testTag("dark_mode_switch")
                        )
                    }

                    Divider(color = colors.border)

                    // Simulated Offline Mode Toggler
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Simulate Offline Mode", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                            Text("Force components to perform in manual sandbox", fontSize = 11.sp, color = colors.textSecondary)
                        }

                        Switch(
                            checked = isManualOffline,
                            onCheckedChange = { isManualOffline = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Red,
                                checkedTrackColor = Color.Red.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.testTag("offline_mode_switch")
                        )
                    }
                }
            }
        }

        // 4. Save Button Row
        item {
            Button(
                onClick = {
                    viewModel.saveDeveloperProfile(name, email, phone, address)
                    viewModel.addNotification("Developer Identity updated to $name successfully.")
                },
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("save_settings_btn")
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("SAVE GENERAL SETTINGS", fontWeight = FontWeight.Bold)
            }
        }

        // 5. System diagnostics Info Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.primary.copy(alpha = 0.15f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.primary, RoundedCornerShape(12.dp))
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = colors.primary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "System active. Active SQLite binary synchronized automatically under thread-safe SQLite sandbox.",
                        fontSize = 11.sp,
                        color = colors.textPrimary
                    )
                }
            }
        }

        // 6. SQLite Persistence Admin Panel
        item {
            LaunchedEffect(key1 = true) {
                viewModel.refreshDbDiagnostics()
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.border, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface)
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SQLite Persistent Diagnostics",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = colors.textPrimary
                            )
                        }
                        
                        Text(
                            text = "${String.format("%.1f", viewModel.dbSize / 1024.0)} KB",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary,
                            modifier = Modifier
                                .background(colors.primary.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        text = "Live records inside offline SQLite. Fully synced.",
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )

                    // Table counts grid - Row 1
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        viewModel.dbTableStats.take(5).forEach { stat ->
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(colors.bgMain, RoundedCornerShape(10.dp))
                                    .border(0.5.dp, colors.border, RoundedCornerShape(10.dp))
                                    .padding(6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = stat.tableName.uppercase().take(5),
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.Black,
                                    color = colors.textSecondary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = stat.recordCount.toString(),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                            }
                        }
                    }

                    // Table counts grid - Row 2
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        viewModel.dbTableStats.drop(5).take(5).forEach { stat ->
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(colors.bgMain, RoundedCornerShape(10.dp))
                                    .border(0.5.dp, colors.border, RoundedCornerShape(10.dp))
                                    .padding(6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = stat.tableName.uppercase().take(5),
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.Black,
                                    color = colors.textSecondary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = stat.recordCount.toString(),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                            }
                        }
                    }

                    Divider(color = colors.border)

                    // Admin Actions Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        var isBackingUp by remember { mutableStateOf(false) }
                        var isRestoring by remember { mutableStateOf(false) }
                        var isVacuuming by remember { mutableStateOf(false) }
                        var isResetting by remember { mutableStateOf(false) }

                        Button(
                            onClick = {
                                isBackingUp = true
                                scope.launch {
                                    viewModel.sqlDatabaseController.backupDatabase()
                                    isBackingUp = false
                                    viewModel.refreshDbDiagnostics()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Backup, contentDescription = null, modifier = Modifier.size(11.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("Backup", fontSize = 9.sp)
                        }

                        Button(
                            onClick = {
                                isRestoring = true
                                scope.launch {
                                    viewModel.sqlDatabaseController.restoreDatabase()
                                    isRestoring = false
                                    viewModel.refreshDbDiagnostics()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.SettingsBackupRestore, contentDescription = null, modifier = Modifier.size(11.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("Restore", fontSize = 9.sp)
                        }

                        Button(
                            onClick = {
                                isVacuuming = true
                                scope.launch {
                                    viewModel.sqlDatabaseController.runVacuum()
                                    isVacuuming = false
                                    viewModel.refreshDbDiagnostics()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(11.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("Vacuum", fontSize = 9.sp)
                        }

                        Button(
                            onClick = {
                                isResetting = true
                                scope.launch {
                                    viewModel.sqlDatabaseController.resetAndReinitialize()
                                    isResetting = false
                                    viewModel.refreshDbDiagnostics()
                                    viewModel.addNotification("Database has been reset & reinitialized with mock records.")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1.1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.RotateLeft, contentDescription = null, modifier = Modifier.size(11.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("Reset Data", fontSize = 9.sp)
                        }
                    }

                    Divider(color = colors.border)

                    // Interactive console
                    Text(
                        text = "Interactive SQLite Diagnostic Console",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )

                    OutlinedTextField(
                        value = viewModel.rawQueryText,
                        onValueChange = { viewModel.rawQueryText = it },
                        placeholder = { Text("e.g. SELECT * FROM projects LIMIT 5") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontSize = 11.sp
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.border
                        )
                    )

                    Button(
                        onClick = { viewModel.runRawQuery() },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.textPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Terminal, contentDescription = null, modifier = Modifier.size(14.dp), tint = colors.surface)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("EXECUTE SQL STATEMENT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.surface)
                    }

                    // Console result output area
                    viewModel.rawQueryResult?.let { result ->
                        Text(
                            text = "Query Results (${result.size} rows):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )

                        Box(
                            modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 200.dp)
                                        .background(Color(0xFF0F172A), RoundedCornerShape(10.dp))
                                        .padding(12.dp)
                        ) {
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(result) { row ->
                                    Text(
                                        text = row.toString(),
                                        color = Color(0xFF38BDF8),
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                        fontSize = 10.sp
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
