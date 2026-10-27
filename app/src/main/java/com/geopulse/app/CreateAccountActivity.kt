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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geopulse.app.ui.theme.GeoPulseTheme

class CreateAccountActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeoPulseTheme {
                var name by remember { mutableStateOf("") }
                var email by remember { mutableStateOf("") }
                var password by remember { mutableStateOf("") }

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
                            text = "Create Account",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryDark
                        )
                        Spacer(modifier = Modifier.height(32.dp))

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Full Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = {
                                startActivity(Intent(this@CreateAccountActivity, Screen7Activity::class.java))
                                finish()
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                        ) {
                            Text("Sign Up", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        TextButton(onClick = {
                            startActivity(Intent(this@CreateAccountActivity, LoginActivity::class.java))
                            finish()
                        }) {
                            Text("Already have an account? Sign In", color = PrimaryDark)
                        }
                    }
                }
            }
        }
    }
}
