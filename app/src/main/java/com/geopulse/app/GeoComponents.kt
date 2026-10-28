package com.geopulse.app

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geopulse.app.ui.theme.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// Unified Professional Design Tokens
val DarkBackground = Color(0xFF070D0A)
val DarkSurface = Color(0xFF112217)
val DarkSurfaceElevated = Color(0xFF193223)
val CardBorderColor = Color(0xFF2E5E43)

val ForestDark = Color(0xFF0D1B12)
val EmeraldPrimary = Color(0xFF1B4D3E)
val LeafGreen = Color(0xFF2E7D32)
val LimeAccent = Color(0xFF81C784)
val GoldAccent = Color(0xFFFFCF55)
val RedAccent = Color(0xFFFF795C)
val CyanAccent = Color(0xFF6BD9F5)
val PineDark = Color(0xFF070D0A)

val TextLight = Color(0xFFFFFFFF)
val TextMuted = Color(0xFF94A3B8)

val GradientHeroStart = Color(0xFF0D2818)
val GradientHeroMid = Color(0xFF1B4D3E)
val GradientHeroEnd = Color(0xFF09160E)

val CardGradientStart = Color(0xFF152E20)
val CardGradientEnd = Color(0xFF0E1F16)

// Compatibility Aliases
val LightBackground = DarkBackground
val PrimaryDark = TextLight
val AccentGreen = LimeAccent
val SurfaceCard = DarkSurface
val GradientGreenStart = GradientHeroStart
val GradientGreenEnd = GradientHeroMid

/**
 * High-performance glass surface modifier.
 */
@Composable
fun Modifier.glassSurface(
    cornerRadius: Dp = 18.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
    borderColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
): Modifier {
    return this
        .clip(RoundedCornerShape(cornerRadius))
        .background(backgroundColor)
        .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(cornerRadius))
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
                Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, fontSize = 18.sp)
                if (subtitle != null) {
                    Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                }
            }
        },
        navigationIcon = {
            if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                }
            }
        },
        actions = {
            if (onActionClick != null) {
                IconButton(onClick = onActionClick) {
                    Icon(actionIcon, contentDescription = "Action", tint = MaterialTheme.colorScheme.onBackground)
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

@Composable
fun MetricChip(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = DarkSurface,
        border = BorderStroke(1.dp, CardBorderColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, fontSize = 10.sp, color = TextMuted)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextLight)
        }
    }
}

/**
 * Compatibility bridge for legacy Screen 8, 9, 10 callers.
 * Forwards directly to Screen7Activity with the corresponding TARGET_TAB.
 */
