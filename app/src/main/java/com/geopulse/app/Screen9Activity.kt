package com.geopulse.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geopulse.app.ui.theme.*

data class ActivityEvent(
    val id: String,
    val title: String,
    val subtitle: String,
    val timestamp: String,
    val category: String,
    val statusColor: Color,
    val icon: ImageVector,
    val targetClass: Class<*>
)

class Screen9Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeoPulseTheme {
                val allEvents = remember {
                    listOf(
                        ActivityEvent("1", "Moisture Deficit in South Pivot", "Root-zone dropped to 48% (Threshold: 55%)", "12m ago", "Alerts", RedAccent, Icons.Default.Warning, Screen13Activity::class.java),
                        ActivityEvent("2", "Center Pivot #01 Cycle Completed", "Delivered 18,450 Liters across North Quadrant", "45m ago", "Irrigation", EmeraldPrimary, Icons.Default.CheckCircle, Screen13Activity::class.java),
                        ActivityEvent("3", "Sensor Node #04 Battery Low", "Battery at 18%, LoRaWAN signal nominal", "1h ago", "Sensors", GoldAccent, Icons.Default.Sensors, Screen18Activity::class.java),
                        ActivityEvent("4", "Drone Multispectral Scan Processed", "NDVI 0.84 calculated for East Field Beta", "3h ago", "Sensors", CyanAccent, Icons.Default.Eco, Screen24Activity::class.java),
                        ActivityEvent("5", "Tractor 8R Planter Finished", "Covered 64 acres in North Sector. Fuel at 82%", "5h ago", "Machinery", EmeraldPrimary, Icons.Default.Agriculture, Screen19Activity::class.java)
                    )
                }

                var selectedCategory by remember { mutableStateOf("All") }
                val filteredEvents = remember(selectedCategory) {
                    if (selectedCategory == "All") allEvents
                    else allEvents.filter { it.category.contains(selectedCategory, ignoreCase = true) }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    Scaffold(
                        topBar = {
                            GeoTopBar(
                                title = "Activity & Telemetry",
                                subtitle = "Live IoT Telemetry & Field Audit",
                                onActionClick = { /* Refresh */ },
                                actionIcon = Icons.Default.Refresh
                            )
                        },
                        containerColor = MaterialTheme.colorScheme.background
                    ) { padding ->
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding),
                            contentPadding = PaddingValues(bottom = 90.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Telemetry KPI Metric Row
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    MetricChip("Active Sensors", "48 / 50", Modifier.weight(1f))
                                    MetricChip("LoRa Gateway", "99.8% Live", Modifier.weight(1f))
                                    MetricChip("Active Alerts", "1 Warning", Modifier.weight(1f))
                                }
                            }

                            // Category Filter Chips
                            item {
                                LazyRow(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(listOf("All", "Alerts", "Irrigation", "Sensors", "Machinery")) { category ->
                                        FilterChip(
                                            selected = selectedCategory == category,
                                            onClick = { selectedCategory = category },
                                            label = { Text(category, fontWeight = FontWeight.SemiBold) }
                                        )
                                    }
                                }
                            }

                            // Section Title
                            item {
                                PaddingValues(horizontal = 16.dp)
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Audit Log (${filteredEvents.size} events)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Text(
                                        text = "Tap to inspect",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                    )
                                }
                            }

                            // Filtered Events
                            items(filteredEvents) { event ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .glassSurface(cornerRadius = 18.dp)
                                        .clickable {
                                            startActivity(Intent(this@Screen9Activity, event.targetClass))
                                            finish()
                                        },
                                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(CircleShape)
                                                .background(event.statusColor.copy(alpha = 0.16f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = event.icon,
                                                contentDescription = null,
                                                tint = event.statusColor,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = event.title,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = event.timestamp,
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = event.subtitle,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                                lineHeight = 16.sp
                                            )
                                        }

                                        Icon(
                                            imageVector = Icons.Default.ChevronRight,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Decoupled floating bottom navigation bar
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                    ) {
                        GeoBottomBar(currentScreen = 9, activity = this@Screen9Activity)
                    }
                }
            }
        }
    }
}
