package com.geopulse.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geopulse.app.ui.theme.*

data class FarmZone(
    val id: String,
    val name: String,
    val acreage: String,
    val crop: String,
    val ndvi: String,
    val moisture: String,
    val status: String,
    val offsetFraction: Offset
)

class Screen8Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeoPulseTheme {
                val zones = remember {
                    listOf(
                        FarmZone("1", "North Sector Alpha", "120.4 Acres", "Corn (Dent)", "0.84 (Optimal)", "72%", "Healthy", Offset(0.35f, 0.35f)),
                        FarmZone("2", "East Field Beta", "84.2 Acres", "Soybeans", "0.78 (Good)", "65%", "Monitored", Offset(0.68f, 0.42f)),
                        FarmZone("3", "South Center Pivot", "145.0 Acres", "Winter Wheat", "0.62 (Moderate)", "48%", "Needs Water", Offset(0.48f, 0.68f))
                    )
                }

                var selectedLayer by remember { mutableStateOf("NDVI Heatmap") }
                var selectedZone by remember { mutableStateOf(zones[0]) }
                var zoomLevel by remember { mutableStateOf(1.0f) }

                Box(modifier = Modifier.fillMaxSize()) {
                    Scaffold(
                        topBar = {
                            GeoTopBar(
                                title = "Geospatial Field Map",
                                subtitle = "GPS: 37.7749° N, 122.4194° W · 3 Zones",
                                onActionClick = { /* Recenter */ },
                                actionIcon = Icons.Default.MyLocation
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
                            // Layer Switcher Chips
                            item {
                                LazyRow(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(listOf("NDVI Heatmap", "Soil Moisture", "Satellite RGB", "Topography")) { layer ->
                                        FilterChip(
                                            selected = selectedLayer == layer,
                                            onClick = { selectedLayer = layer },
                                            label = { Text(layer, fontWeight = FontWeight.SemiBold) },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = when (layer) {
                                                        "NDVI Heatmap" -> Icons.Default.Eco
                                                        "Soil Moisture" -> Icons.Default.Water
                                                        "Satellite RGB" -> Icons.Default.Layers
                                                        else -> Icons.Default.Terrain
                                                    },
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        )
                                    }
                                }
                            }

                            // Interactive GIS Vector Map Canvas
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(290.dp)
                                        .padding(horizontal = 16.dp)
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(if (ThemePreferences.isDarkTheme) Color(0xFF0B1910) else Color(0xFFE4EDE5))
                                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                                ) {
                                    // Custom Map Renderer
                                    val isDark = ThemePreferences.isDarkTheme
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        val w = size.width
                                        val h = size.height

                                        // Background grid lines
                                        val gridColor = if (isDark) Color(0x15FFFFFF) else Color(0x15000000)
                                        val step = 40f
                                        var x = 0f
                                        while (x < w) {
                                            drawLine(gridColor, Offset(x, 0f), Offset(x, h), strokeWidth = 1f)
                                            x += step
                                        }
                                        var y = 0f
                                        while (y < h) {
                                            drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 1f)
                                            y += step
                                        }

                                        // Parcel 1: North Sector Alpha Polygon
                                        val path1 = Path().apply {
                                            moveTo(w * 0.15f, h * 0.15f)
                                            lineTo(w * 0.55f, h * 0.12f)
                                            lineTo(w * 0.50f, h * 0.48f)
                                            lineTo(w * 0.18f, h * 0.45f)
                                            close()
                                        }
                                        val fill1 = when (selectedLayer) {
                                            "NDVI Heatmap" -> Color(0x884CAF50)
                                            "Soil Moisture" -> Color(0x8800BCD4)
                                            else -> Color(0x772E7D32)
                                        }
                                        drawPath(path1, fill1)
                                        drawPath(path1, if (selectedZone.id == "1") Color.White else EmeraldPrimary, style = Stroke(width = if (selectedZone.id == "1") 3.5f else 1.5f))

                                        // Parcel 2: East Field Beta Polygon
                                        val path2 = Path().apply {
                                            moveTo(w * 0.58f, h * 0.18f)
                                            lineTo(w * 0.88f, h * 0.22f)
                                            lineTo(w * 0.85f, h * 0.55f)
                                            lineTo(w * 0.54f, h * 0.52f)
                                            close()
                                        }
                                        val fill2 = when (selectedLayer) {
                                            "NDVI Heatmap" -> Color(0x888BC34A)
                                            "Soil Moisture" -> Color(0x8803A9F4)
                                            else -> Color(0x77388E3C)
                                        }
                                        drawPath(path2, fill2)
                                        drawPath(path2, if (selectedZone.id == "2") Color.White else EmeraldPrimary, style = Stroke(width = if (selectedZone.id == "2") 3.5f else 1.5f))

                                        // Parcel 3: South Center Pivot Circle
                                        val pivotCenter = Offset(w * 0.48f, h * 0.72f)
                                        val pivotRadius = w * 0.22f
                                        val fill3 = when (selectedLayer) {
                                            "NDVI Heatmap" -> Color(0x88FFC107)
                                            "Soil Moisture" -> Color(0x88FF9800)
                                            else -> Color(0x77558B2F)
                                        }
                                        drawCircle(fill3, radius = pivotRadius, center = pivotCenter)
                                        drawCircle(if (selectedZone.id == "3") Color.White else LimeAccent, radius = pivotRadius, center = pivotCenter, style = Stroke(width = if (selectedZone.id == "3") 3.5f else 1.5f))
                                        drawLine(Color.White.copy(alpha = 0.7f), pivotCenter, Offset(pivotCenter.x + pivotRadius * 0.7f, pivotCenter.y - pivotRadius * 0.7f), strokeWidth = 2f)
                                    }

                                    // Floating Map Layer Controls
                                    Column(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                            shadowElevation = 4.dp,
                                            modifier = Modifier.size(38.dp).clickable { zoomLevel = (zoomLevel + 0.2f).coerceAtMost(2f) }
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(Icons.Default.Add, contentDescription = "Zoom In", modifier = Modifier.size(20.dp))
                                            }
                                        }
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                            shadowElevation = 4.dp,
                                            modifier = Modifier.size(38.dp).clickable { zoomLevel = (zoomLevel - 0.2f).coerceAtLeast(0.8f) }
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(Icons.Default.Remove, contentDescription = "Zoom Out", modifier = Modifier.size(20.dp))
                                            }
                                        }
                                    }

                                    // Interactive Pin Selector Buttons on Map
                                    zones.forEach { zone ->
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(
                                                    start = (zone.offsetFraction.x * 260).dp,
                                                    top = (zone.offsetFraction.y * 220).dp
                                                )
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = if (selectedZone.id == zone.id) LimeAccent else PineDark.copy(alpha = 0.85f),
                                                border = BorderStroke(1.dp, if (selectedZone.id == zone.id) Color.White else LimeAccent),
                                                shadowElevation = 6.dp,
                                                modifier = Modifier.clickable { selectedZone = zone }
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Place,
                                                        contentDescription = null,
                                                        tint = if (selectedZone.id == zone.id) PineDark else LimeAccent,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Text(
                                                        text = zone.name.split(" ")[0],
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (selectedZone.id == zone.id) PineDark else Color.White
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Selected Zone Telemetry Card
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .glassSurface(cornerRadius = 20.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                                ) {
                                    Column(modifier = Modifier.padding(20.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = selectedZone.name,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 18.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "${selectedZone.acreage} · ${selectedZone.crop}",
                                                    fontSize = 13.sp,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                                                )
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = when (selectedZone.status) {
                                                    "Healthy" -> EmeraldPrimary.copy(alpha = 0.2f)
                                                    "Monitored" -> GoldAccent.copy(alpha = 0.2f)
                                                    else -> RedAccent.copy(alpha = 0.2f)
                                                }
                                            ) {
                                                Text(
                                                    text = selectedZone.status,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = when (selectedZone.status) {
                                                        "Healthy" -> EmeraldPrimary
                                                        "Monitored" -> GoldAccent
                                                        else -> RedAccent
                                                    }
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            MetricChip("Canopy NDVI", selectedZone.ndvi, Modifier.weight(1f))
                                            MetricChip("Soil Moisture", selectedZone.moisture, Modifier.weight(1f))
                                            MetricChip("Active Implement", "Pivot 01", Modifier.weight(1f))
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        // Quick Navigation Actions
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = {
                                                    startActivity(Intent(this@Screen8Activity, Screen11Activity::class.java))
                                                    finish()
                                                },
                                                modifier = Modifier.weight(1f).height(46.dp),
                                                shape = RoundedCornerShape(12.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                            ) {
                                                Text("Vegetation", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                            }
                                            OutlinedButton(
                                                onClick = {
                                                    startActivity(Intent(this@Screen8Activity, Screen17Activity::class.java))
                                                    finish()
                                                },
                                                modifier = Modifier.weight(1f).height(46.dp),
                                                shape = RoundedCornerShape(12.dp),
                                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                            ) {
                                                Text("Edit Boundary", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
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
                        GeoBottomBar(currentScreen = 8, activity = this@Screen8Activity)
                    }
                }
            }
        }
    }
}
