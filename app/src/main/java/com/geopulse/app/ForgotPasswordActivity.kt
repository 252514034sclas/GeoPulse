package com.geopulse.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geopulse.app.ui.theme.GeoPulseTheme

class ForgotPasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeoPulseTheme {
                var email by remember { mutableStateOf("") }
                var submitted by remember { mutableStateOf(false) }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = LightBackground
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Reset Password",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryDark
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Enter your email address and we'll send you instructions to reset your password.",
                            fontSize = 14.sp,
                            color = Color(0xFF505850),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(32.dp))

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { submitted = true },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                        ) {
                            Text("Send Reset Link", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }

                        if (submitted) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Reset link sent if email exists.", color = AccentGreen, fontWeight = FontWeight.Medium)
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        TextButton(onClick = {
                            startActivity(Intent(this@ForgotPasswordActivity, LoginActivity::class.java))
                            finish()
                        }) {
                            Text("Back to Login", color = PrimaryDark)
                        }
                    }
                }
            }
        }
    }
}
