package com.geopulse.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geopulse.app.ui.theme.GeoPulseTheme

class Screen7Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeoPulseTheme {
                Scaffold(
                    topBar = { GeoTopBar(title = "GeoPulse Dashboard") },
                    bottomBar = { GeoBottomBar(currentScreen = 7, activity = this@Screen7Activity) },
                    containerColor = LightBackground
                ) { padding ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Field Status Overview", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = PrimaryDark)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        MetricItem("Soil Moisture", "72%", Icons.Default.WaterDrop)
                                        MetricItem("NDVI Index", "0.84", Icons.Default.Eco)
                                        MetricItem("Crop Health", "Optimal", Icons.Default.CheckCircle)
                                    }
                                }
                            }
                        }

                        item {
                            Text("Quick Features", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PrimaryDark)
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                FeatureCard("Vegetation Health", Icons.Default.Eco, modifier = Modifier.weight(1f)) {
                                    startActivity(Intent(this@Screen7Activity, Screen11Activity::class.java))
                                    finish()
                                }
                                FeatureCard("Soil & Moisture", Icons.Default.Water, modifier = Modifier.weight(1f)) {
                                    startActivity(Intent(this@Screen7Activity, Screen12Activity::class.java))
                                    finish()
                                }
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                FeatureCard("Irrigation Control", Icons.Default.Opacity, modifier = Modifier.weight(1f)) {
                                    startActivity(Intent(this@Screen7Activity, Screen13Activity::class.java))
                                    finish()
                                }
                                FeatureCard("Weather Forecast", Icons.Default.WbSunny, modifier = Modifier.weight(1f)) {
                                    startActivity(Intent(this@Screen7Activity, Screen14Activity::class.java))
                                    finish()
                                }
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                FeatureCard("Pest Detection", Icons.Default.Warning, modifier = Modifier.weight(1f)) {
                                    startActivity(Intent(this@Screen7Activity, Screen15Activity::class.java))
                                    finish()
                                }
                                FeatureCard("Yield Analytics", Icons.Default.TrendingUp, modifier = Modifier.weight(1f)) {
                                    startActivity(Intent(this@Screen7Activity, Screen16Activity::class.java))
                                    finish()
                                }
                            }
                        }

                        item {
                            Button(
                                onClick = {
                                    startActivity(Intent(this@Screen7Activity, Screen19Activity::class.java))
                                    finish()
                                },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                            ) {
                                Text("View All Farm Sensors & Equipment", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricItem(label: String, value: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = label, tint = AccentGreen, modifier = Modifier.size(28.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PrimaryDark)
        Text(label, fontSize = 12.sp, color = Color(0xFF606860))
    }
}

@Composable
fun FeatureCard(title: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = title, tint = AccentGreen, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = PrimaryDark, textAlign = TextAlign.Center)
        }
    }
}
