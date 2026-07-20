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
import com.example.ui.components.getSchemeColors

@Composable
fun AuthScreen(viewModel: POSViewModel) {
    val isLogin = viewModel.currentScreen == Screen.LOGIN
    val colors = getSchemeColors(viewModel.activeColorScheme, viewModel.isDarkMode)

    // Premium ambient space-dark gradient background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A), // Slate 900
                        Color(0xFF020617), // Slate 950
                        Color(0xFF000000)  // Pitch Black
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Decorative radial glow lights
        Box(
            modifier = Modifier
                .size(350.dp)
                .align(Alignment.TopStart)
                .offset(x = (-120).dp, y = (-60).dp)
                .background(Brush.radialGradient(listOf(colors.primary.copy(alpha = 0.25f), Color.Transparent)))
        )
        Box(
            modifier = Modifier
                .size(350.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 120.dp, y = 120.dp)
                .background(Brush.radialGradient(listOf(colors.primary.copy(alpha = 0.15f), Color.Transparent)))
        )

        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp)
                .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(24.dp))
                .testTag("auth_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xEA0F172A) // Glassmorphic translucent slate
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Administrative Keyhole Badge
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.primary)
                        .border(1.dp, Color(0x44FFFFFF), RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = "Console Identity Logo",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = viewModel.devName,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Authorized Admin Console",
                    fontSize = 12.sp,
                    color = colors.primary,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Unlock access to modify skill mastery levels, manage active services, or respond to client lead generation vaults.",
                    fontSize = 10.sp,
                    color = Color.LightGray.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    lineHeight = 14.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Error message banner
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
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
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
                        label = { Text("Admin Username", color = Color(0x88FFFFFF)) },
                        placeholder = { Text("e.g., admin", color = Color(0x44FFFFFF)) },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = colors.primary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = Color(0x33FFFFFF),
                            focusedLabelColor = colors.primary,
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
                        label = { Text("Admin Password", color = Color(0x88FFFFFF)) },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = colors.primary) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle password visibility",
                                    tint = colors.primary
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = Color(0x33FFFFFF),
                            focusedLabelColor = colors.primary,
                            unfocusedLabelColor = Color(0x88FFFFFF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("password_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

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
                                    checkedColor = colors.primary,
                                    checkmarkColor = Color.White,
                                    uncheckedColor = Color(0x66FFFFFF)
                                )
                            )
                            Text("Stay Logged In", color = Color.White, fontSize = 11.sp)
                        }
                        TextButton(onClick = { viewModel.authError = "Hint: Access is pre-seeded with admin / admin." }) {
                            Text("Need Hint?", color = colors.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { viewModel.performLogin() },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("login_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("LOGIN TO CONSOLE", fontWeight = FontWeight.Black, fontSize = 14.sp, color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("No admin credentials?", color = Color(0x99FFFFFF), fontSize = 12.sp)
                        TextButton(onClick = { viewModel.currentScreen = Screen.SIGNUP; viewModel.authError = null }) {
                            Text("Register Account", color = colors.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                } else {
                    // SIGNUP FIELDS
                    OutlinedTextField(
                        value = viewModel.regDisplayName,
                        onValueChange = { viewModel.regDisplayName = it },
                        label = { Text("Display Name", color = Color(0x88FFFFFF)) },
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = colors.primary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
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
                        label = { Text("Pick Username", color = Color(0x88FFFFFF)) },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = colors.primary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
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
                        label = { Text("Set Password", color = Color(0x88FFFFFF)) },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = colors.primary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = Color(0x33FFFFFF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("reg_password")
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { viewModel.performSignup() },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("signup_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("CREATE ADMIN KEY", fontWeight = FontWeight.Black, fontSize = 14.sp, color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Already registered?", color = Color(0x99FFFFFF), fontSize = 12.sp)
                        TextButton(onClick = { viewModel.currentScreen = Screen.LOGIN; viewModel.authError = null }) {
                            Text("Login", color = colors.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
