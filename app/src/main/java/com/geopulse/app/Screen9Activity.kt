package com.geopulse.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geopulse.app.ui.theme.GeoPulseTheme

class Screen9Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeoPulseTheme {
                Scaffold(
                    topBar = { GeoTopBar(title = "Activity & Telemetry") },
                    bottomBar = { GeoBottomBar(currentScreen = 9, activity = this@Screen9Activity) },
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
                            Text("Recent Telemetry Logs", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PrimaryDark)
                        }
                        items(5) { index ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Sensor Cluster #${index + 1}", fontWeight = FontWeight.Bold, color = PrimaryDark)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("Soil Moisture optimal at 74%", fontSize = 13.sp, color = Color(0xFF606860))
                                    }
                                    Text("2h ago", fontSize = 12.sp, color = AccentGreen)
                                }
                            }
                        }
                        item {
                            Button(
                                onClick = {
                                    startActivity(Intent(this@Screen9Activity, Screen11Activity::class.java))
                                    finish()
                                },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                            ) {
                                Text("View Detailed Reports", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
