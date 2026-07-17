package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.lazy.items
import kotlinx.coroutines.launch
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
import com.example.ui.POSViewModel

@Composable
fun SettingsTab(viewModel: POSViewModel) {
    var name by remember { mutableStateOf(viewModel.shopName) }
    var address by remember { mutableStateOf(viewModel.shopAddress) }
    var phone by remember { mutableStateOf(viewModel.shopPhone) }
    var currency by remember { mutableStateOf(viewModel.shopCurrency) }
    var language by remember { mutableStateOf(viewModel.appLanguage) }
    var receipt by remember { mutableStateOf(viewModel.receiptDesign) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_tab")
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Title section
        item {
            Column {
                Text("Terminal & Shop Settings", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Configure terminal receipts, dark mode, and languages", fontSize = 12.sp, color = Color.Gray)
            }
        }

        // 2. Form Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("General Shop Identity", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF007A48))
                    
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Shop Business Name") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF007A48)),
                        modifier = Modifier.fillMaxWidth().testTag("settings_shop_name")
                    )

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Shop Address") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF007A48)),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Business Phone Number") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF007A48)),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // 3. App customization card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("Terminal & Visual Customization", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF007A48))

                    // Language toggler
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Language", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Urdu translation layout settings", fontSize = 11.sp, color = Color.Gray)
                        }

                        Row(
                            modifier = Modifier
                                .background(Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                                .padding(2.dp)
                        ) {
                            listOf("English", "Urdu").forEach { lang ->
                                val isSel = language == lang
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSel) Color(0xFF007A48) else Color.Transparent)
                                        .clickable { language = lang }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(lang, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSel) Color.White else Color.Black)
                                }
                            }
                        }
                    }

                    // Theme Selector toggler (Dark / Light Mode)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Terminal Theme Mode", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Switch colors to light or dark mode", fontSize = 11.sp, color = Color.Gray)
                        }

                        Switch(
                            checked = viewModel.isDarkMode,
                            onCheckedChange = { viewModel.isDarkMode = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF10B981),
                                checkedTrackColor = Color(0xFFEAF9F1)
                            ),
                            modifier = Modifier.testTag("dark_mode_switch")
                        )
                    }

                    Divider(color = Color(0xFFF1F5F9))

                    // Simulated/Manual Offline Mode
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Simulate Offline Mode", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Force the POS terminal to run offline", fontSize = 11.sp, color = Color.Gray)
                        }

                        Switch(
                            checked = viewModel.isManualOffline,
                            onCheckedChange = { viewModel.isManualOffline = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFFEF4444),
                                checkedTrackColor = Color(0xFFFEF2F2)
                            ),
                            modifier = Modifier.testTag("offline_mode_switch")
                        )
                    }

                    // Receipt Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Printer Receipt design", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Pre-configure column parameters", fontSize = 11.sp, color = Color.Gray)
                        }

                        Row(
                            modifier = Modifier
                                .background(Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                                .padding(2.dp)
                        ) {
                            listOf("Thermal 80mm", "A4 Standard").forEach { rec ->
                                val isSel = receipt == rec
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSel) Color(0xFF007A48) else Color.Transparent)
                                        .clickable { receipt = rec }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(rec, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isSel) Color.White else Color.Black)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Save Button Row
        item {
            Button(
                onClick = {
                    viewModel.saveShopSettings(
                        name = name,
                        addr = address,
                        ph = phone,
                        curr = currency,
                        lang = language,
                        receipt = receipt
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007A48)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("save_settings_btn")
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("SAVE SHOP SETTINGS", fontWeight = FontWeight.Bold)
            }
        }

        // 5. System diagnostics
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF3B82F6), RoundedCornerShape(12.dp))
            ) {
                Row(modifier = Modifier.padding(12.dp)) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF3B82F6))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "System online. Databases are synchronized automatically in local-first sandbox mode.",
                        fontSize = 11.sp,
                        color = Color(0xFF1E40AF)
                    )
                }
            }
        }

        // 6. SQLite Persistence & Offline-First Core Control Panel
        item {
            LaunchedEffect(key1 = true) {
                viewModel.refreshDbDiagnostics()
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
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
                                tint = Color(0xFF059669),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SQLite Persistent Schema & Admin",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                        }
                        
                        Text(
                            text = "${String.format("%.1f", viewModel.dbSize / 1024.0)} KB",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF059669),
                            modifier = Modifier
                                .background(Color(0xFFE6F4EA), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        text = "Real-time record counts inside local SQLite tables. Fully operational under offline-first sandbox.",
                        fontSize = 11.sp,
                        color = Color.Gray
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
                                    .background(Color(0xFFF8FAFC), RoundedCornerShape(10.dp))
                                    .border(0.5.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                                    .padding(6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = stat.tableName.uppercase().take(5),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = stat.recordCount.toString(),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
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
                                    .background(Color(0xFFF8FAFC), RoundedCornerShape(10.dp))
                                    .border(0.5.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                                    .padding(6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = stat.tableName.uppercase().take(5),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = stat.recordCount.toString(),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                            }
                        }
                    }

                    Divider(color = Color(0xFFF1F5F9))

                    // Admin Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        var isBackingUp by remember { mutableStateOf(false) }
                        var isRestoring by remember { mutableStateOf(false) }
                        var isVacuuming by remember { mutableStateOf(false) }
                        var isResetting by remember { mutableStateOf(false) }

                        val scope = rememberCoroutineScope()

                        Button(
                            onClick = {
                                isBackingUp = true
                                scope.launch {
                                    viewModel.sqlDatabaseController.backupDatabase()
                                    isBackingUp = false
                                    viewModel.refreshDbDiagnostics()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Backup,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp)
                            )
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
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SettingsBackupRestore,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp)
                            )
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
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CleaningServices,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp)
                            )
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
                                    viewModel.refreshAlerts()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1.1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.RotateLeft,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("Reset Data", fontSize = 9.sp)
                        }
                    }

                    Divider(color = Color(0xFFF1F5F9))

                    // Interactive SQL console section
                    Text(
                        text = "Interactive SQLite Diagnostic Console",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )

                    OutlinedTextField(
                        value = viewModel.rawQueryText,
                        onValueChange = { viewModel.rawQueryText = it },
                        placeholder = { Text("e.g. SELECT * FROM products LIMIT 5") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontSize = 11.sp
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF059669),
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        )
                    )

                    Button(
                        onClick = { viewModel.runRawQuery() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("EXECUTE SQL STATEMENT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Console result output area
                    viewModel.rawQueryResult?.let { result ->
                        Text(
                            text = "Query Results (${result.size} rows):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
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
