package com.geopulse.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
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

class OnboardingActivity : ComponentActivity() {
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
                        GeoTopBar(
                            title = "Onboarding",
                            onBackClick = {
                                startActivity(Intent(this@OnboardingActivity, WelcomeActivity::class.java))
                                finish()
                            }
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = "Onboarding Map",
                                tint = AccentGreen,
                                modifier = Modifier.size(80.dp)
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "Precision Field Mapping",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryDark
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Map your farm boundaries, set monitoring zones, and receive instant alerts on soil and weather changes.",
                                fontSize = 15.sp,
                                color = Color(0xFF505850),
                                textAlign = TextAlign.Center
                            )
                        }

                        Button(
                            onClick = {
                                startActivity(Intent(this@OnboardingActivity, LoginActivity::class.java))
                                finish()
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                        ) {
                            Text("Continue to Login", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
