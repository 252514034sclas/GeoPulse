package com.geopulse.app

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geopulse.app.ui.theme.*

/**
 * Crisp glassmorphic surface modifier adapting dynamically to Dark and Light themes.
 */
@Composable
fun Modifier.glassSurface(
    cornerRadius: Dp = 20.dp,
    backgroundColor: Color? = null,
    borderColor: Color? = null
): Modifier {
    val isDark = ThemePreferences.isDarkTheme
    val bg = backgroundColor ?: if (isDark) DarkSurface.copy(alpha = 0.90f) else LightSurface.copy(alpha = 0.96f)
    val border = borderColor ?: if (isDark) CardBorderDark else CardBorderLight

    return this
        .clip(RoundedCornerShape(cornerRadius))
        .background(bg)
        .border(BorderStroke(1.dp, border), RoundedCornerShape(cornerRadius))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeoTopBar(
    title: String,
    subtitle: String? = null,
    onBackClick: (() -> Unit)? = null,
    onActionClick: (() -> Unit)? = null,
    actionIcon: ImageVector = Icons.Default.Notifications
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 18.sp
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
                    )
                }
            }
        },
        navigationIcon = {
            if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        },
        actions = {
            if (onActionClick != null) {
                IconButton(onClick = onActionClick) {
                    Icon(
                        imageVector = actionIcon,
                        contentDescription = "Action",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

@Composable
fun GeoBottomBar(currentScreen: Int, activity: ComponentActivity) {
    val isDark = ThemePreferences.isDarkTheme
    val barBg = if (isDark) DarkSurface.copy(alpha = 0.95f) else LightSurface.copy(alpha = 0.96f)
    val barBorder = if (isDark) CardBorderDark else CardBorderLight

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.wrapContentWidth(),
            shape = RoundedCornerShape(32.dp),
            color = barBg,
            shadowElevation = 10.dp,
            border = BorderStroke(1.dp, barBorder)
        ) {
            Row(
                modifier = Modifier.padding(vertical = 6.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FloatingNavTab(
                    icon = Icons.Default.Home,
                    label = "Home",
                    isSelected = currentScreen == 7
                ) {
                    if (currentScreen != 7) {
                        activity.startActivity(Intent(activity, Screen7Activity::class.java))
                        activity.finish()
                    }
                }
                FloatingNavTab(
                    icon = Icons.Default.Place,
                    label = "Map",
                    isSelected = currentScreen == 8
                ) {
                    if (currentScreen != 8) {
                        activity.startActivity(Intent(activity, Screen8Activity::class.java))
                        activity.finish()
                    }
                }
                FloatingNavTab(
                    icon = Icons.Default.Assessment,
                    label = "Activity",
                    isSelected = currentScreen == 9
                ) {
                    if (currentScreen != 9) {
                        activity.startActivity(Intent(activity, Screen9Activity::class.java))
                        activity.finish()
                    }
                }
                FloatingNavTab(
                    icon = Icons.Default.Person,
                    label = "Profile",
                    isSelected = currentScreen == 10
                ) {
                    if (currentScreen != 10) {
                        activity.startActivity(Intent(activity, Screen10Activity::class.java))
                        activity.finish()
                    }
                }
            }
        }
    }
}

@Composable
fun FloatingNavTab(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val isDark = ThemePreferences.isDarkTheme
    val selectedBg = if (isDark) EvergreenDark else Color(0xFFE8F5E9)
    val selectedContent = if (isDark) LimeAccent else LeafGreen
    val unselectedContent = if (isDark) TextMuted else TextDarkMuted

    val backgroundColor = if (isSelected) selectedBg else Color.Transparent
    val contentColor = if (isSelected) selectedContent else unselectedContent

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
            if (isSelected) {
                Text(
                    text = label,
                    color = contentColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// -------------------------------------------------------------
// Specialized Interactive Modules for Screens 11 through 30
// -------------------------------------------------------------

@Composable
fun VegetationHealthModule() {
    var ndviThreshold by remember { mutableStateOf(0.84f) }
    Card(
        modifier = Modifier.fillMaxWidth().glassSurface(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Interactive NDVI Health Analysis", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Crop Vigor Index", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 14.sp)
                Text("NDVI ${(ndviThreshold * 100).toInt() / 100f} - Optimal", fontWeight = FontWeight.Bold, color = LimeAccent, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Slider(
                value = ndviThreshold,
                onValueChange = { ndviThreshold = it },
                valueRange = 0.2f..1.0f,
                colors = SliderDefaults.colors(thumbColor = LimeAccent, activeTrackColor = EmeraldPrimary)
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("0.2 Sparse", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                Text("0.5 Moderate", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                Text("1.0 Dense Canopy", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricChip("Chlorophyll", "42.5 SPAD", Modifier.weight(1f))
                MetricChip("Biomass Vigor", "+14.2% YoY", Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun SoilMoistureModule() {
    var selectedDepth by remember { mutableStateOf("10-30cm") }
    Card(
        modifier = Modifier.fillMaxWidth().glassSurface(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Root-Zone Moisture Profile", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("0-10cm", "10-30cm", "30-60cm").forEach { depth ->
                    FilterChip(
                        selected = selectedDepth == depth,
                        onClick = { selectedDepth = depth },
                        label = { Text(depth) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            val moisture = when (selectedDepth) {
                "0-10cm" -> 74
                "10-30cm" -> 68
                else -> 82
            }
            LinearProgressIndicator(
                progress = { moisture / 100f },
                modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                color = CyanAccent,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Field Capacity: 75%", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Text("Current: $moisture% (Target)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
            }
        }
    }
}

@Composable
fun IrrigationControlModule() {
    var pivot1Active by remember { mutableStateOf(true) }
    var pivot2Active by remember { mutableStateOf(false) }
    var flowRate by remember { mutableStateOf(320f) }

    Card(
        modifier = Modifier.fillMaxWidth().glassSurface(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Automated Irrigation Controls", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Center Pivot #01 (North Quadrant)", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text(if (pivot1Active) "Active · Delivering 320 L/min" else "Standby Mode", fontSize = 12.sp, color = if (pivot1Active) EmeraldPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
                Switch(checked = pivot1Active, onCheckedChange = { pivot1Active = it })
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Center Pivot #02 (South Field)", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text(if (pivot2Active) "Active" else "Standby Mode", fontSize = 12.sp, color = if (pivot2Active) EmeraldPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
                Switch(checked = pivot2Active, onCheckedChange = { pivot2Active = it })
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("Flow Rate Adjustment: ${flowRate.toInt()} L/min", fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Slider(
                value = flowRate,
                onValueChange = { flowRate = it },
                valueRange = 100f..500f,
                colors = SliderDefaults.colors(thumbColor = CyanAccent, activeTrackColor = CyanAccent)
            )
        }
    }
}

@Composable
fun WeatherRadarModule() {
    var isSweeping by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth().glassSurface(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Micro-Climate Weather & Doppler", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                Icon(Icons.Default.WbSunny, contentDescription = null, tint = GoldAccent)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("24°C", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text("Clear Sky · Wind 12 km/h NE", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Precipitation: 10%", fontSize = 13.sp, color = CyanAccent, fontWeight = FontWeight.SemiBold)
                    Text("Humidity: 58%", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { isSweeping = !isSweeping },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isSweeping) "Radar Sweep: Active (No Rain Cells)" else "Trigger Radar Sweep", color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun PestDetectionModule() {
    var simulatedScan by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth().glassSurface(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("AI Early Pest & Disease Diagnostics", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0x224CAF50)).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldPrimary)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Threat Index: Low (14%)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = EmeraldPrimary)
                    Text("Soybean Aphid detected in isolated sector 3B only.", fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = { simulatedScan = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.BugReport, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Simulate AI Canopy Health Scan", fontWeight = FontWeight.Bold)
            }
            if (simulatedScan) {
                Spacer(modifier = Modifier.height(10.dp))
                Text("AI Diagnosis: 98.4% healthy canopy. Recommend predatory mite dispersal in Quadrant 3B.", fontSize = 13.sp, color = LimeAccent)
            }
        }
    }
}

@Composable
fun YieldAnalyticsModule() {
    Card(
        modifier = Modifier.fillMaxWidth().glassSurface(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Predictive Harvest Yield", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("4.82 t/ha", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = LimeAccent)
                    Text("Projected Yield (+8.4% YoY)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("520 MT Total", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Text("Estimated \$184k Gross", fontSize = 12.sp, color = EmeraldPrimary)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { 0.88f },
                modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                color = LeafGreen,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text("Progress toward seasonal historical record: 88%", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
    }
}

@Composable
fun SensorNodeModule() {
    var pingActive by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth().glassSurface(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("IoT Node Mesh Health", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                Text("48/50 Online", fontWeight = FontWeight.Bold, color = EmeraldPrimary, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
            listOf("Node #01 - North Pivot (Battery 96%)", "Node #02 - Creek Basin (Battery 91%)", "Node #03 - East Ridge (Battery 88%)").forEach { node ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(node, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                    Icon(Icons.Default.Check, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { pingActive = !pingActive },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Sensors, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (pingActive) "All Nodes Acknowledged (Latency: 28ms)" else "Ping IoT Mesh Gateway", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AgronomistChatModule() {
    var messages by remember { mutableStateOf(listOf("Hello! Dr. Vance here. How can I assist with your crops today?")) }
    var inputText by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth().glassSurface(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text("Certified Agronomist Consultation", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Text("Dr. Elena Vance, Senior Crop Specialist", fontSize = 12.sp, color = EmeraldPrimary)
            Spacer(modifier = Modifier.height(12.dp))
            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)).padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                messages.forEach { msg ->
                    Text(msg, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Ask an agronomy question...", fontSize = 13.sp) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            messages = messages + "Farmer: $inputText" + "Dr. Vance: Looking into your telemetry logs for field 01..."
                            inputText = ""
                        }
                    }
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun MarketCommodityModule() {
    Card(
        modifier = Modifier.fillMaxWidth().glassSurface(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Live Agricultural Commodity Futures", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(12.dp))
            listOf(
                Triple("Corn Futures (ZCH26)", "$4.42 / bu", "+1.8% ▲"),
                Triple("Soybeans (ZSH26)", "$11.85 / bu", "-0.4% ▼"),
                Triple("Winter Wheat (ZWH26)", "$5.60 / bu", "+0.9% ▲")
            ).forEach { (crop, price, change) ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(crop, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(price, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(change, color = if (change.contains("▲")) EmeraldPrimary else RedAccent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun DataPrivacyDeletionModule(activity: ComponentActivity) {
    var showConfirmDelete by remember { mutableStateOf(false) }
    var exportCompleted by remember { mutableStateOf(false) }

    if (showConfirmDelete) {
        AlertDialog(
            onDismissRequest = { showConfirmDelete = false },
            title = { Text("Delete Farm Records & Account?", fontWeight = FontWeight.Bold) },
            text = { Text("This will permanently erase all field boundaries, telemetry logs, and sensor configurations. This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDelete = false
                        activity.startActivity(Intent(activity, LoginActivity::class.java))
                        activity.finish()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Permanently Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDelete = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth().glassSurface(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Farm Data Ownership & Export", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Export your GIS boundary polygons, soil sensor logs, and crop history in open standard formats (GeoJSON and CSV).", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.height(14.dp))
            OutlinedButton(
                onClick = { exportCompleted = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (exportCompleted) "✓ Export Generated (geopulse_export.zip)" else "Generate & Download Data Archive")
            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Danger Zone", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Button(
                onClick = { showConfirmDelete = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Delete Farm Profile & Account", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun FieldBoundaryModule() {
    var editMode by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth().glassSurface(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Field Boundary Editor", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                Switch(checked = editMode, onCheckedChange = { editMode = it })
            }
            Text(if (editMode) "Edit Mode Active: Tap vertices to adjust" else "Viewing Boundary Polygon (Locked)", fontSize = 12.sp, color = if (editMode) EmeraldPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(14.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricChip("Calculated Area", "124.85 ac", Modifier.weight(1f))
                MetricChip("Perimeter", "2,840 m", Modifier.weight(1f))
                MetricChip("Vertices", "6 Points", Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(onClick = { /* Recalculate */ }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Text("Recalculate Area from GPS Points")
            }
        }
    }
}

@Composable
fun MachineryTrackingModule() {
    var vehicleDispatched by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth().glassSurface(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Farm Machinery Telematics", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("John Deere 8R Tractor", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("Implement: 24-Row Planter", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                }
                Text("Active in North Field", color = EmeraldPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricChip("Fuel Level", "82%", Modifier.weight(1f))
                MetricChip("Ground Speed", "14 km/h", Modifier.weight(1f))
                MetricChip("Engine Hours", "1,420 h", Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = { vehicleDispatched = !vehicleDispatched },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (vehicleDispatched) "Recall Tractor to Base" else "Dispatch to Field 02")
            }
        }
    }
}

@Composable
fun FertilizationPlanModule() {
    var nitrogenRate by remember { mutableStateOf(140f) }
    Card(
        modifier = Modifier.fillMaxWidth().glassSurface(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Variable-Rate Nutrient Prescription", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Target Nitrogen (N): ${nitrogenRate.toInt()} kg/ha", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text("Soil pH: 6.4 (Optimal)", fontSize = 12.sp, color = EmeraldPrimary, fontWeight = FontWeight.Bold)
            }
            Slider(value = nitrogenRate, onValueChange = { nitrogenRate = it }, valueRange = 80f..220f, colors = SliderDefaults.colors(thumbColor = LimeAccent, activeTrackColor = EmeraldPrimary))
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricChip("Phosphorus (P)", "55 kg/ha", Modifier.weight(1f))
                MetricChip("Potassium (K)", "70 kg/ha", Modifier.weight(1f))
                MetricChip("Target ROI", "+18.5%", Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun HarvestLogisticsModule() {
    Card(
        modifier = Modifier.fillMaxWidth().glassSurface(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Silo Storage & Fleet Dispatch", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(12.dp))
            Text("Grain Silo Alpha (Corn): 82% Full (4,100 / 5,000 bu)", fontSize = 13.sp)
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(progress = { 0.82f }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)), color = GoldAccent)
            Spacer(modifier = Modifier.height(12.dp))
            Text("Grain Silo Beta (Soybeans): 45% Full (2,250 / 5,000 bu)", fontSize = 13.sp)
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(progress = { 0.45f }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)), color = EmeraldPrimary)
            Spacer(modifier = Modifier.height(14.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Next Delivery Truck", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                Text("Arriving in 25 mins", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
            }
        }
    }
}

@Composable
fun FinancialSummaryModule() {
    Card(
        modifier = Modifier.fillMaxWidth().glassSurface(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Seasonal Revenue & Cost Projections", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("\$184,200", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                    Text("Gross Projected Revenue", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("\$62,400", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = RedAccent)
                    Text("Total Input Expenses", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricChip("Net Margin", "+66.1%", Modifier.weight(1f))
                MetricChip("Cost / Acre", "\$148.50", Modifier.weight(1f))
                MetricChip("Breakeven Corn", "\$3.40/bu", Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun MultiFarmModule() {
    var selectedFarm by remember { mutableStateOf("Green Valley") }
    Card(
        modifier = Modifier.fillMaxWidth().glassSurface(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Multi-Farm Regional Portfolio", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Green Valley", "High Plains", "River Basin").forEach { farm ->
                    FilterChip(
                        selected = selectedFarm == farm,
                        onClick = { selectedFarm = farm },
                        label = { Text(farm) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            when (selectedFarm) {
                "Green Valley" -> Text("Property: Green Valley (1,250 Acres) · 8 Active Fields · Manager: Johnathan Doe", fontSize = 13.sp)
                "High Plains" -> Text("Property: High Plains (820 Acres) · 5 Active Fields · Manager: Sarah Jenkins", fontSize = 13.sp)
                else -> Text("Property: River Basin (410 Acres) · 3 Active Fields · Manager: Mike Miller", fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricChip("Total Land", "2,480 ac", Modifier.weight(1f))
                MetricChip("Total Sensors", "128 Nodes", Modifier.weight(1f))
                MetricChip("Regional Status", "Optimal", Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun DroneScanModule() {
    var scanning by remember { mutableStateOf(false) }
    var scanProgress by remember { mutableStateOf(0f) }

    LaunchedEffect(scanning) {
        if (scanning) {
            scanProgress = 0f
            while (scanProgress < 1f) {
                kotlinx.coroutines.delay(100)
                scanProgress += 0.05f
            }
            scanning = false
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth().glassSurface(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Autonomous Multispectral Aerial Drone Scan", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("UAV: DJI Matrice 300 RTK", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text("Battery: 98%", fontWeight = FontWeight.Bold, color = EmeraldPrimary, fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricChip("Flight Alt", "120 m", Modifier.weight(1f))
                MetricChip("GSD", "2.4 cm/px", Modifier.weight(1f))
                MetricChip("Coverage", "120 Acres", Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(14.dp))
            if (scanning) {
                Text("Mission in Progress: ${(scanProgress * 100).toInt()}% completed", fontSize = 12.sp, color = LimeAccent)
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(progress = { scanProgress }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)), color = LimeAccent)
            } else {
                Button(
                    onClick = { scanning = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.AirplanemodeActive, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (scanProgress >= 1f) "Scan Complete (NDVI Ready) · Relaunch" else "Launch Autonomous Flight Scan", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CarbonSustainabilityModule() {
    Card(
        modifier = Modifier.fillMaxWidth().glassSurface(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Soil Carbon Sequestration & ESG Credits", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("42.8 MT", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                    Text("CO2e Sequestered This Season", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("\$1,712.00", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = LimeAccent)
                    Text("Verified Carbon Credits", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricChip("Cover Crops", "Active (100%)", Modifier.weight(1f))
                MetricChip("Tillage", "No-Till (85%)", Modifier.weight(1f))
                MetricChip("Organic Matter", "3.8% (+0.4%)", Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun NotificationCenterModule() {
    var notifications by remember {
        mutableStateOf(
            listOf(
                Triple("Frost Warning Advisory", "Sub-zero temperatures expected at 04:00 AM", RedAccent),
                Triple("Soil Moisture Deficit", "Quadrant 3 needs 15mm irrigation cycle", GoldAccent),
                Triple("Satellite Pass Complete", "New Sentinel-2 NDVI multispectral imagery loaded", EmeraldPrimary)
            )
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth().glassSurface(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Audit Alerts (${notifications.size})", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                if (notifications.isNotEmpty()) {
                    TextButton(onClick = { notifications = emptyList() }) {
                        Text("Clear All", fontSize = 12.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (notifications.isEmpty()) {
                Text("All alerts cleared. Telemetry nominal.", fontSize = 13.sp, color = EmeraldPrimary)
            } else {
                notifications.forEach { (title, desc, color) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text(desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppSettingsSyncModule() {
    var syncInterval by remember { mutableStateOf("15 Mins") }
    var useMetric by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().glassSurface(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("App Sync & Local Configurations", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(12.dp))
            Text("Cloud Telemetry Sync Frequency", fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Real-Time", "15 Mins", "Hourly").forEach { interval ->
                    FilterChip(
                        selected = syncInterval == interval,
                        onClick = { syncInterval = interval },
                        label = { Text(interval) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Measurement Units", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text(if (useMetric) "Metric (Hectares, °C, Liters)" else "Imperial (Acres, °F, Gallons)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
                Switch(checked = useMetric, onCheckedChange = { useMetric = it })
            }
        }
    }
}

@Composable
fun GenericTelemetryDiagnosticsModule() {
    Card(
        modifier = Modifier.fillMaxWidth().glassSurface(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Telemetry & Real-Time Diagnostics", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Operational Status", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 14.sp)
                Text("Optimal & Synchronized", fontWeight = FontWeight.SemiBold, color = EmeraldPrimary, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Cloud Telemetry", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 14.sp)
                Text("Live Feed Active", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Mesh Latency", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 14.sp)
                Text("24 ms (LoRaWAN)", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun MetricChip(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

// -------------------------------------------------------------
// GeoDetailScreenContent: Scaffold hosting the rich interactive components
// -------------------------------------------------------------

@Composable
fun GeoDetailScreenContent(
    title: String,
    subtitle: String,
    icon: ImageVector,
    activity: ComponentActivity,
    nextActivityClass: Class<*>? = null,
    nextButtonText: String = "Proceed / Next"
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                GeoTopBar(
                    title = title,
                    onBackClick = {
                        activity.startActivity(Intent(activity, Screen7Activity::class.java))
                        activity.finish()
                    }
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                val isTablet = maxWidth > 600.dp
                val horizontalPadding = if (isTablet) 32.dp else 16.dp
                val heroGradient = remember {
                    Brush.linearGradient(colors = listOf(PineDark, EvergreenDark, ForestDark))
                }

                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(animationSpec = tween(400)) + slideInVertically(animationSpec = tween(400), initialOffsetY = { 30 })
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = horizontalPadding, vertical = 12.dp),
                        contentPadding = PaddingValues(bottom = 90.dp), // Leaves room for floating bottom bar
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Hero Visual Card
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .then(if (isTablet) Modifier.widthIn(max = 800.dp) else Modifier)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(heroGradient)
                                    .border(1.dp, EmeraldPrimary.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                                    .padding(if (isTablet) 28.dp else 20.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = Color.White.copy(alpha = 0.15f),
                                        modifier = Modifier.size(64.dp),
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = title,
                                                tint = LimeAccent,
                                                modifier = Modifier.size(34.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text(
                                        text = title,
                                        fontSize = if (isTablet) 24.sp else 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = subtitle,
                                        fontSize = if (isTablet) 15.sp else 13.sp,
                                        color = Color.White.copy(alpha = 0.85f),
                                        textAlign = TextAlign.Center,
                                        lineHeight = 20.sp
                                    )
                                }
                            }
                        }

                        // Domain Interactive Module
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .then(if (isTablet) Modifier.widthIn(max = 800.dp) else Modifier)
                            ) {
                                when {
                                    title.contains("Vegetation", ignoreCase = true) -> VegetationHealthModule()
                                    title.contains("Soil Moisture", ignoreCase = true) -> SoilMoistureModule()
                                    title.contains("Irrigation", ignoreCase = true) -> IrrigationControlModule()
                                    title.contains("Weather", ignoreCase = true) || title.contains("Radar", ignoreCase = true) -> WeatherRadarModule()
                                    title.contains("Pest", ignoreCase = true) -> PestDetectionModule()
                                    title.contains("Yield", ignoreCase = true) -> YieldAnalyticsModule()
                                    title.contains("Boundary", ignoreCase = true) -> FieldBoundaryModule()
                                    title.contains("Sensor", ignoreCase = true) || title.contains("Node", ignoreCase = true) -> SensorNodeModule()
                                    title.contains("Machinery", ignoreCase = true) || title.contains("Tractor", ignoreCase = true) -> MachineryTrackingModule()
                                    title.contains("Fertilization", ignoreCase = true) || title.contains("Nutrient", ignoreCase = true) -> FertilizationPlanModule()
                                    title.contains("Harvest", ignoreCase = true) || title.contains("Logistics", ignoreCase = true) -> HarvestLogisticsModule()
                                    title.contains("Financial", ignoreCase = true) || title.contains("ROI", ignoreCase = true) -> FinancialSummaryModule()
                                    title.contains("Portfolio", ignoreCase = true) || title.contains("Multi-Farm", ignoreCase = true) -> MultiFarmModule()
                                    title.contains("Drone", ignoreCase = true) || title.contains("Multispectral", ignoreCase = true) -> DroneScanModule()
                                    title.contains("Carbon", ignoreCase = true) || title.contains("Sustainability", ignoreCase = true) -> CarbonSustainabilityModule()
                                    title.contains("Agronomist", ignoreCase = true) || title.contains("Chat", ignoreCase = true) -> AgronomistChatModule()
                                    title.contains("Market", ignoreCase = true) || title.contains("Commodity", ignoreCase = true) -> MarketCommodityModule()
                                    title.contains("Notification", ignoreCase = true) -> NotificationCenterModule()
                                    title.contains("Sync", ignoreCase = true) || title.contains("Settings", ignoreCase = true) -> AppSettingsSyncModule()
                                    title.contains("Privacy", ignoreCase = true) || title.contains("Deletion", ignoreCase = true) -> DataPrivacyDeletionModule(activity)
                                    else -> GenericTelemetryDiagnosticsModule()
                                }
                            }
                        }

                        // Navigation Buttons
                        item {
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .then(if (isTablet) Modifier.widthIn(max = 800.dp) else Modifier),
                                contentAlignment = Alignment.Center
                            ) {
                                Button(
                                    onClick = {
                                        val target = nextActivityClass ?: Screen7Activity::class.java
                                        activity.startActivity(Intent(activity, target))
                                        activity.finish()
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Text(nextButtonText, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                                }
                            }
                        }

                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .then(if (isTablet) Modifier.widthIn(max = 800.dp) else Modifier),
                                contentAlignment = Alignment.Center
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        activity.startActivity(Intent(activity, Screen7Activity::class.java))
                                        activity.finish()
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                                ) {
                                    Text("Return to Dashboard", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Decoupled floating bottom navigation bar overlay
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            GeoBottomBar(currentScreen = 7, activity = activity)
        }
    }
}
