package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.MainTab
import com.example.ui.POSViewModel
import com.example.ui.components.SchemeColors
import com.example.ui.components.getSchemeColors

@Composable
fun DashboardTab(viewModel: POSViewModel) {
    val colors = getSchemeColors(viewModel.activeColorScheme, viewModel.isDarkMode)
    val testimonials by viewModel.testimonials.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgMain)
            .testTag("dashboard_tab")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ==========================================
            // 1. HERO BANNER SECTION (STUNNING FIRST IMPRESSION)
            // ==========================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(colors.primary.copy(alpha = 0.15f), Color.Transparent)
                        )
                    )
                    .padding(horizontal = 24.dp, vertical = 32.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Profile Image with elegant glowing ring
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .shadow(8.dp, CircleShape)
                            .background(colors.surface, CircleShape)
                            .border(3.dp, colors.primary, CircleShape)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&q=80&w=250",
                            contentDescription = "${viewModel.devName} Profile Image",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Name
                    Text(
                        text = viewModel.devName,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = colors.textPrimary,
                        textAlign = TextAlign.Center
                    )

                    // Real-Time Typing Text Animation Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .height(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = viewModel.typedText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary,
                            textAlign = TextAlign.Center
                        )
                        // Pulsing cursor
                        Text(
                            text = "|",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Bio Introduction
                    Text(
                        text = "Highly specialized in engineering custom, responsive, lightning-fast WordPress websites, e-commerce storefronts, and premium custom web applications with perfect Core Web Vitals.",
                        fontSize = 13.sp,
                        color = colors.textSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp,
                        modifier = Modifier.widthIn(max = 500.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Call-To-Action (CTA) Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = { viewModel.currentTab = MainTab.REPORTS },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .testTag("hero_hire_me_button")
                                .padding(horizontal = 6.dp)
                                .height(44.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Hire Me", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = { viewModel.currentTab = MainTab.POS },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primary),
                            border = BorderStroke(1.dp, colors.primary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .testTag("hero_portfolio_button")
                                .padding(horizontal = 6.dp)
                                .height(44.dp)
                        ) {
                            Icon(imageVector = Icons.Default.WorkOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Portfolio", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

            // ==========================================
            // 2. ANIMATED STATISTICS GRID SECTION
            // ==========================================
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                Text(
                    text = "Professional Statistics",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        count = "8+ Yrs",
                        label = "Tech Experience",
                        color = colors.primary,
                        bgColor = colors.surface,
                        borderColor = colors.border,
                        textColor = colors.textPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        count = "120+",
                        label = "Sites Launched",
                        color = colors.primary,
                        bgColor = colors.surface,
                        borderColor = colors.border,
                        textColor = colors.textPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        count = "100%",
                        label = "Job Success",
                        color = colors.primary,
                        bgColor = colors.surface,
                        borderColor = colors.border,
                        textColor = colors.textPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        count = "95+",
                        label = "PageSpeed Score",
                        color = colors.primary,
                        bgColor = colors.surface,
                        borderColor = colors.border,
                        textColor = colors.textPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ==========================================
            // 3. CORE SERVICES PREVIEW / OVERVIEW
            // ==========================================
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Core Developer Offerings",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "View All",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary,
                        modifier = Modifier.clickable { viewModel.currentTab = MainTab.PRODUCTS }
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, colors.border, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = colors.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OfferingRow(icon = Icons.Default.Language, title = "Custom WordPress Design", desc = "Pixel-perfect mockups coded securely into Gutenberg or Elementor.", colors = colors)
                        Divider(color = colors.border, modifier = Modifier.padding(vertical = 10.dp))
                        OfferingRow(icon = Icons.Default.ShoppingCart, title = "WooCommerce Stores", desc = "Secure checkouts, dynamic payment integrations & fast cart flows.", colors = colors)
                        Divider(color = colors.border, modifier = Modifier.padding(vertical = 10.dp))
                        OfferingRow(icon = Icons.Default.FlashOn, title = "Google PageSpeed Optimization", desc = "Reduce load speeds to <1.5s and secure green Core Web Vitals.", colors = colors)
                    }
                }
            }

            // ==========================================
            // 4. CLIENT TESTIMONIALS SECTION
            // ==========================================
            if (testimonials.isNotEmpty()) {
                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                    Text(
                        text = "Client Testimonials",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    testimonials.forEach { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .border(1.dp, colors.border, RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(containerColor = colors.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = item.clientName,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.textPrimary
                                        )
                                        Text(
                                            text = item.company,
                                            fontSize = 10.sp,
                                            color = colors.textSecondary
                                        )
                                    }
                                    Row {
                                        repeat(item.rating) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = "Star",
                                                tint = Color(0xFFFBBF24),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "\"${item.feedback}\"",
                                    fontSize = 12.sp,
                                    color = colors.textSecondary,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            // ==========================================
            // 5. NEWSLETTER SUBSCRIPTION ROW
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surface)
                    .border(BorderStroke(1.dp, colors.border))
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.MailOutline,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Subscribe to My Web Developer Newsletter",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Receive technical SEO guides, speed optimization formulas, and modern UI frameworks directly to your inbox.",
                    fontSize = 11.sp,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(vertical = 6.dp)
                        .widthIn(max = 400.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (viewModel.isNewsletterSubscribed) {
                    Box(
                        modifier = Modifier
                            .background(colors.primary.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "🎉 You have subscribed successfully!",
                            color = colors.primary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 400.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = viewModel.newsletterEmail,
                            onValueChange = { viewModel.newsletterEmail = it },
                            placeholder = { Text("Enter your email address", fontSize = 12.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.primary,
                                unfocusedBorderColor = colors.border
                            ),
                            shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("newsletter_email_input")
                        )
                        Button(
                            onClick = { viewModel.subscribeNewsletter() },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                            shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp),
                            modifier = Modifier
                                .height(50.dp)
                                .testTag("newsletter_subscribe_button")
                        ) {
                            Text("Join", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Footer Spacer
            Spacer(modifier = Modifier.height(80.dp))
        }

        // ==========================================
        // 6. COOKIE CONSENT FLOATING BANNER
        // ==========================================
        if (viewModel.showCookieConsent) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
                    .shadow(12.dp, RoundedCornerShape(16.dp))
                    .background(colors.surface, RoundedCornerShape(16.dp))
                    .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                    .padding(14.dp)
                    .testTag("cookie_consent_banner")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Privacy & Cookies",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "This platform stores local data using offline Room databases to optimize speed and UI caching.",
                            fontSize = 10.sp,
                            color = colors.textSecondary,
                            lineHeight = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = { viewModel.showCookieConsent = false },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Accept", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    count: String,
    label: String,
    color: Color,
    bgColor: Color,
    borderColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .border(1.dp, borderColor, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = count,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun OfferingRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    desc: String,
    colors: SchemeColors
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(colors.primary.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            Text(
                text = desc,
                fontSize = 11.sp,
                color = colors.textSecondary,
                lineHeight = 15.sp
            )
        }
    }
}
