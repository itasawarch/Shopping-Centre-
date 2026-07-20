package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.Inquiry
import com.example.ui.POSViewModel
import com.example.ui.components.SchemeColors
import com.example.ui.components.getSchemeColors
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsTab(viewModel: POSViewModel) {
    val colors = getSchemeColors(viewModel.activeColorScheme, viewModel.isDarkMode)
    val inquiriesList by viewModel.inquiries.collectAsState()
    val isAdmin = viewModel.loggedInUser != null
    val uriHandler = LocalUriHandler.current

    var clientName by remember { mutableStateOf("") }
    var clientEmail by remember { mutableStateOf("") }
    var projectType by remember { mutableStateOf("WordPress Design") }
    var message by remember { mutableStateOf("") }
    var showSuccessMessage by remember { mutableStateOf(false) }

    val projectCategories = listOf("WordPress Design", "WooCommerce E-Commerce", "Speed Optimization", "Custom API Backend", "Other Project")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgMain)
            .padding(16.dp)
            .testTag("reports_tab")
    ) {
        // Scrollable page wrapper to support small mobile screens and high visual density
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ==========================================
            // 1. TITLE HEADER
            // ==========================================
            Text(
                text = "Get in Touch / Hire Me",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = colors.textPrimary,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "Submit your technical requirements or secure custom website quotes instantly.",
                fontSize = 11.sp,
                color = colors.textSecondary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // ==========================================
            // 2. CONTACT CHANNELS QUICK DEEP LINKS
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ContactCard(
                    icon = Icons.Default.Email,
                    label = "Email Direct",
                    value = viewModel.devEmail,
                    colors = colors,
                    onClick = { uriHandler.openUri("mailto:${viewModel.devEmail}?subject=Inquiry") },
                    modifier = Modifier.weight(1f)
                )

                ContactCard(
                    icon = Icons.Default.Chat,
                    label = "WhatsApp Chat",
                    value = viewModel.devPhone,
                    colors = colors,
                    onClick = {
                        val waNum = viewModel.devPhone.filter { it.isDigit() }
                        uriHandler.openUri("https://wa.me/$waNum")
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            // Success Lead Alert Banner
            if (showSuccessMessage) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.primary.copy(alpha = 0.15f))
                        .border(1.dp, colors.primary, RoundedCornerShape(12.dp))
                        .clickable { showSuccessMessage = false }
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = colors.primary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("🎉 Inquiry Lodged Successfully!", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.primary)
                            Text("${viewModel.devName} has cached your message and will respond via email shortly.", fontSize = 11.sp, color = colors.textSecondary)
                        }
                    }
                }
            }

            // ==========================================
            // 3. INQUIRY FORM SHEET
            // ==========================================
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.border, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Instant Requirements Form",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = colors.textPrimary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = clientName,
                        onValueChange = { clientName = it },
                        label = { Text("Your Full Name *", fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("backup_button") // Maps to safe UI testTag
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = clientEmail,
                        onValueChange = { clientEmail = it },
                        label = { Text("Email Address / Phone *", fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.primary),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Project Type Selection Dropdown Layout
                    Text("Select Website Project Focus:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.textSecondary)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        projectCategories.forEach { cat ->
                            val isSel = projectType == cat
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) colors.primary else colors.bgMain)
                                    .clickable { projectType = cat }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = cat,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color.White else colors.textSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        label = { Text("Describe Your Project Requirements / Specs *", fontSize = 12.sp) },
                        minLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.primary),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (clientName.isNotBlank() && clientEmail.isNotBlank() && message.isNotBlank()) {
                                viewModel.addInquiry(clientName, clientEmail, projectType, message)
                                clientName = ""
                                clientEmail = ""
                                message = ""
                                showSuccessMessage = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Send Inquiry to Developer", fontWeight = FontWeight.Black)
                    }
                }
            }

            // ==========================================
            // 4. ADMIN LEADS MONITOR FEED (ROLE LOCKED)
            // ==========================================
            if (isAdmin) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Lead Generation Vault (Admin)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (inquiriesList.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = colors.surface)
                    ) {
                        Text(
                            text = "No inquiries filed yet.",
                            color = colors.textSecondary,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    inquiriesList.forEach { inq ->
                        val inqDate = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(inq.timestamp))
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .border(1.dp, colors.border, RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(containerColor = colors.surface),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(text = inq.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                                        Text(text = "Contact: ${inq.email}", fontSize = 10.sp, color = colors.textSecondary)
                                    }
                                    IconButton(
                                        onClick = { viewModel.deleteInquiry(inq) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(16.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Box(
                                    modifier = Modifier
                                        .background(colors.primary.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(text = inq.phone.uppercase(), fontSize = 8.sp, fontWeight = FontWeight.Black, color = colors.primary)
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(text = inq.message, fontSize = 11.sp, color = colors.textPrimary, lineHeight = 15.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = "Submitted at: $inqDate", fontSize = 8.sp, color = colors.textSecondary)
                            }
                        }
                    }
                }
            }

            // Footer Spacer
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun ContactCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    colors: SchemeColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .border(1.dp, colors.border, RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(colors.primary.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = colors.primary, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
            Text(text = value, fontSize = 9.sp, color = colors.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
