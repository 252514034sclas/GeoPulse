package com.geopulse.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geopulse.app.ui.theme.*

class CreateAccountActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeoPulseTheme {
                var fullName by remember { mutableStateOf("") }
                var farmName by remember { mutableStateOf("") }
                var email by remember { mutableStateOf("") }
                var password by remember { mutableStateOf("") }
                var confirmPassword by remember { mutableStateOf("") }
                var passwordVisible by remember { mutableStateOf(false) }
                var errorMessage by remember { mutableStateOf<String?>(null) }

                val isDark = ThemePreferences.isDarkTheme
                val bgBrush = if (isDark) {
                    Brush.radialGradient(
                        colors = listOf(EvergreenDark.copy(alpha = 0.45f), DarkBackground),
                        radius = 1000f
                    )
                } else {
                    Brush.radialGradient(
                        colors = listOf(LightSurfaceElevated, LightBackground),
                        radius = 1000f
                    )
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(bgBrush)
                            .navigationBarsPadding()
                            .statusBarsPadding()
                            .imePadding()
                            .padding(horizontal = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 480.dp),
                            contentPadding = PaddingValues(vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .widthIn(max = 480.dp)
                                        .glassSurface(cornerRadius = 24.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "Create GeoPulse Account",
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Register your agricultural operation",
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                                        )
                                        Spacer(modifier = Modifier.height(20.dp))

                                        OutlinedTextField(
                                            value = fullName,
                                            onValueChange = { fullName = it; errorMessage = null },
                                            label = { Text("Full Name") },
                                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))

                                        OutlinedTextField(
                                            value = farmName,
                                            onValueChange = { farmName = it; errorMessage = null },
                                            label = { Text("Farm / Ranch Name") },
                                            leadingIcon = { Icon(Icons.Default.Agriculture, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))

                                        OutlinedTextField(
                                            value = email,
                                            onValueChange = { email = it; errorMessage = null },
                                            label = { Text("Email Address") },
                                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))

                                        OutlinedTextField(
                                            value = password,
                                            onValueChange = { password = it; errorMessage = null },
                                            label = { Text("Password") },
                                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                            trailingIcon = {
                                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                                    Icon(
                                                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                        contentDescription = null
                                                    )
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                            singleLine = true,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))

                                        OutlinedTextField(
                                            value = confirmPassword,
                                            onValueChange = { confirmPassword = it; errorMessage = null },
                                            label = { Text("Confirm Password") },
                                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                            modifier = Modifier.fillMaxWidth(),
                                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                            singleLine = true,
                                            shape = RoundedCornerShape(12.dp)
                                        )

                                        if (errorMessage != null) {
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Text(
                                                text = errorMessage!!,
                                                color = MaterialTheme.colorScheme.error,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(20.dp))
                                        Button(
                                            onClick = {
                                                if (fullName.isBlank() || farmName.isBlank()) {
                                                    errorMessage = "Please enter your name and farm name."
                                                } else if (email.isBlank() || !email.contains("@")) {
                                                    errorMessage = "Please enter a valid email address."
                                                } else if (password.length < 4) {
                                                    errorMessage = "Password must be at least 4 characters."
                                                } else if (password != confirmPassword) {
                                                    errorMessage = "Passwords do not match."
                                                } else {
                                                    startActivity(Intent(this@CreateAccountActivity, Screen7Activity::class.java))
                                                    finish()
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth().height(52.dp),
                                            shape = RoundedCornerShape(14.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                        ) {
                                            Text(
                                                text = "Complete Registration",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimary
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                        TextButton(onClick = {
                                            startActivity(Intent(this@CreateAccountActivity, LoginActivity::class.java))
                                            finish()
                                        }) {
                                            Text(
                                                text = "Already have an account? Sign In",
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.SemiBold
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
    }
}
