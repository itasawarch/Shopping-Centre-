package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.POSViewModel
import com.example.ui.Screen

@Composable
fun AuthScreen(viewModel: POSViewModel) {
    val isLogin = viewModel.currentScreen == Screen.LOGIN

    // Ambient Green Obsidian Gradient Background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF042014), // Obsidian Forest Green
                        Color(0xFF0D3322), // Deep Zam Zam Green
                        Color(0xFF001108)  // Solid Black Green
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Decorative floating ambient glow lights
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.TopStart)
                .offset(x = (-100).dp, y = (-50).dp)
                .background(Brush.radialGradient(listOf(Color(0x3310B981), Color.Transparent)))
        )
        Box(
            modifier = Modifier
                .size(350.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 100.dp, y = 100.dp)
                .background(Brush.radialGradient(listOf(Color(0x2210B981), Color.Transparent)))
        )

        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp)
                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(24.dp))
                .testTag("auth_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xD80B1E15) // Deep Green Glassmorphic look
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Enterprise Shop Identity Logo
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF10B981))
                        .border(1.dp, Color(0x66FFFFFF), RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Storefront,
                        contentDescription = "Shop Logo",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Zam Zam Whole Sale",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Shopping Centre & POS System",
                    fontSize = 13.sp,
                    color = Color(0xFFA7F3D0),
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Error message
                viewModel.authError?.let { err ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0x33EF4444), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xAAEF4444), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = err,
                            color = Color(0xFFFECACA),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (isLogin) {
                    // LOGIN FIELDS
                    OutlinedTextField(
                        value = viewModel.usernameInput,
                        onValueChange = { viewModel.usernameInput = it },
                        label = { Text("Username", color = Color(0x88FFFFFF)) },
                        placeholder = { Text("e.g. admin or cashier", color = Color(0x44FFFFFF)) },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF10B981)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF10B981),
                            unfocusedBorderColor = Color(0x33FFFFFF),
                            focusedLabelColor = Color(0xFF10B981),
                            unfocusedLabelColor = Color(0x88FFFFFF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("username_input")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    var passwordVisible by remember { mutableStateOf(false) }

                    OutlinedTextField(
                        value = viewModel.passwordInput,
                        onValueChange = { viewModel.passwordInput = it },
                        label = { Text("Password", color = Color(0x88FFFFFF)) },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF10B981)) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle password visibility",
                                    tint = Color(0xFF10B981)
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF10B981),
                            unfocusedBorderColor = Color(0x33FFFFFF),
                            focusedLabelColor = Color(0xFF10B981),
                            unfocusedLabelColor = Color(0x88FFFFFF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("password_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Remember Me & Forgot Password Placeholder
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = viewModel.rememberMe,
                                onCheckedChange = { viewModel.rememberMe = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color(0xFF10B981),
                                    checkmarkColor = Color.White,
                                    uncheckedColor = Color(0x66FFFFFF)
                                )
                            )
                            Text("Remember Me", color = Color(0xFFA7F3D0), fontSize = 12.sp)
                        }
                        TextButton(onClick = { viewModel.authError = "Please contact Haji Zam Zam Administrator to reset password." }) {
                            Text("Forgot?", color = Color(0xFF34D399), fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { viewModel.performLogin() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("login_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("LOGIN TO TERMINAL", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Need a staff account?", color = Color(0x99FFFFFF), fontSize = 13.sp)
                        TextButton(onClick = { viewModel.currentScreen = Screen.SIGNUP; viewModel.authError = null }) {
                            Text("Sign Up", color = Color(0xFF34D399), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                } else {
                    // SIGNUP FIELDS
                    OutlinedTextField(
                        value = viewModel.regDisplayName,
                        onValueChange = { viewModel.regDisplayName = it },
                        label = { Text("Full Name / Nickname", color = Color(0x88FFFFFF)) },
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = Color(0xFF10B981)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF10B981),
                            unfocusedBorderColor = Color(0x33FFFFFF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("reg_fullname")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = viewModel.regUsername,
                        onValueChange = { viewModel.regUsername = it },
                        label = { Text("Username", color = Color(0x88FFFFFF)) },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF10B981)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF10B981),
                            unfocusedBorderColor = Color(0x33FFFFFF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("reg_username")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = viewModel.regPassword,
                        onValueChange = { viewModel.regPassword = it },
                        label = { Text("Password", color = Color(0x88FFFFFF)) },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF10B981)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF10B981),
                            unfocusedBorderColor = Color(0x33FFFFFF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("reg_password")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Role Picker Dropdown Simulation (Clean Radio selection buttons)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0x11FFFFFF), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text("Select System Role Permission:", color = Color(0x88FFFFFF), fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            listOf("Cashier", "Manager", "Admin").forEach { role ->
                                val isSelected = viewModel.regRole == role
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.background(
                                        if (isSelected) Color(0x2210B981) else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    ).padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { viewModel.regRole = role },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = Color(0xFF10B981),
                                            unselectedColor = Color(0x66FFFFFF)
                                        )
                                    )
                                    Text(role, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { viewModel.performSignup() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("signup_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("REGISTER ACCOUNT", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Already have a terminal key?", color = Color(0x99FFFFFF), fontSize = 13.sp)
                        TextButton(onClick = { viewModel.currentScreen = Screen.LOGIN; viewModel.authError = null }) {
                            Text("Login", color = Color(0xFF34D399), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