@Composable
fun GeoBottomBar(currentScreen: Int, activity: ComponentActivity) {
    val targetTab = when (currentScreen) {
        8 -> 1
        9 -> 2
        10 -> 3
        else -> 0
    }
    LaunchedEffect(currentScreen) {
        val intent = Intent(activity, Screen7Activity::class.java).apply {
            putExtra("TARGET_TAB", targetTab)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        activity.startActivity(intent)
        activity.finish()
    }
}

/**
 * Calculates the destination page index from gesture dynamics:
 * - Dragging left (totalDragPx < 0) advances towards next pages.
 * - Dragging right (totalDragPx > 0) advances towards previous pages.
 * - Deliberate fast flick (|velocityX| > flickThreshold) advances in flick direction.
 * - Settles smoothly to nearest destination if drag meets distance threshold (> 35% item width).
 * - Bounds strictly clamped to [minPage, maxPage]; zero wrap-around.
 */
fun calculateDragTargetPage(
    startPage: Int,
    totalDragPx: Float,
    itemWidthPx: Float,
    velocityX: Float,
    flickThreshold: Float = 450f,
    minPage: Int = 0,
    maxPage: Int = 3
): Int {
    return when {
        velocityX < -flickThreshold -> (startPage + 1).coerceAtMost(maxPage)
        velocityX > flickThreshold -> (startPage - 1).coerceAtLeast(minPage)
        else -> {
            val deltaPages = -totalDragPx / itemWidthPx
            (startPage + deltaPages).roundToInt().coerceIn(minPage, maxPage)
        }
    }
}

/**
 * Responsive Draggable & Tappable Bottom Navigation Dock.
 * - The entire dock is a horizontal drag surface allowing physical scrubbing between destinations.
 * - Also supports direct tap-to-select: tapping an icon animates immediately to that destination.
 * - Responsive: Constrained to a max width of 500dp and centered on tablets, full width on phones.
 * - While dragging, the indicator tracks the finger continuously in real-time.
 * - Screen content transitions in real-time sync with the pointer.
 * - Release settles smoothly with tween (280ms) and handles velocity flicks (> 450 px/s).
 */
@Composable
fun DraggableBottomDock(
    pagerState: PagerState,
    coroutineScope: CoroutineScope
) {
    val totalTabs = 4
    val tabs = remember { listOf("Home", "Map", "Activity", "Profile") }
    val icons = remember {
        listOf(
            Icons.Default.Eco,
            Icons.Default.Place,
            Icons.Default.Assessment,
            Icons.Default.Person
        )
    }

    // Continuous fractional indicator position in range [0f, 3f]
    val indicatorFraction = remember { Animatable(pagerState.currentPage.toFloat()) }
    var isDragging by remember { mutableStateOf(false) }

    // Sync indicator position with pager when not actively dragging
    LaunchedEffect(pagerState.currentPage, pagerState.currentPageOffsetFraction) {
        if (!isDragging) {
            val target = (pagerState.currentPage + pagerState.currentPageOffsetFraction).coerceIn(0f, 3f)
            indicatorFraction.snapTo(target)
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        val density = LocalDensity.current
        val maxDockWidth = 500.dp
        val dockWidth = if (maxWidth > maxDockWidth) maxDockWidth else maxWidth
        val itemWidth = dockWidth / totalTabs
        val itemWidthPx = with(density) { itemWidth.toPx() }

        Surface(
            modifier = Modifier
                .width(dockWidth)
                .height(68.dp)
                .pointerInput(pagerState) {
                    val velocityTracker = VelocityTracker()
                    var dragJob: Job? = null

                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        velocityTracker.resetTracking()
                        velocityTracker.addPosition(down.uptimeMillis, down.position)

                        var totalDragPx = 0f
                        var dragStarted = false
                        val startPage = pagerState.currentPage.toFloat()

                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break

                            if (!change.pressed) {
                                // Pointer Up
                                if (!dragStarted) {
                                    // Tapped on tab without dragging: navigate immediately to tapped destination
                                    val tappedIndex = (down.position.x / itemWidthPx).toInt().coerceIn(0, totalTabs - 1)
                                    coroutineScope.launch {
                                        launch {
                                            indicatorFraction.animateTo(
                                                targetValue = tappedIndex.toFloat(),
                                                animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
                                            )
                                        }
                                        launch {
                                            pagerState.animateScrollToPage(
                                                page = tappedIndex,
                                                animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
                                            )
                                        }
                                    }
                                } else {
                                    // Drag gesture ended: calculate target page using distance & flick velocity
                                    val velocityX = velocityTracker.calculateVelocity().x
                                    val targetPage = calculateDragTargetPage(
                                        startPage = startPage.toInt(),
                                        totalDragPx = totalDragPx,
                                        itemWidthPx = itemWidthPx,
                                        velocityX = velocityX
                                    )

                                    dragJob?.cancel()
                                    coroutineScope.launch {
                                        launch {
                                            indicatorFraction.animateTo(
                                                targetValue = targetPage.toFloat(),
                                                animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
                                            )
                                        }
                                        launch {
                                            pagerState.animateScrollToPage(
                                                page = targetPage,
                                                animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
                                            )
                                        }
                                        isDragging = false
                                    }
                                }
                                break
                            }

                            val deltaX = change.position.x - change.previousPosition.x
                            totalDragPx += deltaX
                            velocityTracker.addPosition(change.uptimeMillis, change.position)

                            if (!dragStarted && kotlin.math.abs(totalDragPx) > 10f) {
                                dragStarted = true
                                isDragging = true
                            }

                            if (dragStarted) {
                                change.consume()
                                val targetFraction = (startPage - (totalDragPx / itemWidthPx)).coerceIn(0f, 3f)

                                dragJob?.cancel()
                                dragJob = coroutineScope.launch {
                                    indicatorFraction.snapTo(targetFraction)
                                    val page = targetFraction.roundToInt().coerceIn(0, 3)
                                    val pageOffset = (targetFraction - page).coerceIn(-0.5f, 0.5f)
                                    pagerState.scrollToPage(page, pageOffset)
                                }
                            }
                        }
                    }
                },
            shape = RoundedCornerShape(34.dp),
            color = DarkSurface.copy(alpha = 0.96f),
            shadowElevation = 14.dp,
            border = BorderStroke(1.dp, CardBorderColor)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Sliding indicator pill following finger continuously
                val pillWidth = itemWidth - 8.dp
                val indicatorOffsetDp = (itemWidth * indicatorFraction.value) + 4.dp

                Box(
                    modifier = Modifier
                        .offset(x = indicatorOffsetDp)
                        .padding(vertical = 8.dp)
                        .width(pillWidth)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(26.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(EmeraldPrimary, LeafGreen)
                            )
                        )
                        .border(1.dp, LimeAccent.copy(alpha = 0.5f), RoundedCornerShape(26.dp))
                )

                // 4 Interactive Destination Slots
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    tabs.forEachIndexed { index, label ->
                        val isCurrent = (indicatorFraction.value.roundToInt() == index)
                        val textColor = if (isCurrent) Color.White else TextMuted
                        val iconTint = if (isCurrent) LimeAccent else TextMuted

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = icons[index],
                                    contentDescription = label,
                                    tint = iconTint,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                    color = textColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// 20 Specialized Interactive Modules for Screens 11 through 30
// -------------------------------------------------------------------------

@Composable
fun VegetationHealthModule() {
    var ndviThreshold by remember { mutableStateOf(0.84f) }
    Card(modifier = Modifier.fillMaxWidth().glassSurface(), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Interactive NDVI Health Analysis", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Crop Vigor Index", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 14.sp)
                Text("NDVI ${(ndviThreshold * 100).toInt() / 100f} - Optimal", fontWeight = FontWeight.Bold, color = LimeAccent, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Slider(value = ndviThreshold, onValueChange = { ndviThreshold = it }, valueRange = 0.2f..1.0f, colors = SliderDefaults.colors(thumbColor = LimeAccent, activeTrackColor = EmeraldPrimary))
            Spacer(modifier = Modifier.height(12.dp))
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
    Card(modifier = Modifier.fillMaxWidth().glassSurface(), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Root-Zone Soil Moisture Probe", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("0-10cm", "10-30cm", "30-60cm").forEach { depth ->
                    FilterChip(selected = selectedDepth == depth, onClick = { selectedDepth = depth }, label = { Text(depth) })
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricChip("Volumetric Water", "72% FC", Modifier.weight(1f))
                MetricChip("Soil Temp", "18.4°C", Modifier.weight(1f))
                MetricChip("Salinity EC", "1.2 dS/m", Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun IrrigationControlModule() {
    var isPivotActive by remember { mutableStateOf(true) }
    var flowRate by remember { mutableStateOf(320f) }
    Card(modifier = Modifier.fillMaxWidth().glassSurface(), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Center Pivot #01", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text("North Quadrant · 120 Acres", fontSize = 12.sp, color = TextMuted)
                }
                Switch(checked = isPivotActive, onCheckedChange = { isPivotActive = it })
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text("Flow Rate: ${flowRate.toInt()} L/min", fontSize = 13.sp, color = TextLight, fontWeight = FontWeight.SemiBold)
            Slider(value = flowRate, onValueChange = { flowRate = it }, valueRange = 100f..500f, colors = SliderDefaults.colors(thumbColor = LimeAccent, activeTrackColor = EmeraldPrimary))
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricChip("Pressure", "42 PSI", Modifier.weight(1f))
                MetricChip("Delivered", "18,450 L", Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun WeatherRadarModule() {
    var selectedView by remember { mutableStateOf("Precipitation") }
    Card(modifier = Modifier.fillMaxWidth().glassSurface(), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Hyperlocal Weather Radar", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Precipitation", "Wind Speed", "Dew Point").forEach { view ->
                    FilterChip(selected = selectedView == view, onClick = { selectedView = view }, label = { Text(view) })
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricChip("Wind", "18 km/h NNW", Modifier.weight(1f))
                MetricChip("Barometer", "1014 hPa", Modifier.weight(1f))
                MetricChip("Rain Chance", "15%", Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun PestDetectionModule() {
    var scanSimulated by remember { mutableStateOf(false) }
    Card(modifier = Modifier.fillMaxWidth().glassSurface(), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("AI Pest & Disease Detection", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Edge computer vision model scanning multispectral leaf reflections.", fontSize = 13.sp, color = TextMuted)
            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = { scanSimulated = !scanSimulated },
                modifier = Modifier.fillMaxWidth().height(46.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (scanSimulated) LeafGreen else EmeraldPrimary)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (scanSimulated) "Scan Completed: Clean" else "Run AI Canopy Scan", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(14.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricChip("Armyworm", "0% Risk", Modifier.weight(1f))
                MetricChip("Aphid Pressure", "Low (4%)", Modifier.weight(1f))
                MetricChip("Rust Fungus", "Negative", Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun YieldAnalyticsModule() {
    var projectedYield by remember { mutableStateOf(8.4f) }
    Card(modifier = Modifier.fillMaxWidth().glassSurface(), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Harvest Yield Projection", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(10.dp))
            Text("Target Model: ${projectedYield} t/ha (+8.4% vs 5-yr historical mean)", fontSize = 13.sp, color = LimeAccent, fontWeight = FontWeight.SemiBold)
            Slider(value = projectedYield, onValueChange = { projectedYield = (it * 10).roundToInt() / 10f }, valueRange = 5.0f..12.0f, colors = SliderDefaults.colors(thumbColor = LimeAccent, activeTrackColor = EmeraldPrimary))
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricChip("Projected Bushels", "172 bu/ac", Modifier.weight(1f))
                MetricChip("Confidence", "94.2%", Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun FieldBoundaryModule() {
    Card(modifier = Modifier.fillMaxWidth().glassSurface(), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("GIS Boundary Polygon Editor", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Field 1 · 12.5 ha · 4 GPS Geofence Vertices Synchronized", fontSize = 13.sp, color = TextMuted)
            Spacer(modifier = Modifier.height(14.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricChip("Perimeter", "1,840 m", Modifier.weight(1f))
                MetricChip("Enclosed Area", "12.5 ha", Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun SensorNodeModule() {
    Card(modifier = Modifier.fillMaxWidth().glassSurface(), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("LoRaWAN Sensor Node Mesh", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricChip("Active Nodes", "48 / 50", Modifier.weight(1f))
                MetricChip("Avg RSSI", "-78 dBm", Modifier.weight(1f))
                MetricChip("Gateway", "Online", Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun MachineryTrackingModule() {
    Card(modifier = Modifier.fillMaxWidth().glassSurface(), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Fleet & Autonomous Machinery", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricChip("John Deere 8R", "12.4 km/h", Modifier.weight(1f))
                MetricChip("Diesel Level", "82%", Modifier.weight(1f))
                MetricChip("Autosteer", "RTK Fix", Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun FertilizationPlanModule() {
    var nitrogenKg by remember { mutableStateOf(45f) }
    Card(modifier = Modifier.fillMaxWidth().glassSurface(), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Variable-Rate Nutrient Prescription", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(10.dp))
            Text("Nitrogen (N): ${nitrogenKg.toInt()} kg/ha (Targeting canopy vigor)", fontSize = 13.sp, color = TextLight)
            Slider(value = nitrogenKg, onValueChange = { nitrogenKg = it }, valueRange = 20f..80f, colors = SliderDefaults.colors(thumbColor = LimeAccent, activeTrackColor = EmeraldPrimary))
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricChip("Phosphorus (P)", "25 kg/ha", Modifier.weight(1f))
                MetricChip("Potassium (K)", "30 kg/ha", Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun HarvestLogisticsModule() {
    Card(modifier = Modifier.fillMaxWidth().glassSurface(), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Harvest & Grain Logistics", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricChip("Combine Bin", "88% Full", Modifier.weight(1f))
                MetricChip("Grain Cart #2", "En Route", Modifier.weight(1f))
                MetricChip("Moisture", "14.8%", Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun FinancialSummaryModule() {
    Card(modifier = Modifier.fillMaxWidth().glassSurface(), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Seasonal Financial ROI", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricChip("Gross Revenue", "$342,000", Modifier.weight(1f))
                MetricChip("Input Costs", "$184,000", Modifier.weight(1f))
                MetricChip("Net Margin", "+46.2%", Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun MultiFarmModule() {
    Card(modifier = Modifier.fillMaxWidth().glassSurface(), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Multi-Farm Portfolio Overview", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricChip("Properties", "3 Farms", Modifier.weight(1f))
                MetricChip("Aggregated", "1,250 ha", Modifier.weight(1f))
                MetricChip("Status", "Nominal", Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun DroneScanModule() {
    var isFlying by remember { mutableStateOf(false) }
    Card(modifier = Modifier.fillMaxWidth().glassSurface(), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("DJI Agras Multispectral UAV", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { isFlying = !isFlying },
                modifier = Modifier.fillMaxWidth().height(46.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (isFlying) LeafGreen else EmeraldPrimary)
            ) {
                Text(if (isFlying) "Mission Active: Scanning Path #4" else "Launch Autonomous Flight", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricChip("Battery", "94%", Modifier.weight(1f))
                MetricChip("Altitude", "60 m AGL", Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun CarbonSustainabilityModule() {
    Card(modifier = Modifier.fillMaxWidth().glassSurface(), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Soil Carbon Sequestration", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricChip("Organic Matter", "3.8% SOM", Modifier.weight(1f))
                MetricChip("Credits Earned", "42 tCO2e", Modifier.weight(1f))
                MetricChip("Registry", "Verra VCS", Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun AgronomistChatModule() {
    var messageText by remember { mutableStateOf("") }
    var sentMessage by remember { mutableStateOf<String?>(null) }
    Card(modifier = Modifier.fillMaxWidth().glassSurface(), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Consultation: Dr. Elena Vance", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Text("Certified Crop Adviser · Senior Soil Biologist", fontSize = 12.sp, color = TextMuted)
            Spacer(modifier = Modifier.height(14.dp))
            if (sentMessage != null) {
                Text("You: $sentMessage", fontSize = 13.sp, color = LimeAccent, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Dr. Vance: Received. Reviewing your Field 1 NDVI layer now.", fontSize = 13.sp, color = TextLight)
                Spacer(modifier = Modifier.height(12.dp))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = { Text("Ask agronomist...", fontSize = 13.sp) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                IconButton(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            sentMessage = messageText
                            messageText = ""
                        }
                    },
                    modifier = Modifier.size(52.dp).clip(RoundedCornerShape(12.dp)).background(EmeraldPrimary)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun MarketCommodityModule() {
    Card(modifier = Modifier.fillMaxWidth().glassSurface(), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("CBOT Live Commodity Futures", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricChip("Corn (ZCE)", "$4.42 /bu (+1.2%)", Modifier.weight(1f))
                MetricChip("Soy (ZSE)", "$11.85 /bu (-0.4%)", Modifier.weight(1f))
                MetricChip("Wheat (ZWE)", "$5.62 /bu (+0.8%)", Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun NotificationCenterModule() {
    Card(modifier = Modifier.fillMaxWidth().glassSurface(), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Farm Audit & Warning Center", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricChip("Critical", "0 Alarms", Modifier.weight(1f))
                MetricChip("Warnings", "2 Active", Modifier.weight(1f))
                MetricChip("System", "Nominal", Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun AppSettingsSyncModule() {
    var autoSync by remember { mutableStateOf(true) }
    Card(modifier = Modifier.fillMaxWidth().glassSurface(), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Telemetry Auto-Sync", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text("Push sensor data every 5 minutes", fontSize = 12.sp, color = TextMuted)
                }
                Switch(checked = autoSync, onCheckedChange = { autoSync = it })
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricChip("Cached Tiles", "142 MB", Modifier.weight(1f))
                MetricChip("Sync Queue", "0 Pending", Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun DataPrivacyDeletionModule() {
    var confirmed by remember { mutableStateOf(false) }
    Card(modifier = Modifier.fillMaxWidth().glassSurface(), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Data Export & Privacy Controls", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Export full precision GeoJSON boundaries or request account deletion.", fontSize = 13.sp, color = TextMuted)
            Spacer(modifier = Modifier.height(14.dp))
            OutlinedButton(
                onClick = { confirmed = true },
                modifier = Modifier.fillMaxWidth().height(46.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (confirmed) "GeoJSON Export Ready" else "Download Farm Archive (.geojson)", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun GenericTelemetryModule(title: String) {
    Card(modifier = Modifier.fillMaxWidth().glassSurface(), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("$title Diagnostics", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricChip("Status", "Active", Modifier.weight(1f))
                MetricChip("Sync", "Cloud Nominal", Modifier.weight(1f))
            }
        }
    }
}

// -------------------------------------------------------------------------
// Detail Screen Content for Screens 11 to 30 with working responsive layout
// -------------------------------------------------------------------------
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
                    subtitle = subtitle,
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
                    .padding(padding),
                contentAlignment = Alignment.TopCenter
            ) {
                val isTablet = maxWidth > 600.dp
                val contentWidth = if (isTablet) 640.dp else maxWidth
                val horizontalPadding = if (maxWidth < 360.dp) 12.dp else 16.dp
                val heroGradient = remember {
                    Brush.linearGradient(colors = listOf(GradientHeroStart, GradientHeroMid, GradientHeroEnd))
                }

                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(animationSpec = tween(400)) + slideInVertically(animationSpec = tween(400), initialOffsetY = { 30 })
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .width(contentWidth)
                            .fillMaxHeight(),
                        contentPadding = PaddingValues(
                            start = horizontalPadding,
                            end = horizontalPadding,
                            top = 16.dp,
                            bottom = 96.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            // Crisp Hero Card with professional gradient
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(heroGradient)
                                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                                    .padding(if (isTablet) 28.dp else 20.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = Color.White.copy(alpha = 0.15f),
                                        modifier = Modifier.size(68.dp),
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = title,
                                                tint = Color.White,
                                                modifier = Modifier.size(34.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text(
                                        text = title,
                                        fontSize = if (isTablet) 24.sp else 21.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = subtitle,
                                        fontSize = if (isTablet) 15.sp else 13.5.sp,
                                        color = Color.White.copy(alpha = 0.85f),
                                        textAlign = TextAlign.Center,
                                        lineHeight = 20.sp
                                    )
                                }
                            }
                        }

                        // Specific Interactive Telemetry Module matching this screen
                        item {
                            when {
                                title.contains("Vegetation", ignoreCase = true) -> VegetationHealthModule()
                                title.contains("Soil Moisture", ignoreCase = true) -> SoilMoistureModule()
                                title.contains("Irrigation", ignoreCase = true) -> IrrigationControlModule()
                                title.contains("Weather", ignoreCase = true) -> WeatherRadarModule()
                                title.contains("Pest", ignoreCase = true) -> PestDetectionModule()
                                title.contains("Yield", ignoreCase = true) -> YieldAnalyticsModule()
                                title.contains("Boundary", ignoreCase = true) -> FieldBoundaryModule()
                                title.contains("Sensor", ignoreCase = true) -> SensorNodeModule()
                                title.contains("Machinery", ignoreCase = true) -> MachineryTrackingModule()
                                title.contains("Fertilization", ignoreCase = true) -> FertilizationPlanModule()
                                title.contains("Harvest", ignoreCase = true) -> HarvestLogisticsModule()
                                title.contains("Financial", ignoreCase = true) -> FinancialSummaryModule()
                                title.contains("Multi-Farm", ignoreCase = true) -> MultiFarmModule()
                                title.contains("Drone", ignoreCase = true) -> DroneScanModule()
                                title.contains("Carbon", ignoreCase = true) -> CarbonSustainabilityModule()
                                title.contains("Agronomist", ignoreCase = true) -> AgronomistChatModule()
                                title.contains("Commodity", ignoreCase = true) -> MarketCommodityModule()
                                title.contains("Notification", ignoreCase = true) -> NotificationCenterModule()
                                title.contains("Settings", ignoreCase = true) || title.contains("Sync", ignoreCase = true) -> AppSettingsSyncModule()
                                title.contains("Privacy", ignoreCase = true) || title.contains("Deletion", ignoreCase = true) -> DataPrivacyDeletionModule()
                                else -> GenericTelemetryModule(title)
                            }
                        }

                        // Primary Action Button (Working)
                        item {
                            Spacer(modifier = Modifier.height(4.dp))
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
                                Text(nextButtonText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Secondary Return Action Button (Working)
                        item {
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
}
