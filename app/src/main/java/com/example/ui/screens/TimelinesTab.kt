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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.Education
import com.example.data.Experience
import com.example.ui.POSViewModel
import com.example.ui.components.SchemeColors
import com.example.ui.components.getSchemeColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelinesTab(viewModel: POSViewModel) {
    val colors = getSchemeColors(viewModel.activeColorScheme, viewModel.isDarkMode)
    val experienceList by viewModel.experiences.collectAsState()
    val educationList by viewModel.educations.collectAsState()
    val isAdmin = viewModel.loggedInUser != null

    var activeSegment by remember { mutableStateOf("Experience") } // "Experience" or "Education"
    var showAddExpDialog by remember { mutableStateOf(false) }
    var showAddEduDialog by remember { mutableStateOf(false) }
    
    var cvDownloading by remember { mutableStateOf(false) }
    var cvDownloaded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgMain)
            .padding(16.dp)
            .testTag("timelines_tab")
    ) {
        // ==========================================
        // 1. TITLE AND HEADER WITH CV DOWNLOADER
        // ==========================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Professional Timeline",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = colors.textPrimary
                )
                Text(
                    text = "A chronicle of senior leadership, education, and contributions.",
                    fontSize = 11.sp,
                    color = colors.textSecondary
                )
            }

            // CV Download Button
            if (cvDownloaded) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.primary.copy(alpha = 0.15f))
                        .clickable { cvDownloaded = false }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("✓ CV Saved", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.primary)
                }
            } else {
                Button(
                    onClick = {
                        cvDownloading = true
                        // Simulate offline compile download process
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    if (cvDownloading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                        LaunchedEffect(Unit) {
                            kotlinx.coroutines.delay(1200)
                            cvDownloading = false
                            cvDownloaded = true
                        }
                    } else {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Get CV", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Switch Resume Segment Toggle (Experience / Education)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surface, RoundedCornerShape(12.dp))
                .border(1.dp, colors.border, RoundedCornerShape(12.dp))
                .padding(4.dp)
        ) {
            listOf("Experience", "Education").forEach { segment ->
                val isSel = activeSegment == segment
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSel) colors.primary else Color.Transparent)
                        .clickable { activeSegment = segment }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (segment == "Experience") "Work Experience" else "Education History",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSel) Color.White else colors.textSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ADD BUTTON FOR LOGGED IN ADMINS
        if (isAdmin) {
            Button(
                onClick = {
                    if (activeSegment == "Experience") showAddExpDialog = true
                    else showAddEduDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Register New $activeSegment Milestone", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        // ==========================================
        // 2. TIMELINE SCROLLABLE CONTENT
        // ==========================================
        if (activeSegment == "Experience") {
            if (experienceList.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No career history records found.", color = colors.textSecondary)
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(experienceList) { exp ->
                        TimelineNodeRow(
                            title = exp.role,
                            subtitle = exp.company,
                            period = exp.period,
                            description = exp.description,
                            colors = colors,
                            isAdmin = isAdmin,
                            onDelete = { viewModel.deleteExperience(exp) }
                        )
                    }
                }
            }
        } else {
            if (educationList.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No education logs found.", color = colors.textSecondary)
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(educationList) { edu ->
                        TimelineNodeRow(
                            title = edu.degree,
                            subtitle = edu.institution,
                            period = edu.period,
                            description = edu.description,
                            colors = colors,
                            isAdmin = isAdmin,
                            onDelete = { viewModel.deleteEducation(edu) }
                        )
                    }
                }
            }
        }
    }

    // ==========================================
    // 3. ADD EXPERIENCE DIALOG (ADMIN)
    // ==========================================
    if (showAddExpDialog) {
        var role by remember { mutableStateOf("") }
        var company by remember { mutableStateOf("") }
        var period by remember { mutableStateOf("") }
        var bullets by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showAddExpDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface)
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Add Career Experience", fontSize = 18.sp, fontWeight = FontWeight.Black, color = colors.textPrimary)

                    OutlinedTextField(
                        value = role,
                        onValueChange = { role = it },
                        label = { Text("Job Role / Title *") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.primary),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = company,
                        onValueChange = { company = it },
                        label = { Text("Company Name *") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.primary),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = period,
                        onValueChange = { period = it },
                        label = { Text("Employment Period (e.g., 2021 - Present) *") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.primary),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = bullets,
                        onValueChange = { bullets = it },
                        label = { Text("Bullet Contributions (Newline separated)") },
                        minLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.primary),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(onClick = { showAddExpDialog = false }, modifier = Modifier.weight(1f)) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                if (role.isNotBlank() && company.isNotBlank() && period.isNotBlank()) {
                                    viewModel.addExperience(role, company, period, bullets)
                                    showAddExpDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Text("Save Experience")
                        }
                    }
                }
            }
        }
    }

    // ==========================================
    // 4. ADD EDUCATION DIALOG (ADMIN)
    // ==========================================
    if (showAddEduDialog) {
        var degree by remember { mutableStateOf("") }
        var school by remember { mutableStateOf("") }
        var period by remember { mutableStateOf("") }
        var highlights by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showAddEduDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface)
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Add Education Milestone", fontSize = 18.sp, fontWeight = FontWeight.Black, color = colors.textPrimary)

                    OutlinedTextField(
                        value = degree,
                        onValueChange = { degree = it },
                        label = { Text("Degree / Certification *") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.primary),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = school,
                        onValueChange = { school = it },
                        label = { Text("School / University *") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.primary),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = period,
                        onValueChange = { period = it },
                        label = { Text("Enrollment Years (e.g., 2014 - 2018) *") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.primary),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = highlights,
                        onValueChange = { highlights = it },
                        label = { Text("Achievements Highlights (Optional)") },
                        minLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.primary),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(onClick = { showAddEduDialog = false }, modifier = Modifier.weight(1f)) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                if (degree.isNotBlank() && school.isNotBlank() && period.isNotBlank()) {
                                    viewModel.addEducation(degree, school, period, highlights)
                                    showAddEduDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Text("Save Education")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimelineNodeRow(
    title: String,
    subtitle: String,
    period: String,
    description: String,
    colors: SchemeColors,
    isAdmin: Boolean,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Timeline pipeline rail decoration column
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(36.dp)
        ) {
            // Circle node
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(colors.surface, CircleShape)
                    .border(3.dp, colors.primary, CircleShape)
            )

            // Vertical line pipe
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(130.dp)
                    .background(colors.border)
            )
        }

        // Details card adjacent to the pipeline
        Card(
            modifier = Modifier
                .weight(1f)
                .padding(start = 4.dp, bottom = 12.dp)
                .border(1.dp, colors.border, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "$subtitle • $period",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary
                        )
                    }

                    if (isAdmin) {
                        IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Render description lines cleanly
                if (description.isNotBlank()) {
                    val bulletList = description.split("\n")
                    bulletList.forEach { bullet ->
                        if (bullet.trim().isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "•",
                                    fontSize = 12.sp,
                                    color = colors.primary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(end = 6.dp)
                                )
                                Text(
                                    text = bullet.trim(),
                                    fontSize = 11.sp,
                                    color = colors.textSecondary,
                                    lineHeight = 15.sp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
