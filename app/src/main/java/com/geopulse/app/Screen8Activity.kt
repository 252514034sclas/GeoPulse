package com.geopulse.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geopulse.app.ui.theme.GeoPulseTheme

class Screen8Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeoPulseTheme {
                Scaffold(
                    topBar = { GeoTopBar(title = "Geospatial Map", onActionClick = {}, actionIcon = Icons.Default.Layers) },
                    bottomBar = { GeoBottomBar(currentScreen = 8, activity = this@Screen8Activity) },
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
                                modifier = Modifier.fillMaxWidth().height(220.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = PrimaryDark)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(16.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.Place, contentDescription = "Map View", tint = AccentGreen, modifier = Modifier.size(64.dp))
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("Interactive Farm Boundary Map", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                    Text("Lat: 37.7749° N, Lon: 122.4194° W", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                                }
                            }
                        }

                        item {
                            Text("Active Farm Zones", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PrimaryDark)
                        }

                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("North Sector - Corn & Soybeans", fontWeight = FontWeight.Bold, color = PrimaryDark)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Size: 120 Acres | Status: Monitored", fontSize = 13.sp, color = Color(0xFF606860))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            startActivity(Intent(this@Screen8Activity, Screen15Activity::class.java))
                                            finish()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                                    ) {
                                        Text("Inspect Zone Alerts")
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
