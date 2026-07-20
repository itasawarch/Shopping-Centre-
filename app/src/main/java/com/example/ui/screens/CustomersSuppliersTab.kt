package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.window.Dialog
import com.example.data.Skill
import com.example.ui.POSViewModel
import com.example.ui.components.getSchemeColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomersSuppliersTab(viewModel: POSViewModel) {
    val colors = getSchemeColors(viewModel.activeColorScheme, viewModel.isDarkMode)
    val skillsList by viewModel.skills.collectAsState()
    val isAdmin = viewModel.loggedInUser != null

    var activeCategoryFilter by remember { mutableStateOf("All") } // "All", "CMS / WordPress", "Core Frontend", "Backend & DB", "Design & Tools"
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var skillToEdit by remember { mutableStateOf<Skill?>(null) }

    // Standard core categories in alignment with seeded data
    val categories = listOf("All", "CMS / WordPress", "Core Frontend", "Backend & DB", "Design & Tools")

    val filteredSkills = skillsList.filter { skill ->
        val matchesCategory = activeCategoryFilter == "All" ||
                skill.category.equals(activeCategoryFilter, ignoreCase = true)
        val matchesSearch = searchQuery.isBlank() ||
                skill.name.contains(searchQuery, ignoreCase = true) ||
                skill.category.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgMain)
            .padding(16.dp)
            .testTag("customers_suppliers_tab")
    ) {
        // ==========================================
        // 1. HEADER TITLE ROW
        // ==========================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Technical Skill Matrix",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = colors.textPrimary
                )
                Text(
                    text = "Dynamic expertise rating across full-stack languages, CMS customizers, and modern libraries.",
                    fontSize = 11.sp,
                    color = colors.textSecondary
                )
            }

            if (isAdmin) {
                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("add_partner_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Skill", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search panel
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search skill names (e.g., WordPress, Kotlin, PHP)...", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = colors.primary) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.primary,
                unfocusedBorderColor = colors.border
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("partner_search_field")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Category Filter Row Pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { cat ->
                val isSel = activeCategoryFilter == cat
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSel) colors.primary else colors.surface)
                        .border(1.dp, if (isSel) colors.primary else colors.border, RoundedCornerShape(20.dp))
                        .clickable { activeCategoryFilter = cat }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = cat,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSel) Color.White else colors.textSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ==========================================
        // 2. SKILL PROGRESS BAR CARDS
        // ==========================================
        if (filteredSkills.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = colors.textSecondary.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No skills match this filter criteria.",
                        color = colors.textSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredSkills) { skill ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, colors.border, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = skill.name,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.sp,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = "Section: ${skill.category}",
                                        fontSize = 11.sp,
                                        color = colors.textSecondary
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .background(colors.primary.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "${(skill.progress / 10).coerceAtLeast(1)}Y EXP",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = colors.primary
                                        )
                                    }

                                    if (isAdmin) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        IconButton(
                                            onClick = { skillToEdit = skill },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit Skill",
                                                tint = colors.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = { viewModel.deleteSkill(skill) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete Skill",
                                                tint = Color.Red,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Beautiful Progress Tracker Row
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                LinearProgressIndicator(
                                    progress = skill.progress / 100f,
                                    color = colors.primary,
                                    trackColor = colors.border,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "${skill.progress}%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = colors.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // ==========================================
    // 3. REGISTER SKILL DIALOG (ADMIN)
    // ==========================================
    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("CMS / WordPress") }
        var progressText by remember { mutableStateOf("90") }

        Dialog(onDismissRequest = { showAddDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = BorderStroke(1.dp, colors.border)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Register Technical Skill",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = colors.textPrimary
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Skill Name *") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.primary),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Category Selections
                    Text("Select Skill Segment Category:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.textSecondary)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val segs = listOf("CMS / WordPress", "Core Frontend", "Backend & DB", "Design & Tools")
                        segs.forEach { seg ->
                            val isSel = category == seg
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) colors.primary.copy(alpha = 0.15f) else Color.Transparent)
                                    .border(1.dp, if (isSel) colors.primary else Color.Transparent, RoundedCornerShape(8.dp))
                                    .clickable { category = seg }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSel,
                                    onClick = { category = seg },
                                    colors = RadioButtonDefaults.colors(selectedColor = colors.primary)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = seg,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) colors.primary else colors.textSecondary
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = progressText,
                        onValueChange = { progressText = it },
                        label = { Text("Expertise Proficiency (0-100)%") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.primary),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showAddDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                if (name.isNotBlank()) {
                                    val progressVal = progressText.toIntOrNull() ?: 90
                                    viewModel.addSkill(name, progressVal, category)
                                    showAddDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Text("Save Skill")
                        }
                    }
                }
            }
        }
    }

    // ==========================================
    // 4. EDIT SKILL DIALOG (ADMIN)
    // ==========================================
    if (skillToEdit != null) {
        val sk = skillToEdit!!
        var name by remember { mutableStateOf(sk.name) }
        var category by remember { mutableStateOf(sk.category) }
        var progressText by remember { mutableStateOf(sk.progress.toString()) }

        Dialog(onDismissRequest = { skillToEdit = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = BorderStroke(1.dp, colors.border)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Modify Technical Skill",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = colors.textPrimary
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Skill Name *") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.primary),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Category Selection
                    Text("Select Skill Segment Category:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.textSecondary)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val segs = listOf("CMS / WordPress", "Core Frontend", "Backend & DB", "Design & Tools")
                        segs.forEach { seg ->
                            val isSel = category == seg
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) colors.primary.copy(alpha = 0.15f) else Color.Transparent)
                                    .border(1.dp, if (isSel) colors.primary else Color.Transparent, RoundedCornerShape(8.dp))
                                    .clickable { category = seg }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSel,
                                    onClick = { category = seg },
                                    colors = RadioButtonDefaults.colors(selectedColor = colors.primary)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = seg,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) colors.primary else colors.textSecondary
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = progressText,
                        onValueChange = { progressText = it },
                        label = { Text("Expertise Proficiency (0-100)%") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.primary),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { skillToEdit = null },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                if (name.isNotBlank()) {
                                    val progressVal = progressText.toIntOrNull() ?: 90
                                    viewModel.addSkill(name, progressVal, category)
                                    skillToEdit = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Text("Update Skill")
                        }
                    }
                }
            }
        }
    }
}
