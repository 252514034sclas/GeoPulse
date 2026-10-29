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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import com.geopulse.app.network.*
import com.geopulse.app.ui.premium.*
import com.geopulse.app.ui.theme.*
import kotlinx.coroutines.launch

class Screen7Activity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val initialTab = intent?.getIntExtra("TARGET_TAB", 0)?.coerceIn(0, 3) ?: 0

        setContent {
            GeoPulseTheme {
                val pagerState = rememberPagerState(initialPage = initialTab, pageCount = { 4 })
                val coroutineScope = rememberCoroutineScope()
                var showLogoutDialog by remember { mutableStateOf(false) }
                val isDark = ThemePreferences.isDarkTheme

                if (showLogoutDialog) {
                    AlertDialog(
                        onDismissRequest = { showLogoutDialog = false },
                        title = { Text("Sign Out", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
                        text = { Text("Are you sure you want to sign out of GeoPulse?", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)) },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showLogoutDialog = false
                                    startActivity(Intent(this@Screen7Activity, LoginActivity::class.java))
                                    finish()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Sign Out")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showLogoutDialog = false }) {
                                Text("Cancel", color = MaterialTheme.colorScheme.primary)
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .statusBarsPadding()
                ) {
                    // Connected screen content transitions in sync with dragging
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        userScrollEnabled = true
                    ) { page ->
                        when (page) {
                            0 -> {
                                PremiumHomeScreen(
                                    isDarkTheme = isDark,
                                    onFieldMapClick = {
                                        coroutineScope.launch { pagerState.animateScrollToPage(1) }
                                    },
                                    onVegetationHealthClick = {
                                        startActivity(Intent(this@Screen7Activity, Screen11Activity::class.java))
                                    },
                                    onSoilMoistureClick = {
                                        startActivity(Intent(this@Screen7Activity, Screen12Activity::class.java))
                                    },
                                    onWeatherClick = {
                                        startActivity(Intent(this@Screen7Activity, Screen14Activity::class.java))
                                    },
                                    onActionClick = {
                                        startActivity(Intent(this@Screen7Activity, Screen20Activity::class.java))
                                    },
                                    onProfileClick = {
                                        coroutineScope.launch { pagerState.animateScrollToPage(3) }
                                    },
                                    onNotificationClick = {
                                        startActivity(Intent(this@Screen7Activity, Screen28Activity::class.java))
                                    },
                                    onCropHealthClick = {
                                        startActivity(Intent(this@Screen7Activity, Screen15Activity::class.java))
                                    },
                                    onFieldBoundaryClick = {
                                        startActivity(Intent(this@Screen7Activity, Screen17Activity::class.java))
                                    },
                                    onYieldClick = {
                                        startActivity(Intent(this@Screen7Activity, Screen16Activity::class.java))
                                    }
                                )
                            }
                            1 -> {
                                FullGisMapTab(
                                    onVegetationClick = {
                                        startActivity(Intent(this@Screen7Activity, Screen11Activity::class.java))
                                    },
                                    onBoundaryClick = {
                                        startActivity(Intent(this@Screen7Activity, Screen17Activity::class.java))
                                    },
                                    onNavigate = { targetClass ->
                                        startActivity(Intent(this@Screen7Activity, targetClass))
                                    }
                                )
                            }
                            2 -> {
                                FullAdvisoryTab(
                                    onTaskClick = { targetClass ->
                                        startActivity(Intent(this@Screen7Activity, targetClass))
                                    }
                                )
                            }
                            3 -> {
                                FullProfileSettingsTab(
                                    currentThemeMode = ThemePreferences.themeMode,
                                    onThemeModeSelected = { mode ->
                                        ThemePreferences.updateThemeMode(this@Screen7Activity, mode)
                                    },
                                    onNavigate = { targetClass ->
                                        startActivity(Intent(this@Screen7Activity, targetClass))
                                    },
                                    onSignOut = {
                                        showLogoutDialog = true
                                    }
                                )
                            }
                        }
                    }

                    // Physical Finger-Dragged Bottom Navigation Dock (No Tapping)
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                    ) {
                        DraggableBottomDock(pagerState = pagerState, coroutineScope = coroutineScope)
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val targetTab = intent.getIntExtra("TARGET_TAB", -1)
        if (targetTab in 0..3) {
            // Handled when activity is re-entered
            recreate()
        }
    }
}

// -------------------------------------------------------------
// Tab 1: Full GIS Interactive Map Screen (Sentinel-2 & Live Drawing)
// -------------------------------------------------------------
data class GisZone(
    val id: String,
    val name: String,
    val acreage: String,
    val hectares: Double,
    val crop: String,
    val ndvi: String,
    val moisture: String,
    val status: String,
    val offsetFraction: Offset,
    val polygonPoints: List<Offset> = emptyList(),
    val irrigationVerdict: String = "NO IRRIGATION NEEDED",
    val irrigationDepthMm: Double = 0.0,
    val irrigationVolumeLitres: Long = 0L,
    val irrigationVolumeM3: Double = 0.0,
    val seedVariety: String = "DKC 9108 (Bio-Seed)",
    val seedRateKgHa: Double = 20.0,
    val totalSeedKg: Double = 974.0,
    val approxBags: String = "39 bags (25kg)",
    val spraySafe: Boolean = true,
    val sprayExplanation: String = "Wind calm at 8.5 km/h · Rain chance 0% · Safe today",
    val source: String = "Sentinel-2 L2A (10m) · Copernicus & ISRO"
)

val initialGisZones = listOf(
    GisZone(
        id = "1",
        name = "North Sector Alpha",
        acreage = "120.4 Acres (48.7 ha)",
        hectares = 48.7,
        crop = "Corn (Dent)",
        ndvi = "0.84 (Optimal)",
        moisture = "72%",
        status = "Healthy",
        offsetFraction = Offset(0.35f, 0.35f),
        polygonPoints = listOf(Offset(0.15f, 0.15f), Offset(0.55f, 0.12f), Offset(0.50f, 0.48f), Offset(0.18f, 0.45f)),
        irrigationVerdict = "NO IRRIGATION NEEDED",
        irrigationDepthMm = 0.0,
        irrigationVolumeLitres = 0L,
        irrigationVolumeM3 = 0.0,
        seedVariety = "DKC 9108 (Bio-Seed)",
        seedRateKgHa = 20.0,
        totalSeedKg = 974.0,
        approxBags = "39 bags (25kg)",
        spraySafe = true,
        sprayExplanation = "Wind calm at 8.5 km/h · No rain in 48h"
    ),
    GisZone(
        id = "2",
        name = "East Field Beta",
        acreage = "84.2 Acres (34.1 ha)",
        hectares = 34.1,
        crop = "Soybeans",
        ndvi = "0.78 (Good)",
        moisture = "65%",
        status = "Monitored",
        offsetFraction = Offset(0.68f, 0.42f),
        polygonPoints = listOf(Offset(0.58f, 0.18f), Offset(0.88f, 0.22f), Offset(0.85f, 0.55f), Offset(0.54f, 0.52f)),
        irrigationVerdict = "NO IRRIGATION NEEDED",
        irrigationDepthMm = 0.0,
        irrigationVolumeLitres = 0L,
        irrigationVolumeM3 = 0.0,
        seedVariety = "JS 335 (ICAR-IISR)",
        seedRateKgHa = 75.0,
        totalSeedKg = 2557.5,
        approxBags = "85 bags (30kg)",
        spraySafe = true,
        sprayExplanation = "Safe foliar spraying window open"
    ),
    GisZone(
        id = "3",
        name = "South Center Pivot",
        acreage = "145.0 Acres (58.7 ha)",
        hectares = 58.7,
        crop = "Winter Wheat",
        ndvi = "0.62 (Moderate)",
        moisture = "48%",
        status = "Needs Water",
        offsetFraction = Offset(0.48f, 0.68f),
        polygonPoints = listOf(Offset(0.35f, 0.60f), Offset(0.62f, 0.60f), Offset(0.62f, 0.85f), Offset(0.35f, 0.85f)),
        irrigationVerdict = "IRRIGATE TODAY",
        irrigationDepthMm = 35.0,
        irrigationVolumeLitres = 20545000L,
        irrigationVolumeM3 = 20545.0,
        seedVariety = "HD 2967 (ICAR-IARI)",
        seedRateKgHa = 100.0,
        totalSeedKg = 5870.0,
        approxBags = "147 bags (40kg)",
        spraySafe = true,
        sprayExplanation = "Irrigate early AM before evaporation loss"
    )
)

private fun calculateShoelaceArea(points: List<Offset>): Double {
    if (points.size < 3) return 0.0
    var sum = 0.0
    for (i in points.indices) {
        val j = (i + 1) % points.size
        sum += points[i].x * points[j].y - points[j].x * points[i].y
    }
    return kotlin.math.abs(sum) / 2.0
}

@Composable
fun FullGisMapTab(
    onVegetationClick: () -> Unit,
    onBoundaryClick: () -> Unit,
    onNavigate: ((Class<*>) -> Unit)? = null
) {
    val zones = remember { mutableStateListOf(*initialGisZones.toTypedArray()) }
    var selectedZone by remember { mutableStateOf(zones[0]) }
    var selectedLayer by remember { mutableStateOf("NDVI Heatmap") }

    // Interactive Drawing Mode State
    var isDrawingMode by remember { mutableStateOf(false) }
    val drawnPoints = remember { mutableStateListOf<Offset>() }
    var isSubmitting by remember { mutableStateOf(false) }
    var statusNotice by remember { mutableStateOf<String?>("Tap any parcel pin or switch to 'Interactive Boundary' to draw your farmland.") }
    val coroutineScope = rememberCoroutineScope()
    val repository = remember { GeoPulseRepository() }

    val drawnNormArea = calculateShoelaceArea(drawnPoints)
    val drawnLiveAcres = kotlin.math.round((drawnNormArea * 741.3) * 10.0) / 10.0
    val drawnLiveHa = kotlin.math.round((drawnLiveAcres / 2.47105) * 10.0) / 10.0

    val isDark = ThemePreferences.isDarkTheme
    val gisBgBrush = if (isDark) {
        Brush.radialGradient(
            colors = listOf(EmeraldPrimary.copy(alpha = 0.25f), DarkBackground),
            radius = 900f
        )
    } else {
        Brush.radialGradient(
            colors = listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.background),
            radius = 900f
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gisBgBrush)
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            val isTablet = maxWidth > 600.dp
            val contentWidth = if (isTablet) 640.dp else maxWidth
            val horizontalPadding = if (maxWidth < 360.dp) 12.dp else 16.dp

            LazyColumn(
                modifier = Modifier
                    .width(contentWidth)
                    .fillMaxHeight(),
                contentPadding = PaddingValues(start = horizontalPadding, end = horizontalPadding, top = 16.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    GeoTopBar(
                        title = "Geospatial Field Map",
                        subtitle = "Sentinel-2 L2A STAC · GPS: 13.06° N, 80.21° E · ${zones.size} Parcels",
                        actionIcon = Icons.Default.MyLocation
                    )
                }

                // Layer Selector & Drawing Mode Toggle Chips
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(listOf("NDVI Heatmap", "Soil Moisture", "Satellite RGB", "Interactive Boundary")) { layer ->
                                FilterChip(
                                    selected = if (layer == "Interactive Boundary") isDrawingMode else (selectedLayer == layer && !isDrawingMode),
                                    onClick = {
                                        if (layer == "Interactive Boundary") {
                                            isDrawingMode = !isDrawingMode
                                            if (isDrawingMode) {
                                                statusNotice = "Tap 3 or more points on the map to enclose your farmland parcel."
                                            }
                                        } else {
                                            selectedLayer = layer
                                            isDrawingMode = false
                                        }
                                    },
                                    label = { Text(layer, fontWeight = FontWeight.SemiBold) },
                                    leadingIcon = if (layer == "Interactive Boundary") {
                                        { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                    } else null
                                )
                            }
                        }

                        // Drawing Mode Controls Banner
                        if (isDrawingMode) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(1.dp, LimeAccent),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "✏️ Draw Land Boundary (${drawnPoints.size} points)",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = LimeAccent
                                        )
                                        if (drawnPoints.size >= 3) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = EmeraldPrimary.copy(alpha = 0.25f)
                                            ) {
                                                Text(
                                                    text = "Area: $drawnLiveAcres ac ($drawnLiveHa ha)",
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                    fontSize = 11.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = LimeAccent
                                                )
                                            }
                                        }
                                    }

                                    Text(
                                        text = "Tap corners on the map below. Connect at least 3 points to enclose your parcel.",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                if (drawnPoints.isNotEmpty()) drawnPoints.removeAt(drawnPoints.size - 1)
                                            },
                                            enabled = drawnPoints.isNotEmpty(),
                                            modifier = Modifier.weight(1f).height(38.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Undo", fontSize = 12.sp)
                                        }

                                        OutlinedButton(
                                            onClick = { drawnPoints.clear() },
                                            enabled = drawnPoints.isNotEmpty(),
                                            modifier = Modifier.weight(1f).height(38.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Clear", fontSize = 12.sp)
                                        }

                                        Button(
                                            onClick = {
                                                if (drawnPoints.size >= 3) {
                                                    coroutineScope.launch {
                                                        isSubmitting = true
                                                        statusNotice = "Computing geodesic area and Sentinel-2 indices..."
                                                        val coords = drawnPoints.map { p ->
                                                            val lon = 80.21 + (p.x - 0.5) * 0.02
                                                            val lat = 13.06 + (0.5 - p.y) * 0.02
                                                            "%.6f %.6f".format(java.util.Locale.US, lon, lat)
                                                        }.toMutableList()
                                                        coords.add(coords.first())
                                                        val wkt = "POLYGON ((${coords.joinToString(", ")}))"

                                                        val newField = repository.createDrawnField(
                                                            name = "My Parcel ${zones.size + 1}",
                                                            cropType = "Corn (Dent)",
                                                            boundaryWkt = wkt
                                                        )

                                                        val finalAcres = newField?.areaAcres ?: drawnLiveAcres.coerceAtLeast(12.0)
                                                        val finalHa = finalAcres / 2.47105
                                                        val irri = repository.getIrrigationRecommendation(newField?.id ?: (zones.size + 1))
                                                        val sow = repository.getSowingRecommendation(newField?.id ?: (zones.size + 1))

                                                        val avgX = drawnPoints.map { it.x }.average().toFloat()
                                                        val avgY = drawnPoints.map { it.y }.average().toFloat()

                                                        val createdZone = GisZone(
                                                            id = "${zones.size + 1}",
                                                            name = newField?.name ?: "My Parcel ${zones.size + 1}",
                                                            acreage = "${String.format(java.util.Locale.US, "%.1f", finalAcres)} Acres (${String.format(java.util.Locale.US, "%.1f", finalHa)} ha)",
                                                            hectares = finalHa,
                                                            crop = newField?.cropType ?: "Corn (Dent)",
                                                            ndvi = "0.82 (Good)",
                                                            moisture = "70%",
                                                            status = "Registered",
                                                            offsetFraction = Offset(avgX, avgY),
                                                            polygonPoints = drawnPoints.toList(),
                                                            irrigationVerdict = irri.irrigationRecommendation,
                                                            irrigationDepthMm = irri.recommendedDepthMm,
                                                            irrigationVolumeLitres = (irri.recommendedDepthMm * finalHa * 10000.0).toLong(),
                                                            irrigationVolumeM3 = (irri.recommendedDepthMm * finalHa * 10.0),
                                                            seedVariety = sow.recommendedVariety,
                                                            seedRateKgHa = sow.seedRatePerHaKg,
                                                            totalSeedKg = kotlin.math.round(sow.seedRatePerHaKg * finalHa * 10.0) / 10.0,
                                                            approxBags = "${kotlin.math.ceil((sow.seedRatePerHaKg * finalHa) / 25.0).toInt()} bags (25kg)",
                                                            spraySafe = true,
                                                            sprayExplanation = "Calm winds · Safe foliar spraying",
                                                            source = "Farmer Drawn Polygon · Geodesic Engine"
                                                        )

                                                        zones.add(createdZone)
                                                        selectedZone = createdZone
                                                        drawnPoints.clear()
                                                        isDrawingMode = false
                                                        isSubmitting = false
                                                        statusNotice = "Field registered! Physical water volume & seeds calculated for your land."
                                                    }
                                                }
                                            },
                                            enabled = drawnPoints.size >= 3 && !isSubmitting,
                                            modifier = Modifier.weight(1.8f).height(38.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = LimeAccent, contentColor = PineDark)
                                        ) {
                                            Text(if (isSubmitting) "Saving..." else "Save & Analyze", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Interactive GIS Vector & Satellite Map Canvas
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF09140D))
                            .border(1.5.dp, if (isDrawingMode) LimeAccent else CardBorderColor, RoundedCornerShape(16.dp))
                            .pointerInput(isDrawingMode) {
                                if (isDrawingMode) {
                                    detectTapGestures { tapOffset ->
                                        val normX = (tapOffset.x / size.width).coerceIn(0.04f, 0.96f)
                                        val normY = (tapOffset.y / size.height).coerceIn(0.04f, 0.96f)
                                        drawnPoints.add(Offset(normX, normY))
                                    }
                                }
                            }
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height

                            // 1. Natural satellite landscape simulation background
                            drawRect(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0xFF163820), Color(0xFF09150E)),
                                    center = Offset(w * 0.5f, h * 0.5f),
                                    radius = w * 0.9f
                                )
                            )

                            // 2. Geospatial Grid Lines (10m Sentinel-2 pixel grid simulation)
                            val step = 38f
                            var x = 0f
                            while (x < w) {
                                drawLine(Color(0x18FFFFFF), Offset(x, 0f), Offset(x, h), strokeWidth = 1f)
                                x += step
                            }
                            var y = 0f
                            while (y < h) {
                                drawLine(Color(0x18FFFFFF), Offset(0f, y), Offset(w, y), strokeWidth = 1f)
                                y += step
                            }

                            // 3. Agricultural crop row contours
                            var contourX = 0f
                            while (contourX < w + h) {
                                drawLine(
                                    color = Color(0x0C81C784),
                                    start = Offset(contourX, 0f),
                                    end = Offset(contourX - h * 0.6f, h),
                                    strokeWidth = 1.2f
                                )
                                contourX += 30f
                            }

                            // 4. Render Established Parcels
                            zones.forEach { zone ->
                                if (zone.polygonPoints.size >= 3) {
                                    val polyPath = Path().apply {
                                        moveTo(zone.polygonPoints[0].x * w, zone.polygonPoints[0].y * h)
                                        for (i in 1 until zone.polygonPoints.size) {
                                            lineTo(zone.polygonPoints[i].x * w, zone.polygonPoints[i].y * h)
                                        }
                                        close()
                                    }

                                    val fillColor = when (selectedLayer) {
                                        "Soil Moisture" -> if (zone.moisture.contains("48")) Color(0x66FFB74D) else Color(0x6600BCD4)
                                        "Satellite RGB" -> Color(0x662E7D32)
                                        else -> if (zone.ndvi.contains("0.8")) Color(0x664CAF50) else Color(0x668BC34A)
                                    }
                                    drawPath(polyPath, fillColor)

                                    val isSelected = selectedZone.id == zone.id
                                    drawPath(
                                        polyPath,
                                        if (isSelected) Color.White else EmeraldPrimary,
                                        style = Stroke(width = if (isSelected) 3.5f else 1.8f)
                                    )
                                }
                            }

                            // 5. Render Active User-Drawn Boundary
                            if (drawnPoints.isNotEmpty()) {
                                val drawnPath = Path()
                                drawnPath.moveTo(drawnPoints[0].x * w, drawnPoints[0].y * h)
                                for (i in 1 until drawnPoints.size) {
                                    drawnPath.lineTo(drawnPoints[i].x * w, drawnPoints[i].y * h)
                                }

                                if (drawnPoints.size >= 3) {
                                    drawnPath.close()
                                    drawPath(drawnPath, Color(0x5581C784))
                                    drawPath(drawnPath, LimeAccent, style = Stroke(width = 3.2f))
                                } else {
                                    drawPath(drawnPath, LimeAccent, style = Stroke(width = 2.5f))
                                }

                                // Draw numbered node handles
                                drawnPoints.forEach { pt ->
                                    val nodeCenter = Offset(pt.x * w, pt.y * h)
                                    drawCircle(Color.White, radius = 9f, center = nodeCenter)
                                    drawCircle(LimeAccent, radius = 6f, center = nodeCenter)
                                }
                            }
                        }

                        // Interactive Centroid Pins for parcel selection
                        if (!isDrawingMode) {
                            zones.forEach { zone ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(
                                            start = (zone.offsetFraction.x * 250).dp,
                                            top = (zone.offsetFraction.y * 200).dp
                                        )
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (selectedZone.id == zone.id) LimeAccent else PineDark.copy(alpha = 0.88f),
                                        border = BorderStroke(1.2.dp, if (selectedZone.id == zone.id) Color.White else LimeAccent),
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
                }

                // Status Notification Banner
                if (statusNotice != null) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = LimeAccent, modifier = Modifier.size(18.dp))
                                Text(
                                    text = statusNotice!!,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 16.sp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Comprehensive Precision Agriculture Card for Selected Field
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().glassSurface(cornerRadius = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            // Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(selectedZone.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Text("${selectedZone.acreage} · ${selectedZone.crop}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        selectedZone.status,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            // Provenance Citation
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Sensors, contentDescription = null, tint = LimeAccent, modifier = Modifier.size(14.dp))
                                    Text("Data Source: ${selectedZone.source}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            // 3 Primary Sensor Metrics
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MetricChip("NDVI Vigor", selectedZone.ndvi, Modifier.weight(1f))
                                MetricChip("Soil Moisture", selectedZone.moisture, Modifier.weight(1f))
                                MetricChip("Spray Window", if (selectedZone.spraySafe) "Safe Today" else "Wait", Modifier.weight(1f))
                            }

                            // 1. FAO-56 Penman-Monteith Irrigation Water Balance
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("💧 FAO-56 Irrigation Water Balance", fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = LimeAccent)
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (selectedZone.irrigationVerdict == "NO IRRIGATION NEEDED") EmeraldPrimary.copy(alpha = 0.3f) else RedAccent.copy(alpha = 0.3f)
                                        ) {
                                            Text(
                                                text = selectedZone.irrigationVerdict,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp,
                                                color = if (selectedZone.irrigationVerdict == "NO IRRIGATION NEEDED") LimeAccent else RedAccent
                                            )
                                        }
                                    }

                                    Text(
                                        text = if (selectedZone.irrigationVerdict == "NO IRRIGATION NEEDED")
                                            "Soil moisture (${selectedZone.moisture}) is within optimal root-zone threshold. Crop has sufficient water storage."
                                        else
                                            "Root-zone water depletion exceeded threshold (48%). Apply irrigation immediately before afternoon peak heat.",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 16.sp
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Required Depth", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("${selectedZone.irrigationDepthMm} mm", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                        }
                                        Column(modifier = Modifier.weight(1.5f)) {
                                            Text("Total Water Volume", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(
                                                "${String.format(java.util.Locale.US, "%,d", selectedZone.irrigationVolumeLitres)} Litres",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = LimeAccent
                                            )
                                        }
                                    }

                                    Text(
                                        text = "Physical Formula: 1 mm water over 1 ha = 10,000 Litres (10 m³). Over ${selectedZone.hectares} ha.",
                                        fontSize = 10.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                    )
                                }
                            }

                            // 2. Verified ICAR Seed & Sowing Recommendation
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("🌱 Verified ICAR Sowing & Seed Calculator", fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = LimeAccent)
                                    Text(
                                        text = "Recommended Variety: ${selectedZone.seedVariety} · Sowing Window: June 15 - July 15",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Recommended Seed Rate", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("${selectedZone.seedRateKgHa} kg/ha", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                        }
                                        Column(modifier = Modifier.weight(1.5f)) {
                                            Text("Total Seed Required", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(
                                                "${selectedZone.totalSeedKg} kg (${selectedZone.approxBags})",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = LimeAccent
                                            )
                                        }
                                    }

                                    Text(
                                        text = "Planting Geometry: 60 cm (row) x 20 cm (plant) · Sowing Depth: 3 - 5 cm",
                                        fontSize = 10.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                    )
                                }
                            }

                            // Quick Action Navigation Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = onVegetationClick,
                                    modifier = Modifier.weight(1f).height(46.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
                                ) {
                                    Text("Vegetation", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        isDrawingMode = true
                                        statusNotice = "Drawing mode enabled. Tap map corners to redefine parcel boundary."
                                    },
                                    modifier = Modifier.weight(1f).height(46.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                ) {
                                    Text("Redraw Parcel", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Tab 2: Farm Advisory & Daily Action Planner (Practical Layperson Guidance)
// -------------------------------------------------------------
data class FarmerAdvisoryItem(
    val title: String,
    val subtitle: String,
    val timeBadge: String,
    val iconColor: Color,
    val icon: ImageVector,
    val targetClass: Class<*>
)

@Composable
fun FullAdvisoryTab(onTaskClick: (Class<*>) -> Unit) {
    val tasks = remember {
        listOf(
            FarmerAdvisoryItem(
                title = "Safe Spraying Window Open Today",
                subtitle = "Wind is calm at 8 km/h and humidity 58%. Safe to spray foliar nutrients before Friday rains.",
                timeBadge = "Today",
                iconColor = LimeAccent,
                icon = Icons.Default.WbSunny,
                targetClass = Screen14Activity::class.java
            ),
            FarmerAdvisoryItem(
                title = "Topsoil Moisture Dropping",
                subtitle = "Satellite water index indicates moisture at 54% in Field 1. Schedule watering tomorrow morning.",
                timeBadge = "Next 24h",
                iconColor = CyanAccent,
                icon = Icons.Default.WaterDrop,
                targetClass = Screen12Activity::class.java
            ),
            FarmerAdvisoryItem(
                title = "Nitrogen Fertilizer Top-Dressing",
                subtitle = "Satellite NDVI shows moderate crop vigor. Apply split Urea dose (45 kg/acre) before rains.",
                timeBadge = "This Week",
                iconColor = EmeraldPrimary,
                icon = Icons.Default.Spa,
                targetClass = Screen20Activity::class.java
            ),
            FarmerAdvisoryItem(
                title = "Pest & Leaf Blight Watch",
                subtitle = "Warm temperatures favor caterpillar emergence. Inspect under-leaf canopy in Field 2.",
                timeBadge = "Advisory",
                iconColor = RedAccent,
                icon = Icons.Default.BugReport,
                targetClass = Screen15Activity::class.java
            ),
            FarmerAdvisoryItem(
                title = "Local Mandi Prices Trending Up",
                subtitle = "Corn wholesale prices rose +3.2% in regional agricultural markets.",
                timeBadge = "Market",
                iconColor = GoldAccent,
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                targetClass = Screen27Activity::class.java
            )
        )
    }

    val isDark = ThemePreferences.isDarkTheme
    val gisBgBrush = if (isDark) {
        Brush.radialGradient(
            colors = listOf(EmeraldPrimary.copy(alpha = 0.25f), DarkBackground),
            radius = 900f
        )
    } else {
        Brush.radialGradient(
            colors = listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.background),
            radius = 900f
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gisBgBrush)
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            val isTablet = maxWidth > 600.dp
            val contentWidth = if (isTablet) 600.dp else maxWidth
            val horizontalPadding = if (maxWidth < 360.dp) 12.dp else 16.dp

            LazyColumn(
                modifier = Modifier
                    .width(contentWidth)
                    .fillMaxHeight(),
                contentPadding = PaddingValues(start = horizontalPadding, end = horizontalPadding, top = 8.dp, bottom = 104.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    GeoTopBar(
                        title = "Farm Advisory & Action Plan",
                        subtitle = "Satellite-derived daily farming recommendations"
                    )
                }

                // Summary Banner Card
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Field 1 · Corn (Maize)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        "Vegetative Stage (Day 52)",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Today's conditions are optimal for foliar nutrient spraying and light weeding. Expected rain on Friday will provide natural irrigation.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                // Practical KPI Chips
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricChip("Crop Vigor", "84% (Optimal)", Modifier.weight(1f))
                        MetricChip("Soil Moisture", "72% (Adequate)", Modifier.weight(1f))
                        MetricChip("Spraying", "Safe Today", Modifier.weight(1f))
                    }
                }

                item {
                    Text("Today's Actionable Tasks (" + tasks.size + ")", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                }

                items(tasks) { task ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onTaskClick(task.targetClass) },
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(task.iconColor.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(task.icon, contentDescription = null, tint = task.iconColor, modifier = Modifier.size(18.dp))
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(task.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = task.iconColor.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            task.timeBadge,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = task.iconColor,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(task.subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 16.sp)
                            }

                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Tab 3: Farm Profile & Settings (3-Way Theme Switcher & Practical Tools)
// -------------------------------------------------------------
@Composable
fun FullProfileSettingsTab(
    currentThemeMode: ThemeMode,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onNavigate: (Class<*>) -> Unit,
    onSignOut: () -> Unit
) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var offlineCaching by remember { mutableStateOf(true) }

    val isDark = ThemePreferences.isDarkTheme
    val gisBgBrush = if (isDark) {
        Brush.radialGradient(
            colors = listOf(EmeraldPrimary.copy(alpha = 0.25f), DarkBackground),
            radius = 900f
        )
    } else {
        Brush.radialGradient(
            colors = listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.background),
            radius = 900f
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gisBgBrush)
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            val isTablet = maxWidth > 600.dp
            val contentWidth = if (isTablet) 600.dp else maxWidth
            val horizontalPadding = if (maxWidth < 360.dp) 12.dp else 16.dp

            LazyColumn(
                modifier = Modifier
                    .width(contentWidth)
                    .fillMaxHeight(),
                contentPadding = PaddingValues(start = horizontalPadding, end = horizontalPadding, top = 8.dp, bottom = 104.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    GeoTopBar(title = "Profile & Settings")
                }

                // Farmer Profile Header Card
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(listOf(LimeAccent, EmeraldPrimary))),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("N", color = PineDark, fontSize = 24.sp, fontWeight = FontWeight.Black)
                            }
                            Column {
                                Text("Nithish Kumar", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Text("Green Valley Farm · 12.5 ha", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Primary Crops: Corn & Soybean", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                // Appearance & Theme Mode Selector (3-way: Dark, Light, System)
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Theme & Appearance", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text("Choose your preferred display theme", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    Triple(ThemeMode.LIGHT, "Light", Icons.Default.LightMode),
                                    Triple(ThemeMode.DARK, "Dark", Icons.Default.DarkMode),
                                    Triple(ThemeMode.SYSTEM, "System", Icons.Default.SettingsBrightness)
                                ).forEach { (mode, label, icon) ->
                                    val isSelected = currentThemeMode == mode
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { onThemeModeSelected(mode) },
                                        modifier = Modifier.weight(1f),
                                        leadingIcon = {
                                            Icon(icon, contentDescription = null, modifier = Modifier.size(15.dp))
                                        },
                                        label = {
                                            Text(
                                                label,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                            )
                                        }
                                    )
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Weather & Spray Alerts", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Text("Frost, rain and disease notifications", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Switch(checked = notificationsEnabled, onCheckedChange = { notificationsEnabled = it })
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Offline Satellite Map Caching", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Text("Save field satellite maps for offline use", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Switch(checked = offlineCaching, onCheckedChange = { offlineCaching = it })
                            }
                        }
                    }
                }

                // Practical Farm Operations & Layperson Tools
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Practical Farm Tools", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.height(2.dp))

                            ProfileItemRow("Local Mandi Market Rates", "Wholesale selling prices for your crops", Icons.AutoMirrored.Filled.TrendingUp) {
                                onNavigate(Screen27Activity::class.java)
                            }
                            ProfileItemRow("Farm Budget & Profit Calculator", "Track seed, fertilizer, and harvest net margin", Icons.Default.MonetizationOn) {
                                onNavigate(Screen22Activity::class.java)
                            }
                            ProfileItemRow("Crop Growth Calendar", "Growth stage milestones and harvest timeline", Icons.Default.CalendarMonth) {
                                onNavigate(Screen18Activity::class.java)
                            }
                            ProfileItemRow("My Land Parcels & Fields", "Switch and manage your registered fields", Icons.Default.Landscape) {
                                onNavigate(Screen23Activity::class.java)
                            }
                            ProfileItemRow("Kisan Advisory & Helpline", "Direct chat with agricultural specialists", Icons.Default.Chat) {
                                onNavigate(Screen26Activity::class.java)
                            }
                            ProfileItemRow("Data Privacy & Farm Records Export", "Download field archive or clear records", Icons.Default.Security) {
                                onNavigate(Screen30Activity::class.java)
                            }
                        }
                    }
                }

                // Sign Out Button
                item {
                    Button(
                        onClick = onSignOut,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sign Out of Account", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileItemRow(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(17.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
    }
}

