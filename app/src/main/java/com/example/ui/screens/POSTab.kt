package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.Project
import com.example.ui.POSViewModel
import com.example.ui.components.getSchemeColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun POSTab(viewModel: POSViewModel) {
    val colors = getSchemeColors(viewModel.activeColorScheme, viewModel.isDarkMode)
    val projectsList by viewModel.projects.collectAsState()
    val uriHandler = LocalUriHandler.current

    var searchQuery by remember { mutableStateOf("") }
    var selectedProjectForLightbox by remember { mutableStateOf<Project?>(null) }

    // Filter project list by category AND search query
    val filteredProjects = projectsList.filter { project ->
        val categoryMatches = viewModel.activeProjectFilter == "All" || 
                project.category.equals(viewModel.activeProjectFilter, ignoreCase = true)
        
        val searchMatches = searchQuery.isBlank() ||
                project.title.contains(searchQuery, ignoreCase = true) ||
                project.description.contains(searchQuery, ignoreCase = true) ||
                project.technologies.contains(searchQuery, ignoreCase = true)
        
        categoryMatches && searchMatches
    }

    val categories = listOf("All", "WordPress", "Web", "Mobile")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgMain)
            .padding(16.dp)
            .testTag("pos_tab")
    ) {
        // ==========================================
        // 1. FILTERING & SEARCH HEADER
        // ==========================================
        Text(
            text = "Project Portfolio Showcase",
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = colors.textPrimary,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "Browse fully functional live projects, custom WordPress designs, and Android systems.",
            fontSize = 11.sp,
            color = colors.textSecondary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Instant search field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search projects by name or technology (e.g. WooCommerce, Stripe)...", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = colors.primary) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = null)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.primary,
                unfocusedBorderColor = colors.border
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("pos_search_input")
        )

        Spacer(modifier = Modifier.height(14.dp))

        // WordPress, Web, Mobile Category Tabs Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { cat ->
                val isSel = viewModel.activeProjectFilter == cat
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSel) colors.primary else colors.surface)
                        .border(1.dp, if (isSel) colors.primary else colors.border, RoundedCornerShape(20.dp))
                        .clickable { viewModel.activeProjectFilter = cat }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
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
        // 2. PROJECT CARDS GRID
        // ==========================================
        if (filteredProjects.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Inventory,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = colors.textSecondary.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No projects match your current selection.",
                        color = colors.textSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(280.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("projects_grid")
            ) {
                items(filteredProjects) { project ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                            .clickable { selectedProjectForLightbox = project },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.surface)
                    ) {
                        Column {
                            // Beautiful Hero Thumbnail
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                            ) {
                                AsyncImage(
                                    model = project.imageUrl,
                                    contentDescription = project.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )

                                // Category Overlay Tag
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(10.dp)
                                        .background(colors.primary, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = project.category.uppercase(),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                }
                            }

                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = project.title,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = colors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = project.description,
                                    fontSize = 11.sp,
                                    color = colors.textSecondary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    lineHeight = 15.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Tech pills
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    spacing = 4.dp
                                ) {
                                    project.technologies.split(",").forEach { tech ->
                                        Box(
                                            modifier = Modifier
                                                .background(colors.bgMain, RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = tech.trim(),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = colors.primary
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Action Links Buttons (Live Demo & Github)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { uriHandler.openUri(project.demoUrl) },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primary),
                                        border = BorderStroke(1.dp, colors.primary),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(34.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Launch, contentDescription = null, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Live Demo", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = { uriHandler.openUri(project.githubUrl) },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(34.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Code, contentDescription = null, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Source", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ==========================================
    // 3. PROJECT DETAIL LIGHTBOX MODAL DIALOG
    // ==========================================
    if (selectedProjectForLightbox != null) {
        val proj = selectedProjectForLightbox!!
        Dialog(onDismissRequest = { selectedProjectForLightbox = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.85f),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = BorderStroke(1.dp, colors.border)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Full lightbox hero image
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        AsyncImage(
                            model = proj.imageUrl,
                            contentDescription = proj.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        IconButton(
                            onClick = { selectedProjectForLightbox = null },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }

                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = proj.title,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = colors.textPrimary
                        )
                        
                        Box(
                            modifier = Modifier
                                .padding(vertical = 6.dp)
                                .background(colors.primary.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = proj.category.uppercase(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = colors.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "PROJECT OVERVIEW",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textSecondary,
                            letterSpacing = 1.sp
                        )

                        Text(
                            text = proj.description,
                            fontSize = 13.sp,
                            color = colors.textPrimary,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                        )

                        Text(
                            text = "TECHNOLOGY STACK USED",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textSecondary,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            spacing = 6.dp
                        ) {
                            proj.technologies.split(",").forEach { tech ->
                                Box(
                                    modifier = Modifier
                                        .background(colors.bgMain, RoundedCornerShape(6.dp))
                                        .border(1.dp, colors.border, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = tech.trim(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.primary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Outer actions in details
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { uriHandler.openUri(proj.demoUrl); selectedProjectForLightbox = null },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Launch, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Launch Demo Site", fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { uriHandler.openUri(proj.githubUrl); selectedProjectForLightbox = null },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primary),
                                border = BorderStroke(1.dp, colors.primary),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Code, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Repository Code", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Custom flow-like row layout for pills to avoid wrapping overflows
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    spacing: androidx.compose.ui.unit.Dp = 4.dp,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        androidx.compose.ui.layout.Layout(
            content = content
        ) { measurables, constraints ->
            val spacingPx = spacing.roundToPx()
            var x = 0
            var y = 0
            var rowHeight = 0
            val placeables = measurables.map { measurable ->
                val placeable = measurable.measure(constraints.copy(minWidth = 0))
                if (x + placeable.width > constraints.maxWidth) {
                    x = 0
                    y += rowHeight + spacingPx
                    rowHeight = 0
                }
                x += placeable.width + spacingPx
                rowHeight = maxOf(rowHeight, placeable.height)
                placeable
            }

            x = 0
            y = 0
            rowHeight = 0
            layout(constraints.maxWidth, y + rowHeight + 100) {
                placeables.forEach { placeable ->
                    if (x + placeable.width > constraints.maxWidth) {
                        x = 0
                        y += rowHeight + spacingPx
                        rowHeight = 0
                    }
                    placeable.placeRelative(x, y)
                    x += placeable.width + spacingPx
                    rowHeight = maxOf(rowHeight, placeable.height)
                }
            }
        }
    }
}
