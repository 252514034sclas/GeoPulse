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
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.input.pointer.pointerInput
import org.maplibre.android.geometry.LatLng
import com.geopulse.app.map.MapLibreGisMap
import com.geopulse.app.map.BasemapType
import com.geopulse.app.map.MapConfig
import com.geopulse.app.map.GeospatialMath
import com.geopulse.app.network.*
import com.geopulse.app.ui.premium.*
import com.geopulse.app.ui.theme.*
import com.geopulse.app.notifications.GeoPulseFirebaseMessagingService
import com.google.firebase.messaging.FirebaseMessaging
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.launch

class Screen7Activity : ComponentActivity() {

    private val selectedTargetTab = mutableIntStateOf(-1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val initialTab = intent?.getIntExtra("TARGET_TAB", 0)?.coerceIn(0, 3) ?: 0

        // Ensure notification channels are registered
        GeoPulseFirebaseMessagingService.createNotificationChannels(this)

        // Android 13+ POST_NOTIFICATIONS permission request
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        // Register FCM device push token with backend
        try {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful && !task.result.isNullOrBlank()) {
                    GeoPulseFirebaseMessagingService.registerDeviceWithBackend(this, task.result)
                }
            }
        } catch (e: Exception) {
            android.util.Log.w("Screen7Activity", "FCM token registration: ${e.message}")
        }

        // Sync notifications with backend
        lifecycleScope.launch {
            com.geopulse.app.notifications.NotificationRepository.syncWithBackend(this@Screen7Activity)
        }

        setContent {
            GeoPulseTheme {
                val pagerState = rememberPagerState(initialPage = initialTab, pageCount = { 4 })
                val coroutineScope = rememberCoroutineScope()
                var showLogoutDialog by remember { mutableStateOf(false) }
                var showNotificationDialog by remember { mutableStateOf(false) }
                val isDark = ThemePreferences.isDarkTheme

                LaunchedEffect(selectedTargetTab.intValue) {
                    if (selectedTargetTab.intValue in 0..3 && pagerState.currentPage != selectedTargetTab.intValue) {
                        pagerState.scrollToPage(selectedTargetTab.intValue)
                    }
                }

                val repo = remember { com.geopulse.app.network.GeoPulseRepository() }
                LaunchedEffect(Unit) {
                    try {
                        val summary = repo.getDashboardSummary()
                        summary.todaysTasks.forEach { task ->
                            com.geopulse.app.notifications.NotificationRepository.addNotification(
                                com.geopulse.app.notifications.GeoNotification(
                                    id = "task_${task.id}",
                                    title = task.title,
                                    message = task.description,
                                    timestamp = task.timeBadge,
                                    priority = task.priority,
                                    category = "Advisory"
                                )
                            )
                        }
                        summary.weather?.let { w ->
                            if (!w.spraySafe || w.precipitationMm > 0) {
                                com.geopulse.app.notifications.NotificationRepository.addNotification(
                                    com.geopulse.app.notifications.GeoNotification(
                                        id = "weather_alert_${w.id}",
                                        title = if (!w.spraySafe) "Spray Window Warning" else "Precipitation Notice",
                                        message = w.sprayAdvice,
                                        timestamp = "Today",
                                        priority = if (!w.spraySafe) "HIGH" else "MEDIUM",
                                        category = "Weather"
                                    )
                                )
                            }
                        }
                    } catch (_: Exception) {}
                }

                if (showNotificationDialog) {
                    com.geopulse.app.notifications.GeoNotificationDialog(
                        onDismiss = { showNotificationDialog = false }
                    )
                }

                if (showLogoutDialog) {
                    AlertDialog(
                        onDismissRequest = { showLogoutDialog = false },
                        title = { Text("Sign Out", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
                        text = { Text("Are you sure you want to sign out of GeoPulse?", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)) },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showLogoutDialog = false
                                    com.geopulse.app.network.UserSession.clearSession(this@Screen7Activity)
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
                        userScrollEnabled = pagerState.currentPage != 1
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
                                        showNotificationDialog = true
                                    },
                                    onCropHealthClick = {
                                        startActivity(Intent(this@Screen7Activity, Screen11Activity::class.java))
                                    },
                                    onFieldBoundaryClick = {
                                        startActivity(Intent(this@Screen7Activity, Screen17Activity::class.java))
                                    },
                                    onYieldClick = {
                                        startActivity(Intent(this@Screen7Activity, Screen16Activity::class.java))
                                    },
                                    onActivityClick = {
                                        coroutineScope.launch { pagerState.animateScrollToPage(2) }
                                    },
                                    onNavigate = { targetClass ->
                                        startActivity(Intent(this@Screen7Activity, targetClass))
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
            selectedTargetTab.intValue = targetTab
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
    val cropHealth: String,
    val moisture: String,
    val status: String,
    val offsetFraction: Offset,
    val polygonPoints: List<Offset> = emptyList(),
    val geoPoints: List<LatLng> = emptyList(),
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

val initialGisZones: List<GisZone> = emptyList()

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
    onNavigate: ((Class<*>) -> Unit)? = null,
    showHeader: Boolean = true
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val zones = remember { mutableStateListOf(*initialGisZones.toTypedArray()) }
    var selectedZone by remember { mutableStateOf<GisZone?>(zones.firstOrNull()) }
    var selectedLayer by remember { mutableStateOf("Crop Health") }
    var activeBasemap by remember { mutableStateOf(MapConfig.getSelectedBasemap(context)) }

    // Interactive GIS Drawing Mode State
    var isDrawingMode by remember { mutableStateOf(false) }
    val drawnGeoPoints = remember { mutableStateListOf<LatLng>() }
    var isSubmitting by remember { mutableStateOf(false) }
    var statusNotice by remember { mutableStateOf<String?>("Interactive MapLibre GIS loaded. Tap 'Interactive Boundary' to trace GPS parcel coordinates.") }
    val coroutineScope = rememberCoroutineScope()
    val repository = remember { GeoPulseRepository() }

    // Persist and load drawn fields from backend database
    LaunchedEffect(Unit) {
        try {
            val summary = repository.getDashboardSummary()
            if (summary.fields.isNotEmpty()) {
                summary.fields.forEach { field ->
                    if (zones.none { it.id == field.id.toString() || it.name == field.name }) {
                        val ha = field.areaAcres / 2.47105
                        val parsedGeoPoints = if (!field.boundary.isNullOrEmpty()) {
                            GeospatialMath.parseWktToLatLngList(field.boundary)
                        } else {
                            val lat = field.latitude
                            val lon = field.longitude
                            listOf(
                                LatLng(lat - 0.003, lon - 0.004),
                                LatLng(lat - 0.003, lon + 0.004),
                                LatLng(lat + 0.003, lon + 0.004),
                                LatLng(lat + 0.003, lon - 0.004)
                            )
                        }
                        zones.add(
                            GisZone(
                                id = field.id.toString(),
                                name = field.name,
                                acreage = "${String.format(java.util.Locale.US, "%.1f", field.areaAcres)} Acres (${String.format(java.util.Locale.US, "%.1f", ha)} ha)",
                                hectares = ha,
                                crop = field.cropType,
                                cropHealth = "82% (Good)",
                                moisture = "70%",
                                status = field.status,
                                offsetFraction = Offset(0.5f, 0.5f),
                                polygonPoints = emptyList(),
                                geoPoints = parsedGeoPoints,
                                irrigationVerdict = "NO IRRIGATION NEEDED",
                                irrigationDepthMm = 0.0,
                                irrigationVolumeLitres = 0L,
                                seedVariety = "DKC 9108 (Bio-Seed)",
                                seedRateKgHa = 20.0,
                                totalSeedKg = kotlin.math.round(20.0 * ha * 10.0) / 10.0,
                                source = "Persisted Field Boundary (GeoPulse Database)"
                            )
                        )
                    }
                }
                if (selectedZone == null && zones.isNotEmpty()) {
                    selectedZone = zones.firstOrNull()
                }
            }
        } catch (_: Exception) {}
    }

    val liveArea = remember(drawnGeoPoints.size) {
        GeospatialMath.calculateGeodesicArea(drawnGeoPoints.toList())
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
            val contentWidth = if (isTablet) 640.dp else maxWidth
            val horizontalPadding = if (maxWidth < 360.dp) 12.dp else 16.dp

            LazyColumn(
                modifier = Modifier
                    .width(contentWidth)
                    .fillMaxHeight(),
                contentPadding = PaddingValues(start = horizontalPadding, end = horizontalPadding, top = 16.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (showHeader) {
                    item {
                        GeoTopBar(
                            title = "Agricultural Fields & Land Parcels",
                            subtitle = "Field Selection · Sentinel-2 observation & live GPS boundaries · ${zones.size} Parcels",
                            actionIcon = Icons.Default.MyLocation
                        )
                    }
                }

                // Flow Step 1: Field Selection (Fields -> Field Selection -> Selected Field Overview)
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Field Selection",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.5.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${zones.size} Registered",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(zones) { zone ->
                                val isSelected = selectedZone?.id == zone.id
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedZone = zone
                                        zone.id.toIntOrNull()?.let { fId ->
                                            com.geopulse.app.network.UserSession.setSelectedFieldId(context, fId)
                                        }
                                    },
                                    label = {
                                        Text(
                                            text = "${zone.name} (${zone.crop})",
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Landscape,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                )
                            }
                            item {
                                FilterChip(
                                    selected = isDrawingMode,
                                    onClick = {
                                        isDrawingMode = !isDrawingMode
                                        if (isDrawingMode) {
                                            statusNotice = "Tap 3 or more points on the map to enclose your farmland parcel."
                                        }
                                    },
                                    label = { Text("+ Add Parcel", fontWeight = FontWeight.Bold) },
                                    leadingIcon = {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                )
                            }
                        }
                    }
                }

                // Layer Selector & Drawing Mode Toggle Chips
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(end = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(listOf("Crop Health", "Soil Water", "Satellite View", "Trace Boundary")) { layer ->
                                FilterChip(
                                    selected = if (layer == "Trace Boundary") isDrawingMode else (selectedLayer == layer && !isDrawingMode),
                                    onClick = {
                                        if (layer == "Trace Boundary") {
                                            isDrawingMode = !isDrawingMode
                                            if (isDrawingMode) {
                                                statusNotice = "Tap 3 or more points on the map to enclose your farmland parcel."
                                            }
                                        } else {
                                            selectedLayer = layer
                                            isDrawingMode = false
                                            if (layer == "Satellite View") {
                                                activeBasemap = BasemapType.SATELLITE
                                                MapConfig.setSelectedBasemap(context, BasemapType.SATELLITE)
                                            }
                                        }
                                    },
                                    label = { Text(layer, fontWeight = FontWeight.SemiBold) },
                                    leadingIcon = if (layer == "Trace Boundary") {
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
                                            text = "✏️ Draw Land Boundary (${drawnGeoPoints.size} points)",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = LimeAccent
                                        )
                                        if (drawnGeoPoints.size >= 3) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = EmeraldPrimary.copy(alpha = 0.25f)
                                            ) {
                                                Text(
                                                    text = "Area: ${String.format(java.util.Locale.US, "%.1f", liveArea.areaAcres)} ac (${String.format(java.util.Locale.US, "%.1f", liveArea.areaHectares)} ha)",
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
                                                if (drawnGeoPoints.isNotEmpty()) drawnGeoPoints.removeAt(drawnGeoPoints.size - 1)
                                            },
                                            enabled = drawnGeoPoints.isNotEmpty(),
                                            modifier = Modifier.weight(1f).height(38.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Undo", fontSize = 12.sp)
                                        }

                                        OutlinedButton(
                                            onClick = { drawnGeoPoints.clear() },
                                            enabled = drawnGeoPoints.isNotEmpty(),
                                            modifier = Modifier.weight(1f).height(38.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Clear", fontSize = 12.sp)
                                        }

                                        Button(
                                            onClick = {
                                                if (drawnGeoPoints.size >= 3) {
                                                    coroutineScope.launch {
                                                        isSubmitting = true
                                                        statusNotice = "Computing geodesic area and Sentinel-2 indices..."
                                                        val wkt = GeospatialMath.latLngListToWkt(drawnGeoPoints.toList())

                                                        val newField = repository.createDrawnField(
                                                            name = "My Parcel ${zones.size + 1}",
                                                            cropType = "Corn (Dent)",
                                                            boundaryWkt = wkt
                                                        )

                                                        val finalAcres = newField?.areaAcres ?: liveArea.areaAcres.coerceAtLeast(10.0)
                                                        val finalHa = finalAcres / 2.47105
                                                        val fieldId = newField?.id ?: 0
                                                        val irri = if (newField != null) {
                                                            com.geopulse.app.network.UserSession.setSelectedFieldId(context, newField.id)
                                                            repository.getIrrigationRecommendation(newField.id)
                                                        } else {
                                                            com.geopulse.app.network.IrrigationDto(
                                                                fieldAreaHa = finalHa,
                                                                fieldAreaAcres = finalAcres,
                                                                crop = "Corn (Dent)",
                                                                growthStage = "Vegetative",
                                                                dailyCropWaterDemandMm = 5.4,
                                                                currentMoisturePct = 70.0,
                                                                irrigationRecommendation = "SAFE TO WATER",
                                                                actionExplanation = "Calculated from boundary geometry.",
                                                                recommendedTiming = "Tomorrow 6:00 AM - 8:30 AM",
                                                                recommendedDepthMm = 15.0,
                                                                totalWaterVolumeLitres = (15.0 * finalHa * 10000.0).toLong(),
                                                                totalWaterVolumeM3 = (15.0 * finalHa * 10.0),
                                                                confidence = "High"
                                                            )
                                                        }
                                                        val sow = if (newField != null) {
                                                            repository.getSowingRecommendation(newField.id)
                                                        } else {
                                                            com.geopulse.app.network.SowingDto(
                                                                cropName = "Corn (Dent)",
                                                                fieldAreaHa = finalHa,
                                                                fieldAreaAcres = finalAcres,
                                                                recommendedVariety = "DKC 9108 (Bio-Seed)",
                                                                season = "Kharif",
                                                                duration = "105-115 Days",
                                                                soilSuitability = "Loam",
                                                                recommendedSowingWindow = "June 15 - July 15",
                                                                soilPreparationSteps = "1 deep disc ploughing followed by 2 harrowings.",
                                                                seedRatePerHaKg = 20.0,
                                                                seedRatePerAcreKg = 8.1,
                                                                totalSeedRequiredKg = kotlin.math.round(20.0 * finalHa * 10.0) / 10.0,
                                                                approxBagsNeeded = "${kotlin.math.ceil((20.0 * finalHa) / 25.0).toInt()} bags (25kg)",
                                                                plantingGeometry = "60 cm x 20 cm",
                                                                sowingDepth = "3 - 5 cm",
                                                                expectedYieldRange = "28-34 Q/ha",
                                                                totalWaterRequirement = "550 mm",
                                                                authoritativeSource = "Regional Package of Practices"
                                                            )
                                                        }

                                                        val createdZone = GisZone(
                                                            id = "$fieldId",
                                                            name = newField?.name ?: "My Parcel ${zones.size + 1}",
                                                            acreage = "${String.format(java.util.Locale.US, "%.1f", finalAcres)} Acres (${String.format(java.util.Locale.US, "%.1f", finalHa)} ha)",
                                                            hectares = finalHa,
                                                            crop = newField?.cropType ?: "Corn (Dent)",
                                                            cropHealth = "82% (Good)",
                                                            moisture = "70%",
                                                            status = "Registered",
                                                            offsetFraction = Offset(0.5f, 0.5f),
                                                            polygonPoints = emptyList(),
                                                            geoPoints = drawnGeoPoints.toList(),
                                                            irrigationVerdict = irri.irrigationRecommendation,
                                                            irrigationDepthMm = irri.recommendedDepthMm,
                                                            irrigationVolumeLitres = (irri.recommendedDepthMm * finalHa * 10000.0).toLong(),
                                                            irrigationVolumeM3 = (irri.recommendedDepthMm * finalHa * 10.0),
                                                            seedVariety = sow.recommendedVariety,
                                                            seedRateKgHa = sow.seedRatePerHaKg,
                                                            totalSeedKg = kotlin.math.round(sow.seedRatePerHaKg * finalHa * 10.0) / 10.0,
                                                            approxBags = "${kotlin.math.ceil((sow.seedRatePerHaKg * finalHa) / 25.0).toInt()} bags (25kg)",
                                                            spraySafe = true,
                                                            sprayExplanation = "Calm winds · Safe foliar spraying window",
                                                            source = "Farmer Drawn Boundary · MapLibre Geodesic Engine"
                                                        )

                                                        zones.add(createdZone)
                                                        selectedZone = createdZone
                                                        drawnGeoPoints.clear()
                                                        isDrawingMode = false
                                                        isSubmitting = false
                                                        statusNotice = "Field registered! Physical water volume & seeds calculated for your land."
                                                    }
                                                }
                                            },
                                            enabled = drawnGeoPoints.size >= 3 && !isSubmitting,
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

                // Interactive Real MapLibre Native GIS Map
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(350.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, if (isDrawingMode) LimeAccent else MaterialTheme.colorScheme.outline),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        MapLibreGisMap(
                            modifier = Modifier.fillMaxSize(),
                            zones = zones,
                            selectedZone = selectedZone,
                            onZoneSelected = { zone ->
                                selectedZone = zone
                            },
                            isDrawingMode = isDrawingMode,
                            drawnPoints = drawnGeoPoints.toList(),
                            onMapTappedInDrawingMode = { latLng ->
                                drawnGeoPoints.add(latLng)
                            },
                            activeBasemap = activeBasemap,
                            onBasemapChange = { newType ->
                                activeBasemap = newType
                                MapConfig.setSelectedBasemap(context, newType)
                            }
                        )
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
                    val currentZone = selectedZone
                    if (currentZone != null) {
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
                                        Text(currentZone.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                                        Text("${currentZone.acreage} · ${currentZone.crop}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            currentZone.status,
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
                                        Text("Data Source: ${currentZone.source}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                // 3 Primary Sensor Metrics
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    MetricChip("Crop Health", currentZone.cropHealth, Modifier.weight(1f))
                                    MetricChip("Soil Moisture", currentZone.moisture, Modifier.weight(1f))
                                    MetricChip("Spray Window", if (currentZone.spraySafe) "Safe Today" else "Wait", Modifier.weight(1f))
                                }

                                // 1. Scientific Crop Water Balance
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
                                            Text("💧 Crop Water Balance", fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = LimeAccent)
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (currentZone.irrigationVerdict == "NO IRRIGATION NEEDED") EmeraldPrimary.copy(alpha = 0.3f) else RedAccent.copy(alpha = 0.3f)
                                            ) {
                                                Text(
                                                    text = currentZone.irrigationVerdict,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 10.sp,
                                                    color = if (currentZone.irrigationVerdict == "NO IRRIGATION NEEDED") LimeAccent else RedAccent
                                                )
                                            }
                                        }

                                        Text(
                                            text = if (currentZone.irrigationVerdict == "NO IRRIGATION NEEDED")
                                                "Soil moisture (${currentZone.moisture}) is within optimal root-zone threshold. Crop has sufficient water storage."
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
                                                Text("${currentZone.irrigationDepthMm} mm", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                            }
                                            Column(modifier = Modifier.weight(1.5f)) {
                                                Text("Total Water Volume", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text(
                                                    "${String.format(java.util.Locale.US, "%,d", currentZone.irrigationVolumeLitres)} Litres",
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = LimeAccent
                                                )
                                            }
                                        }

                                        Text(
                                            text = "Physical Formula: 1 mm water over 1 ha = 10,000 Litres (10 m³). Over ${currentZone.hectares} ha.",
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
                                            text = "Recommended Variety: ${currentZone.seedVariety} · Sowing Window: June 15 - July 15",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("Recommended Seed Rate", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text("${currentZone.seedRateKgHa} kg/ha", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                            }
                                            Column(modifier = Modifier.weight(1.5f)) {
                                                Text("Total Seed Required", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text(
                                                    "${currentZone.totalSeedKg} kg (${currentZone.approxBags})",
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

                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                                Text(
                                    text = "Field Navigation & Analytical Capabilities",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                // Structured Access to 6 Field Domains
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    // 1. Field Map & Boundaries
                                    FieldFeatureRow(
                                        title = "Field Map & Boundaries",
                                        subtitle = "Interactive Sentinel-2 map, GPS boundary & parcel area",
                                        icon = Icons.Default.Place,
                                        iconTint = LimeAccent,
                                        badge = currentZone.acreage,
                                        onClick = onBoundaryClick
                                    )

                                    // 2. Satellite Crop Health
                                    FieldFeatureRow(
                                        title = "Satellite Crop Health & Vigor",
                                        subtitle = "NDVI vegetative index: ${currentZone.cropHealth} · Canopy health",
                                        icon = Icons.Default.Eco,
                                        iconTint = EmeraldPrimary,
                                        badge = "Live Sentinel-2",
                                        onClick = onVegetationClick
                                    )

                                    // 3. Soil Moisture
                                    FieldFeatureRow(
                                        title = "Soil Moisture & Hydration",
                                        subtitle = "Root-zone moisture: ${currentZone.moisture} · Topsoil hydration report",
                                        icon = Icons.Default.WaterDrop,
                                        iconTint = CyanAccent,
                                        badge = currentZone.moisture,
                                        onClick = {
                                            onNavigate?.invoke(Screen12Activity::class.java)
                                        }
                                    )

                                    // 4. Irrigation Recommendations
                                    FieldFeatureRow(
                                        title = "Irrigation Recommendations",
                                        subtitle = "FAO-56 water balance: ${currentZone.irrigationVerdict} (${currentZone.irrigationDepthMm} mm)",
                                        icon = Icons.Default.Water,
                                        iconTint = StatusBlue,
                                        badge = "${String.format(java.util.Locale.US, "%,d", currentZone.irrigationVolumeLitres)} L",
                                        onClick = {
                                            onNavigate?.invoke(Screen13Activity::class.java)
                                        }
                                    )

                                    // 5. Crop and Sowing Information
                                    FieldFeatureRow(
                                        title = "Crop & Sowing Information",
                                        subtitle = "Variety: ${currentZone.seedVariety} · Sowing schedule & geometry",
                                        icon = Icons.Default.Spa,
                                        iconTint = LimeAccent,
                                        badge = "${currentZone.totalSeedKg} kg Seed",
                                        onClick = {
                                            onNavigate?.invoke(Screen18Activity::class.java)
                                        }
                                    )

                                    // 6. Field-Specific History
                                    FieldFeatureRow(
                                        title = "Field History & Yield Forecast",
                                        subtitle = "Seasonal bi-weekly growth trends & harvest yield estimator",
                                        icon = Icons.Default.Assessment,
                                        iconTint = GoldAccent,
                                        badge = "Yield Forecast",
                                        onClick = {
                                            onNavigate?.invoke(Screen16Activity::class.java)
                                        }
                                    )
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
                    } else {
                        // Clean Empty State Card
                        Card(
                            modifier = Modifier.fillMaxWidth().glassSurface(cornerRadius = 16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(Icons.Default.Landscape, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(44.dp))
                                Text("No Farm Parcels Added Yet", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                                Text(
                                    "Tap 'Interactive Boundary' above or tap on the map to draw your field boundary and get live satellite intelligence.",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Button(
                                    onClick = {
                                        isDrawingMode = true
                                        statusNotice = "Tap 3 or more points on the map to enclose your farmland parcel."
                                    },
                                    modifier = Modifier.height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = LeafGreen, contentColor = Color.White)
                                ) {
                                    Text("Trace Field Boundary", fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
// Reusable Field Domain Feature Row
// -------------------------------------------------------------
@Composable
fun FieldFeatureRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    badge: String? = null,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                    if (badge != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = iconTint.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = badge,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = iconTint,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            }

            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
        }
    }
}

// -------------------------------------------------------------
// Tab 2: Farm Activities & Task Planner (Operational Workflows)
// -------------------------------------------------------------
data class FarmActionItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val timeBadge: String,
    val iconColor: Color,
    val icon: ImageVector,
    val targetClass: Class<*>,
    val details: String = ""
)

@Composable
fun FullAdvisoryTab(
    onTaskClick: (Class<*>) -> Unit,
    showHeader: Boolean = true
) {
    val completedTasks = remember { mutableStateMapOf<String, Boolean>() }

    val todayTasks = remember {
        listOf(
            FarmActionItem(
                id = "spray_window",
                title = "Safe Spraying Window Open Today",
                subtitle = "Wind calm at 8 km/h, humidity 58%. Safe to spray foliar nutrients before Friday rains.",
                timeBadge = "Optimal Today",
                iconColor = LimeAccent,
                icon = Icons.Default.WbSunny,
                targetClass = Screen14Activity::class.java,
                details = "Optimal foliar nutrient & neem oil spray conditions"
            ),
            FarmActionItem(
                id = "irrigation_today",
                title = "Schedule Morning Root-Zone Irrigation",
                subtitle = "Soil water index indicates 54% moisture in Field 1. Apply 15 mm irrigation before 10:00 AM.",
                timeBadge = "Next 24h",
                iconColor = CyanAccent,
                icon = Icons.Default.WaterDrop,
                targetClass = Screen13Activity::class.java,
                details = "FAO-56 water deficit: 15 mm (150,000 L volume)"
            )
        )
    }

    val upcomingActivities = remember {
        listOf(
            FarmActionItem(
                id = "nitrogen_topdress",
                title = "Nitrogen Fertilizer Top-Dressing",
                subtitle = "Vegetative tasseling phase. Apply split Urea dose (45 kg/acre) prior to forecasted rain.",
                timeBadge = "In 2 Days",
                iconColor = EmeraldPrimary,
                icon = Icons.Default.Spa,
                targetClass = Screen20Activity::class.java,
                details = "Target: Field 1 · Urea (46% N)"
            ),
            FarmActionItem(
                id = "cultivation_weeding",
                title = "Field Cultivation & Inter-Row Weeding",
                subtitle = "Shallow plowing and aeration to suppress weed growth while soil moisture is workable.",
                timeBadge = "In 4 Days",
                iconColor = GoldAccent,
                icon = Icons.Default.Agriculture,
                targetClass = Screen19Activity::class.java,
                details = "Target: Field 1 & 2 · Inter-row cultivator"
            ),
            FarmActionItem(
                id = "pest_scouting",
                title = "Under-Canopy Pest & Disease Scouting",
                subtitle = "Warm temperatures favor caterpillar emergence. Inspect lower leaf surfaces for early fall armyworm.",
                timeBadge = "Advisory",
                iconColor = RedAccent,
                icon = Icons.Default.BugReport,
                targetClass = Screen15Activity::class.java,
                details = "Biological check: Neem oil or pheromone traps"
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
                contentPadding = PaddingValues(start = horizontalPadding, end = horizontalPadding, top = 8.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (showHeader) {
                    item {
                        GeoTopBar(
                            title = "Farm Activities & Task Planner",
                            subtitle = "Operational schedules, spray windows & fieldwork planner"
                        )
                    }
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

                // 1. Today's Tasks
                val pendingTodayTasks = todayTasks.filter { completedTasks[it.id] != true }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Today's Tasks (${pendingTodayTasks.size} Pending)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Tap to Complete / Open",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                items(todayTasks) { task ->
                    val isDone = completedTasks[task.id] == true
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onTaskClick(task.targetClass) },
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, if (isDone) EmeraldPrimary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Checkbox(
                                checked = isDone,
                                onCheckedChange = { checked ->
                                    completedTasks[task.id] = checked
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = EmeraldPrimary,
                                    checkmarkColor = Color.White
                                )
                            )

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
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
                                    Text(
                                        text = task.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp,
                                        color = if (isDone) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isDone) EmeraldPrimary.copy(alpha = 0.15f) else task.iconColor.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = if (isDone) "Done" else task.timeBadge,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDone) EmeraldPrimary else task.iconColor,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = task.subtitle,
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 15.sp
                                )
                            }

                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                // 2. Upcoming Agricultural Activities
                item {
                    Text(
                        text = "Upcoming Agricultural Activities (${upcomingActivities.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                items(upcomingActivities) { activity ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onTaskClick(activity.targetClass) },
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
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(activity.iconColor.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(activity.icon, contentDescription = null, tint = activity.iconColor, modifier = Modifier.size(18.dp))
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = activity.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = activity.iconColor.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = activity.timeBadge,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = activity.iconColor,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = activity.subtitle,
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 15.sp
                                )
                            }

                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                // 3. Irrigation Schedule Card
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, StatusBlue.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.Water, contentDescription = null, tint = StatusBlue, modifier = Modifier.size(20.dp))
                                    Text("Irrigation Schedule", fontWeight = FontWeight.Bold, fontSize = 14.5.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                                Surface(shape = RoundedCornerShape(6.dp), color = StatusBlue.copy(alpha = 0.15f)) {
                                    Text("FAO-56 Cycle", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = StatusBlue, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                            Text("Next Window: Tomorrow 6:00 AM - 8:30 AM · Required Depth: 15.0 mm (150,000 Litres)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Button(
                                onClick = { onTaskClick(Screen13Activity::class.java) },
                                modifier = Modifier.fillMaxWidth().height(42.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = StatusBlue)
                            ) {
                                Text("View Irrigation Timing Guide >", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                            }
                        }
                    }
                }

                // 4. Sowing Schedule Card
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.Spa, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
                                    Text("Sowing Schedule", fontWeight = FontWeight.Bold, fontSize = 14.5.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                                Surface(shape = RoundedCornerShape(6.dp), color = EmeraldPrimary.copy(alpha = 0.15f)) {
                                    Text("Kharif Season", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                            Text("Target Window: June 15 - July 15 · Variety: DKC 9108 (Bio-Seed) · Seed Rate: 20 kg/ha", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Button(
                                onClick = { onTaskClick(Screen18Activity::class.java) },
                                modifier = Modifier.fillMaxWidth().height(42.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                            ) {
                                Text("View Crop Growth Calendar >", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                            }
                        }
                    }
                }

                // 5. Important Advisory Actions Notice
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.7f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(20.dp))
                                Text("Important Weather Advisory Action", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = GoldAccent)
                            }
                            Text(
                                "Rain arriving Friday (12 mm expected). Postpone heavy irrigation to prevent nutrient runoff and save electricity/fuel pumping costs.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 16.sp
                            )
                            OutlinedButton(
                                onClick = { onTaskClick(Screen14Activity::class.java) },
                                modifier = Modifier.fillMaxWidth().height(42.dp),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, GoldAccent)
                            ) {
                                Text("View Weather & Spraying Forecast >", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = GoldAccent)
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Tab 3: Farm Profile & Settings (Preferences, Language & Tools)
// -------------------------------------------------------------
@Composable
fun FullProfileSettingsTab(
    currentThemeMode: ThemeMode,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onNavigate: (Class<*>) -> Unit,
    onSignOut: () -> Unit,
    showHeader: Boolean = true
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var notificationsEnabled by remember { mutableStateOf(true) }
    var selectedLanguage by remember { mutableStateOf("English") }
    var selectedAreaUnit by remember { mutableStateOf("Acres") }
    var selectedTempUnit by remember { mutableStateOf("°C") }

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
                contentPadding = PaddingValues(start = horizontalPadding, end = horizontalPadding, top = 8.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (showHeader) {
                    item {
                        GeoTopBar(
                            title = "Farmer Profile & Preferences",
                            subtitle = "Account settings, regional units, language & notification controls"
                        )
                    }
                }

                // 1. Farmer Details Header Card
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
                            val fullName = remember { com.geopulse.app.network.UserSession.getFullName(context).ifBlank { "Registered Farmer" } }
                            val farmName = remember { com.geopulse.app.network.UserSession.getFarmName(context).ifBlank { "GeoPulse Farmland" } }
                            val initial = fullName.take(1).uppercase()

                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(listOf(LimeAccent, EmeraldPrimary))),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(initial, color = PineDark, fontSize = 24.sp, fontWeight = FontWeight.Black)
                            }
                            Column {
                                Text(fullName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Text("$farmName · Active Account", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Verified Geospatial Intelligence Account", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                // 2. Language Selection Card
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Language / மொழி", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                                TextButton(onClick = { onNavigate(Screen29Activity::class.java) }) {
                                    Text("Settings >", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("English", "தமிழ் (Tamil)").forEach { lang ->
                                    val isSelected = selectedLanguage == lang
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedLanguage = lang },
                                        modifier = Modifier.weight(1f),
                                        label = {
                                            Text(
                                                lang,
                                                fontSize = 12.5.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. Notification Preferences Card
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Notification Preferences", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                                TextButton(onClick = { onNavigate(Screen28Activity::class.java) }) {
                                    Text("Alert Center >", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                    Text("In-App Weather & Spray Alerts", fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Text("Real-time warnings for severe weather, rain & spray windows", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Switch(checked = notificationsEnabled, onCheckedChange = { notificationsEnabled = it })
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                    Text("Quiet Hours (10:00 PM - 06:00 AM)", fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Text("Non-critical agricultural advisories silenced during night", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Surface(shape = RoundedCornerShape(6.dp), color = EmeraldPrimary.copy(alpha = 0.15f)) {
                                    Text("Active", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                        }
                    }
                }

                // 4. Units & Regional Settings Card
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Units & Regional Display", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)

                            // Area Units
                            Text("Land Area Unit", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("Acres", "Hectares", "Cents").forEach { unit ->
                                    val isSelected = selectedAreaUnit == unit
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedAreaUnit = unit },
                                        modifier = Modifier.weight(1f),
                                        label = { Text(unit, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) }
                                    )
                                }
                            }

                            // Theme Mode
                            Text("Theme & Appearance", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                        }
                    }
                }

                // 5. Account Security & Farm Records
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Account Security & Data", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.height(2.dp))

                            ProfileItemRow("Farm Records & Data Export", "Download field archive, reports or backup data", Icons.Default.Security) {
                                onNavigate(Screen30Activity::class.java)
                            }
                            ProfileItemRow("Password & Account Security", "Update farmer PIN, password & credential recovery", Icons.Default.Lock) {
                                onNavigate(ForgotPasswordActivity::class.java)
                            }
                        }
                    }
                }

                // 6. Farm Business & Support Tools
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Farm Business & Support", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.height(2.dp))

                            ProfileItemRow("Kisan Advisory Helpline (1800-180-1551)", "Direct chat with agricultural specialists", Icons.Default.Phone) {
                                onNavigate(Screen26Activity::class.java)
                            }
                            ProfileItemRow("Local Mandi Market Rates", "Daily wholesale commodity prices for Wheat, Rice, Corn", Icons.AutoMirrored.Filled.TrendingUp) {
                                onNavigate(Screen27Activity::class.java)
                            }
                            ProfileItemRow("Farm Budget & Expense Tracker", "Track seeds, fertilizers, labor, and crop profits", Icons.Default.MonetizationOn) {
                                onNavigate(Screen22Activity::class.java)
                            }
                        }
                    }
                }

                // 7. Sign Out Button
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
