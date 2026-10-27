package com.geopulse.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geopulse.app.ui.theme.GeoPulseTheme

class WelcomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeoPulseTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = LightBackground
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Spacer(modifier = Modifier.height(48.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Agriculture,
                                contentDescription = "Welcome Icon",
                                tint = AccentGreen,
                                modifier = Modifier.size(96.dp)
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "Welcome to GeoPulse",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryDark
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Monitor crop health, analyze soil moisture, track field activity, and optimize farm yields with real-time geospatial intelligence.",
                                fontSize = 16.sp,
                                color = Color(0xFF505850),
                                textAlign = TextAlign.Center
                            )
                        }

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    startActivity(Intent(this@WelcomeActivity, OnboardingActivity::class.java))
                                    finish()
                                },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                            ) {
                                Text("Get Started", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = {
                                    startActivity(Intent(this@WelcomeActivity, LoginActivity::class.java))
                                    finish()
                                },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryDark)
                            ) {
                                Text("Sign In", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
