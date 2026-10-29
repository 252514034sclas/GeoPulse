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
// Tab 1: Full GIS Interactive Map Screen
// -------------------------------------------------------------
data class GisZone(
    val id: String,
    val name: String,
    val acreage: String,
    val crop: String,
    val ndvi: String,
    val moisture: String,
    val status: String,
    val offsetFraction: Offset
)

@Composable
fun FullGisMapTab(
    onVegetationClick: () -> Unit,
    onBoundaryClick: () -> Unit
) {
    val zones = remember {
        listOf(
            GisZone("1", "North Sector Alpha", "120.4 Acres", "Corn (Dent)", "0.84 (Optimal)", "72%", "Healthy", Offset(0.35f, 0.35f)),
            GisZone("2", "East Field Beta", "84.2 Acres", "Soybeans", "0.78 (Good)", "65%", "Monitored", Offset(0.68f, 0.42f)),
            GisZone("3", "South Center Pivot", "145.0 Acres", "Winter Wheat", "0.62 (Moderate)", "48%", "Needs Water", Offset(0.48f, 0.68f))
        )
    }

    var selectedLayer by remember { mutableStateOf("NDVI Heatmap") }
    var selectedZone by remember { mutableStateOf(zones[0]) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(EmeraldPrimary.copy(alpha = 0.25f), DarkBackground),
                    radius = 900f
                )
            )
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            item {
                GeoTopBar(
                    title = "Geospatial Field Map",
                    subtitle = "GPS: 37.7749° N, 122.4194° W · 3 Zones",
                    actionIcon = Icons.Default.MyLocation
                )
            }

            // Layer Selector Chips
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(listOf("NDVI Heatmap", "Soil Moisture", "Satellite RGB", "Topography")) { layer ->
                        FilterChip(
                            selected = selectedLayer == layer,
                            onClick = { selectedLayer = layer },
                            label = { Text(layer, fontWeight = FontWeight.SemiBold) }
                        )
                    }
                }
            }

            // Interactive GIS Vector Map Canvas
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF0B1910))
                        .border(1.dp, CardBorderColor, RoundedCornerShape(16.dp))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height

                        // Grid lines
                        val step = 40f
                        var x = 0f
                        while (x < w) {
                            drawLine(Color(0x15FFFFFF), Offset(x, 0f), Offset(x, h), strokeWidth = 1f)
                            x += step
                        }
                        var y = 0f
                        while (y < h) {
                            drawLine(Color(0x15FFFFFF), Offset(0f, y), Offset(w, y), strokeWidth = 1f)
                            y += step
                        }

                        // Parcel 1
                        val path1 = Path().apply {
                            moveTo(w * 0.15f, h * 0.15f)
                            lineTo(w * 0.55f, h * 0.12f)
                            lineTo(w * 0.50f, h * 0.48f)
                            lineTo(w * 0.18f, h * 0.45f)
                            close()
                        }
                        val fill1 = if (selectedLayer == "NDVI Heatmap") Color(0x884CAF50) else Color(0x8800BCD4)
                        drawPath(path1, fill1)
                        drawPath(path1, if (selectedZone.id == "1") Color.White else EmeraldPrimary, style = Stroke(width = if (selectedZone.id == "1") 3.5f else 1.5f))

                        // Parcel 2
                        val path2 = Path().apply {
                            moveTo(w * 0.58f, h * 0.18f)
                            lineTo(w * 0.88f, h * 0.22f)
                            lineTo(w * 0.85f, h * 0.55f)
                            lineTo(w * 0.54f, h * 0.52f)
                            close()
                        }
                        val fill2 = if (selectedLayer == "NDVI Heatmap") Color(0x888BC34A) else Color(0x8803A9F4)
                        drawPath(path2, fill2)
                        drawPath(path2, if (selectedZone.id == "2") Color.White else EmeraldPrimary, style = Stroke(width = if (selectedZone.id == "2") 3.5f else 1.5f))

                        // Parcel 3 Pivot
                        val pivotCenter = Offset(w * 0.48f, h * 0.72f)
                        val pivotRadius = w * 0.20f
                        drawCircle(Color(0x88FFC107), radius = pivotRadius, center = pivotCenter)
                        drawCircle(if (selectedZone.id == "3") Color.White else LimeAccent, radius = pivotRadius, center = pivotCenter, style = Stroke(width = if (selectedZone.id == "3") 3.5f else 1.5f))
                    }

                    // Interactive Zone Pins
                    zones.forEach { zone ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(
                                    start = (zone.offsetFraction.x * 240).dp,
                                    top = (zone.offsetFraction.y * 190).dp
                                )
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (selectedZone.id == zone.id) LimeAccent else PineDark.copy(alpha = 0.85f),
                                border = BorderStroke(1.dp, if (selectedZone.id == zone.id) Color.White else LimeAccent),
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

            // Selected Zone Inspector Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().glassSurface(cornerRadius = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(selectedZone.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextLight)
                                Text("${selectedZone.acreage} · ${selectedZone.crop}", fontSize = 13.sp, color = TextMuted)
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = EmeraldPrimary.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    selectedZone.status,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    color = EmeraldPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            MetricChip("Canopy NDVI", selectedZone.ndvi, Modifier.weight(1f))
                            MetricChip("Soil Moisture", selectedZone.moisture, Modifier.weight(1f))
                            MetricChip("Implement", "Pivot 01", Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onVegetationClick,
                                modifier = Modifier.weight(1f).height(46.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = LeafGreen)
                            ) {
                                Text("Vegetation", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = onBoundaryClick,
                                modifier = Modifier.weight(1f).height(46.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, CardBorderColor)
                            ) {
                                Text("Edit Boundary", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextLight)
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(EmeraldPrimary.copy(alpha = 0.25f), DarkBackground),
                    radius = 900f
                )
            )
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
                        color = DarkSurface,
                        border = BorderStroke(1.dp, CardBorderColor)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Field 1 · Corn (Maize)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextLight)
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = EmeraldPrimary.copy(alpha = 0.25f)
                                ) {
                                    Text(
                                        "Vegetative Stage (Day 52)",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        color = LimeAccent,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Today's conditions are optimal for foliar nutrient spraying and light weeding. Expected rain on Friday will provide natural irrigation.",
                                fontSize = 13.sp,
                                color = TextMuted,
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
                    Text("Today's Actionable Tasks (" + tasks.size + ")", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextLight)
                }

                items(tasks) { task ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onTaskClick(task.targetClass) },
                        shape = RoundedCornerShape(16.dp),
                        color = DarkSurface,
                        border = BorderStroke(1.dp, CardBorderColor)
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
                                    Text(task.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextLight)
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
                                Text(task.subtitle, fontSize = 12.sp, color = TextMuted, lineHeight = 16.sp)
                            }

                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(EmeraldPrimary.copy(alpha = 0.25f), DarkBackground),
                    radius = 900f
                )
            )
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
                        color = DarkSurface,
                        border = BorderStroke(1.dp, CardBorderColor)
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
                                Text("Nithish Kumar", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Green Valley Farm · 12.5 ha", fontSize = 13.sp, color = TextMuted)
                                Text("Primary Crops: Corn & Soybean", fontSize = 11.sp, color = LimeAccent, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                // Appearance & Theme Mode Selector (3-way: Dark, Light, System)
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = DarkSurface,
                        border = BorderStroke(1.dp, CardBorderColor)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Theme & Appearance", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextLight)
                            Text("Choose your preferred display theme", fontSize = 12.sp, color = TextMuted)

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

                            HorizontalDivider(color = CardBorderColor.copy(alpha = 0.5f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Weather & Spray Alerts", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextLight)
                                    Text("Frost, rain and disease notifications", fontSize = 12.sp, color = TextMuted)
                                }
                                Switch(checked = notificationsEnabled, onCheckedChange = { notificationsEnabled = it })
                            }

                            HorizontalDivider(color = CardBorderColor.copy(alpha = 0.5f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Offline Satellite Map Caching", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextLight)
                                    Text("Save field satellite maps for offline use", fontSize = 12.sp, color = TextMuted)
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
                        color = DarkSurface,
                        border = BorderStroke(1.dp, CardBorderColor)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Practical Farm Tools", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextLight)
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
                .background(EmeraldPrimary.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = LimeAccent, modifier = Modifier.size(17.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp, color = TextLight)
            Text(subtitle, fontSize = 11.5.sp, color = TextMuted)
        }

        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
    }
}

