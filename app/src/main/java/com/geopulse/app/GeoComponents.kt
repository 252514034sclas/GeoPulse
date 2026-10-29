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
val CardBorderColor = Color(0xFF1E3F2B)

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
                .height(62.dp)
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
            shape = RoundedCornerShape(31.dp),
            color = Color(0xFA0B160F),
            shadowElevation = 10.dp,
            border = BorderStroke(1.dp, Color(0xFF1E3F2B))
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Sliding indicator pill following finger continuously
                val pillWidth = itemWidth - 8.dp
                val indicatorOffsetDp = (itemWidth * indicatorFraction.value) + 4.dp

                Box(
                    modifier = Modifier
                        .offset(x = indicatorOffsetDp)
                        .padding(vertical = 6.dp)
                        .width(pillWidth)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(25.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(EmeraldPrimary, LeafGreen)
                            )
                        )
                        .border(1.dp, LimeAccent.copy(alpha = 0.5f), RoundedCornerShape(25.dp))
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
// 20 Practical Satellite & Layperson Farming Modules (Screens 11 through 30)
// -------------------------------------------------------------------------

@Composable
fun VegetationHealthModule() {
    var growthStageSlider by remember { mutableStateOf(0.84f) }
    Card(modifier = Modifier.fillMaxWidth().glassSurface(cornerRadius = 16.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text("Satellite Crop Health & Vigor (NDVI)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Sentinel-2 satellite scans measure plant greenness and canopy chlorophyll.", fontSize = 12.5.sp, color = TextMuted)
            Spacer(modifier = Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Canopy Greenness Level", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f), fontSize = 13.5.sp)
                val statusText = if (growthStageSlider > 0.75f) "Optimal Vigor" else if (growthStageSlider > 0.5f) "Moderate" else "Yellowing / Stressed"
                val statusColor = if (growthStageSlider > 0.75f) LimeAccent else if (growthStageSlider > 0.5f) GoldAccent else RedAccent
                Text("${(growthStageSlider * 100).toInt()}% · $statusText", fontWeight = FontWeight.Bold, color = statusColor, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Slider(
                value = growthStageSlider,
                onValueChange = { growthStageSlider = it },
                valueRange = 0.2f..1.0f,
                colors = SliderDefaults.colors(thumbColor = LimeAccent, activeTrackColor = EmeraldPrimary)
            )

            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = DarkSurfaceElevated,
                border = BorderStroke(1.dp, CardBorderColor)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Farmer Recommendation", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = LimeAccent)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "Crop vigor is strong across Field 1. Plants have healthy dark green leaves. No nitrogen deficiency detected. Maintain regular weed-free conditions.",
                        fontSize = 12.sp,
                        color = TextLight,
                        lineHeight = 17.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricChip("Chlorophyll", "42.5 SPAD", Modifier.weight(1f))
                MetricChip("Biomass Vigor", "Optimal", Modifier.weight(1f))
                MetricChip("Next Scan", "In 3 Days", Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun SoilMoistureModule() {
    var selectedDepth by remember { mutableStateOf("0-15 cm (Topsoil)") }
    Card(modifier = Modifier.fillMaxWidth().glassSurface(cornerRadius = 16.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text("Satellite Soil Moisture (NDWI Index)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Estimates moisture content in topsoil layers from satellite water absorption bands.", fontSize = 12.5.sp, color = TextMuted)
            Spacer(modifier = Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("0-15 cm (Topsoil)", "15-40 cm (Root Zone)").forEach { depth ->
                    FilterChip(
                        selected = selectedDepth == depth,
                        onClick = { selectedDepth = depth },
                        label = { Text(depth, fontSize = 12.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = DarkSurfaceElevated,
                border = BorderStroke(1.dp, CardBorderColor)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Watering Advice", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CyanAccent)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "Topsoil moisture is 72% (Adequate). Roots have sufficient hydration for the next 48 hours. Hold watering today to prevent waterlogging.",
                        fontSize = 12.sp,
                        color = TextLight,
                        lineHeight = 17.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricChip("Moisture", "72% (Good)", Modifier.weight(1f))
                MetricChip("Soil Status", "Moist", Modifier.weight(1f))
                MetricChip("Watering Due", "In 48 Hours", Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun IrrigationTimingModule() {
    Card(modifier = Modifier.fillMaxWidth().glassSurface(cornerRadius = 16.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text("Safe Watering Timing & Evaporation Guide", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Time irrigation with weather to prevent water loss and leaf scorch.", fontSize = 12.5.sp, color = TextMuted)
            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = DarkSurfaceElevated,
                border = BorderStroke(1.dp, CardBorderColor)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Best Irrigation Window: Tomorrow 6:00 AM - 8:30 AM", fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = LimeAccent)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Early morning watering allows moisture to soak deep before sun rises. Avoid 11 AM - 3 PM watering when high heat causes up to 40% evaporation loss.",
                        fontSize = 12.sp,
                        color = TextLight,
                        lineHeight = 17.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricChip("Best Window", "Early AM", Modifier.weight(1f))
                MetricChip("Evaporation", "Low (6%)", Modifier.weight(1f))
                MetricChip("Water Saved", "12,000 L", Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun WeatherSprayingModule() {
    Card(modifier = Modifier.fillMaxWidth().glassSurface(cornerRadius = 16.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text("Weather & Safe Spraying Forecast", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Hourly rainfall and wind speed forecast for safe crop spraying.", fontSize = 12.5.sp, color = TextMuted)
            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = DarkSurfaceElevated,
                border = BorderStroke(1.dp, CardBorderColor)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Today's Spray Status: Safe to Spray", fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = LimeAccent)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Wind is calm at 8 km/h and rain probability is low (10%). Safe to apply nutrient sprays today. Rain expected Friday afternoon will provide natural watering.",
                        fontSize = 12.sp,
                        color = TextLight,
                        lineHeight = 17.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricChip("Wind Speed", "8 km/h (Calm)", Modifier.weight(1f))
                MetricChip("Rain Friday", "12 mm Showers", Modifier.weight(1f))
                MetricChip("Humidity", "58%", Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun PestAdvisoryModule() {
    Card(modifier = Modifier.fillMaxWidth().glassSurface(cornerRadius = 16.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text("Pest & Disease Advisory Guide", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Early warning based on weather humidity and crop stress zones.", fontSize = 12.5.sp, color = TextMuted)
            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = DarkSurfaceElevated,
                border = BorderStroke(1.dp, CardBorderColor)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Fall Armyworm & Leaf Spot: Low-to-Medium Risk", fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = GoldAccent)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Inspect under-leaf canopy in Field 2 for small pinholes. Organic remedy: Spray 5ml Neem oil per liter of water in early morning. Keep beneficial ladybugs unharmed.",
                        fontSize = 12.sp,
                        color = TextLight,
                        lineHeight = 17.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricChip("Pest Risk", "Low", Modifier.weight(1f))
                MetricChip("Fungal Risk", "Negative", Modifier.weight(1f))
                MetricChip("Remedy", "Neem Bio-Spray", Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun YieldEstimatorModule() {
    Card(modifier = Modifier.fillMaxWidth().glassSurface(cornerRadius = 16.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text("Expected Harvest & Yield Estimator", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Predicted harvest production based on satellite vegetative growth curve.", fontSize = 12.5.sp, color = TextMuted)
            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = DarkSurfaceElevated,
                border = BorderStroke(1.dp, CardBorderColor)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Estimated Production: 34 - 36 Quintals / acre (Corn)", fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = LimeAccent)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Growth trajectory is +8.4% above normal 5-year average. Expected harvest window is in ~28 days (Nov 24 - Nov 30). Begin harvest when grain moisture drops below 16%.",
                        fontSize = 12.sp,
                        color = TextLight,
                        lineHeight = 17.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricChip("Per Acre", "35 Q/ac", Modifier.weight(1f))
                MetricChip("Field Total", "438 Quintals", Modifier.weight(1f))
                MetricChip("Harvest In", "~28 Days", Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun FieldBoundaryModule() {
    Card(modifier = Modifier.fillMaxWidth().glassSurface(cornerRadius = 16.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text("Field Boundary & Land Area", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(4.dp))
            Text("GPS perimeter delineation and accurate acreage calculation for your fields.", fontSize = 12.5.sp, color = TextMuted)
            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = DarkSurfaceElevated,
                border = BorderStroke(1.dp, CardBorderColor)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Field 1: 12.5 Hectares (30.8 Acres)", fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = LimeAccent)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Perimeter: 1,840 meters · 4 GPS boundary vertices verified against Sentinel-2 satellite grid. Ready for input dosage calculations.",
                        fontSize = 12.sp,
                        color = TextLight,
                        lineHeight = 17.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricChip("Hectares", "12.5 ha", Modifier.weight(1f))
                MetricChip("Acres", "30.8 ac", Modifier.weight(1f))
                MetricChip("Boundary", "Verified", Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun CropCalendarModule() {
    Card(modifier = Modifier.fillMaxWidth().glassSurface(cornerRadius = 16.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text("Crop Calendar & Growth Stages", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Follow your crop milestones from sowing to maturity.", fontSize = 12.5.sp, color = TextMuted)
            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = DarkSurfaceElevated,
                border = BorderStroke(1.dp, CardBorderColor)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Current Stage: Tasseling & Silking (Day 52 of 105)", fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = LimeAccent)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Grain filling begins in ~10 days. Ensure consistent soil moisture during this stage to prevent poor ear fill.",
                        fontSize = 12.sp,
                        color = TextLight,
                        lineHeight = 17.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricChip("Current", "Tasseling", Modifier.weight(1f))
                MetricChip("Days Done", "52 Days", Modifier.weight(1f))
                MetricChip("To Harvest", "53 Days", Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun FieldOperationModule() {
    Card(modifier = Modifier.fillMaxWidth().glassSurface(cornerRadius = 16.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text("Field Operations & Plowing Planner", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Schedule tilling, weeding, and tractor work based on soil moisture.", fontSize = 12.5.sp, color = TextMuted)
            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = DarkSurfaceElevated,
                border = BorderStroke(1.dp, CardBorderColor)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Soil Condition: Firm & Workable", fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = LimeAccent)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Optimal time for inter-row weeding and earthing-up. Avoid heavy machinery on Friday when rains may cause soil compaction.",
                        fontSize = 12.sp,
                        color = TextLight,
                        lineHeight = 17.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricChip("Soil Status", "Workable", Modifier.weight(1f))
                MetricChip("Plowing", "Optimal", Modifier.weight(1f))
                MetricChip("Weeding", "Due Today", Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun FertilizationPlanModule() {
    var nitrogenKg by remember { mutableStateOf(45f) }
    Card(modifier = Modifier.fillMaxWidth().glassSurface(cornerRadius = 16.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text("Fertilizer & Nutrient Advisory", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Plain-language fertilizer dosages per acre tailored to your crop greenness.", fontSize = 12.5.sp, color = TextMuted)
            Spacer(modifier = Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Recommended Urea (Nitrogen)", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f), fontSize = 13.5.sp)
                Text("${nitrogenKg.toInt()} kg / acre (1 Bag)", fontWeight = FontWeight.Bold, color = LimeAccent, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Slider(
                value = nitrogenKg,
                onValueChange = { nitrogenKg = it },
                valueRange = 25f..65f,
                colors = SliderDefaults.colors(thumbColor = LimeAccent, activeTrackColor = EmeraldPrimary)
            )

            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = DarkSurfaceElevated,
                border = BorderStroke(1.dp, CardBorderColor)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Application Timing Tip", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = LimeAccent)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "Apply Urea along rows 12 hours before expected light rain or immediately before morning irrigation for best root uptake.",
                        fontSize = 12.sp,
                        color = TextLight,
                        lineHeight = 17.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricChip("Urea (N)", "45 kg/ac", Modifier.weight(1f))
                MetricChip("DAP (P)", "30 kg/ac", Modifier.weight(1f))
                MetricChip("Potash (K)", "20 kg/ac", Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun HarvestStorageModule() {
    Card(modifier = Modifier.fillMaxWidth().glassSurface(cornerRadius = 16.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text("Harvest Preparation & Storage Tips", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Post-harvest guidelines to prevent mold and storage grain losses.", fontSize = 12.5.sp, color = TextMuted)
            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = DarkSurfaceElevated,
                border = BorderStroke(1.dp, CardBorderColor)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Grain Moisture Safety Rule", fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = LimeAccent)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Sun-dry harvested grains on tarpaulins for 2-3 days until moisture drops below 14%. Store bags on raised wooden pallets in a dry, ventilated shed.",
                        fontSize = 12.sp,
                        color = TextLight,
                        lineHeight = 17.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricChip("Safe Moisture", "< 14%", Modifier.weight(1f))
                MetricChip("Drying Time", "2-3 Days", Modifier.weight(1f))
                MetricChip("Storage Risk", "Low", Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun FarmBudgetModule() {
    Card(modifier = Modifier.fillMaxWidth().glassSurface(cornerRadius = 16.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text("Farm Budget & Crop Expense Tracker", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Track seed, fertilizer, and labor costs against expected market selling price.", fontSize = 12.5.sp, color = TextMuted)
            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = DarkSurfaceElevated,
                border = BorderStroke(1.dp, CardBorderColor)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Estimated Net Profit: +$320 / acre", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = LimeAccent)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Input Costs: $260/acre (Seed: $60, Nutrients: $110, Field Labor: $90) vs Expected Gross: $580/acre based on 35 Q/ac @ local mandi price.",
                        fontSize = 12.sp,
                        color = TextLight,
                        lineHeight = 17.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricChip("Input Costs", "$260/ac", Modifier.weight(1f))
                MetricChip("Gross Returns", "$580/ac", Modifier.weight(1f))
                MetricChip("Net Profit", "+$320/ac", Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun MyFieldsModule() {
    Card(modifier = Modifier.fillMaxWidth().glassSurface(cornerRadius = 16.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text("My Land Parcels & Registered Fields", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Overview of all your registered plots and planted crop varieties.", fontSize = 12.5.sp, color = TextMuted)
            Spacer(modifier = Modifier.height(14.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    Triple("Field 1 (North Sector)", "12.5 ha · Corn · Healthy", LimeAccent),
                    Triple("Field 2 (East Beta)", "8.4 ha · Soybean · Good", LimeAccent),
                    Triple("Field 3 (South Acreage)", "14.5 ha · Wheat · Moisture Low", GoldAccent)
                ).forEach { (name, desc, color) ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = DarkSurfaceElevated,
                        border = BorderStroke(1.dp, CardBorderColor)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextLight)
                                Text(desc, fontSize = 11.5.sp, color = TextMuted)
                            }
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricChip("Total Land", "35.4 ha", Modifier.weight(1f))
                MetricChip("Active Fields", "3 Plots", Modifier.weight(1f))
                MetricChip("Primary", "Corn", Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun SatelliteHistoryModule() {
    Card(modifier = Modifier.fillMaxWidth().glassSurface(cornerRadius = 16.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text("Satellite Growth History (30-Day Trend)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Review satellite greenness comparisons over the crop season.", fontSize = 12.5.sp, color = TextMuted)
            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = DarkSurfaceElevated,
                border = BorderStroke(1.dp, CardBorderColor)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Steady Growth: Canopy Expanded +56%", fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = LimeAccent)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Week 1 (Day 15): Emergence (NDVI 0.28)\nWeek 4 (Day 35): Rapid Canopy Spread (NDVI 0.62)\nToday (Day 52): Peak Vigor & Flowering (NDVI 0.84)",
                        fontSize = 12.sp,
                        color = TextLight,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricChip("30-Day Gain", "+56%", Modifier.weight(1f))
                MetricChip("Canopy Cover", "88%", Modifier.weight(1f))
                MetricChip("Growth Trend", "Optimal", Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun OrganicSoilModule() {
    Card(modifier = Modifier.fillMaxWidth().glassSurface(cornerRadius = 16.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text("Organic Soil Health & Composting", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Natural methods to build soil fertility without high chemical costs.", fontSize = 12.5.sp, color = TextMuted)
            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = DarkSurfaceElevated,
                border = BorderStroke(1.dp, CardBorderColor)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Soil Building Recommendations", fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = LimeAccent)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Incorporate farmyard cow manure (FYM) or vermicompost before tilling. Rotating with pulse/legume crops naturally fixes atmospheric nitrogen into the soil.",
                        fontSize = 12.sp,
                        color = TextLight,
                        lineHeight = 17.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricChip("Organic Carbon", "2.8% SOM", Modifier.weight(1f))
                MetricChip("Soil Type", "Clay Loam", Modifier.weight(1f))
                MetricChip("Microbes", "Active", Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun AgronomistChatModule() {
    var messageText by remember { mutableStateOf("") }
    var sentMessage by remember { mutableStateOf<String?>(null) }
    Card(modifier = Modifier.fillMaxWidth().glassSurface(cornerRadius = 16.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text("Kisan Advisory: Dr. Elena Vance", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Text("Certified Crop Adviser · Agricultural Specialist", fontSize = 12.sp, color = TextMuted)
            Spacer(modifier = Modifier.height(14.dp))
            if (sentMessage != null) {
                Text("You: $sentMessage", fontSize = 13.sp, color = LimeAccent, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Dr. Vance: Received. Your Field 1 satellite greenness looks optimal. Keep soil moist through the tasseling phase.", fontSize = 12.5.sp, color = TextLight, lineHeight = 17.sp)
                Spacer(modifier = Modifier.height(12.dp))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = { Text("Ask crop question...", fontSize = 12.5.sp) },
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
    Card(modifier = Modifier.fillMaxWidth().glassSurface(cornerRadius = 16.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text("Local Mandi Wholesale Selling Rates", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Today's crop selling prices at regional wholesale agricultural markets.", fontSize = 12.5.sp, color = TextMuted)
            Spacer(modifier = Modifier.height(14.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    Triple("Corn (Maize)", "$240 / quintal", "+3.2% ↗"),
                    Triple("Wheat (Sharbati)", "$290 / quintal", "Stable"),
                    Triple("Soybean (Yellow)", "$460 / quintal", "-0.5% ↘"),
                    Triple("Rice (Paddy)", "$275 / quintal", "+1.8% ↗")
                ).forEach { (crop, price, trend) ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = DarkSurfaceElevated,
                        border = BorderStroke(1.dp, CardBorderColor)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(crop, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = TextLight)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(price, fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp, color = TextLight)
                                Text(trend, fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = if (trend.contains("+")) LimeAccent else TextMuted)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricChip("Corn", "$240/Q", Modifier.weight(1f))
                MetricChip("Wheat", "$290/Q", Modifier.weight(1f))
                MetricChip("Soybean", "$460/Q", Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun NotificationCenterModule() {
    Card(modifier = Modifier.fillMaxWidth().glassSurface(cornerRadius = 16.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text("Weather & Crop Warning Center", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Critical agricultural alerts and weather warnings for your area.", fontSize = 12.5.sp, color = TextMuted)
            Spacer(modifier = Modifier.height(14.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = DarkSurfaceElevated,
                    border = BorderStroke(1.dp, CardBorderColor)
                ) {
                    Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Cloud, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(20.dp))
                        Column {
                            Text("Rain Advisory: 12mm Showers on Friday", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextLight)
                            Text("Cover harvested produce and complete foliar sprays before Friday.", fontSize = 11.5.sp, color = TextMuted)
                        }
                    }
                }
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = DarkSurfaceElevated,
                    border = BorderStroke(1.dp, CardBorderColor)
                ) {
                    Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Spa, contentDescription = null, tint = LimeAccent, modifier = Modifier.size(20.dp))
                        Column {
                            Text("Nitrogen Top-Dress Window: Field 1", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextLight)
                            Text("Optimal soil moisture for applying Urea split dose.", fontSize = 11.5.sp, color = TextMuted)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricChip("Alerts", "2 Active", Modifier.weight(1f))
                MetricChip("Weather Alert", "Rain Fri", Modifier.weight(1f))
                MetricChip("Crop Alert", "Fertilizer", Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun AppSettingsSyncModule() {
    val currentMode = ThemePreferences.themeMode
    Card(modifier = Modifier.fillMaxWidth().glassSurface(cornerRadius = 16.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text("App Appearance & Theme Mode", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Switch between Light, Dark, or System Default appearance.", fontSize = 12.5.sp, color = TextMuted)
            Spacer(modifier = Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    Triple(ThemeMode.LIGHT, "Light", Icons.Default.LightMode),
                    Triple(ThemeMode.DARK, "Dark", Icons.Default.DarkMode),
                    Triple(ThemeMode.SYSTEM, "System", Icons.Default.SettingsBrightness)
                ).forEach { (mode, label, icon) ->
                    val isSelected = currentMode == mode
                    FilterChip(
                        selected = isSelected,
                        onClick = { ThemePreferences.themeMode = mode },
                        modifier = Modifier.weight(1f),
                        leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(15.dp)) },
                        label = { Text(label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricChip("Current Theme", currentMode.name.lowercase().replaceFirstChar { it.uppercase() }, Modifier.weight(1f))
                MetricChip("Offline Maps", "Ready", Modifier.weight(1f))
                MetricChip("Language", "English", Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun DataPrivacyDeletionModule() {
    var exportCompleted by remember { mutableStateOf(false) }
    Card(modifier = Modifier.fillMaxWidth().glassSurface(cornerRadius = 16.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text("Farm Records & Data Export", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Export your field boundaries, spray logs, and expense records.", fontSize = 12.5.sp, color = TextMuted)
            Spacer(modifier = Modifier.height(14.dp))

            OutlinedButton(
                onClick = { exportCompleted = true },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (exportCompleted) "Farm Records Exported Successfully" else "Export Field Records (.geojson)", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
            }

            Spacer(modifier = Modifier.height(14.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricChip("Format", "GeoJSON", Modifier.weight(1f))
                MetricChip("Records", "Field 1 Diary", Modifier.weight(1f))
                MetricChip("Privacy", "Encrypted", Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun GenericFarmerModule(title: String) {
    Card(modifier = Modifier.fillMaxWidth().glassSurface(cornerRadius = 16.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text("$title Guide", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Satellite agronomic intelligence and recommendations for your farm.", fontSize = 12.5.sp, color = TextMuted)
            Spacer(modifier = Modifier.height(14.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricChip("Status", "Optimal", Modifier.weight(1f))
                MetricChip("Update", "Daily Sync", Modifier.weight(1f))
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
    nextButtonText: String = "View Related Advisory"
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }

    Box(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        Scaffold(
            topBar = {
                GeoTopBar(
                    title = title,
                    subtitle = subtitle,
                    onBackClick = {
                        activity.startActivity(
                            Intent(activity, Screen7Activity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                            }
                        )
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
                val contentWidth = if (isTablet) 600.dp else maxWidth
                val horizontalPadding = if (maxWidth < 360.dp) 12.dp else 16.dp
                val heroGradient = remember {
                    Brush.linearGradient(colors = listOf(GradientHeroStart, GradientHeroMid, GradientHeroEnd))
                }

                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(animationSpec = tween(350)) + slideInVertically(animationSpec = tween(350), initialOffsetY = { 20 })
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .width(contentWidth)
                            .fillMaxHeight(),
                        contentPadding = PaddingValues(
                            start = horizontalPadding,
                            end = horizontalPadding,
                            top = 12.dp,
                            bottom = 96.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        item {
                            // Hero Summary Card
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(heroGradient)
                                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                    .padding(if (isTablet) 24.dp else 18.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = Color.White.copy(alpha = 0.15f),
                                        modifier = Modifier.size(56.dp),
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = title,
                                                tint = Color.White,
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = title,
                                        fontSize = if (isTablet) 22.sp else 19.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = subtitle,
                                        fontSize = if (isTablet) 14.sp else 12.5.sp,
                                        color = Color.White.copy(alpha = 0.85f),
                                        textAlign = TextAlign.Center,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }

                        // Specific Interactive Layperson Satellite Module matching this screen
                        item {
                            when {
                                title.contains("Vegetation", ignoreCase = true) || title.contains("Crop Health", ignoreCase = true) -> VegetationHealthModule()
                                title.contains("Soil Moisture", ignoreCase = true) -> SoilMoistureModule()
                                title.contains("Watering", ignoreCase = true) || title.contains("Irrigation", ignoreCase = true) -> IrrigationTimingModule()
                                title.contains("Weather", ignoreCase = true) || title.contains("Radar", ignoreCase = true) || title.contains("Spraying", ignoreCase = true) -> WeatherSprayingModule()
                                title.contains("Pest", ignoreCase = true) || title.contains("Disease", ignoreCase = true) -> PestAdvisoryModule()
                                title.contains("Yield", ignoreCase = true) || title.contains("Harvest Estimator", ignoreCase = true) -> YieldEstimatorModule()
                                title.contains("Boundary", ignoreCase = true) || title.contains("Land Area", ignoreCase = true) -> FieldBoundaryModule()
                                title.contains("Calendar", ignoreCase = true) || title.contains("Stages", ignoreCase = true) || title.contains("Sensor", ignoreCase = true) -> CropCalendarModule()
                                title.contains("Operations", ignoreCase = true) || title.contains("Plowing", ignoreCase = true) || title.contains("Machinery", ignoreCase = true) -> FieldOperationModule()
                                title.contains("Fertiliz", ignoreCase = true) || title.contains("Nutrient", ignoreCase = true) -> FertilizationPlanModule()
                                title.contains("Storage", ignoreCase = true) || title.contains("Harvest Logistics", ignoreCase = true) || title.contains("Harvest Preparation", ignoreCase = true) -> HarvestStorageModule()
                                title.contains("Financial", ignoreCase = true) || title.contains("Budget", ignoreCase = true) -> FarmBudgetModule()
                                title.contains("Multi-Farm", ignoreCase = true) || title.contains("Parcels", ignoreCase = true) || title.contains("My Land", ignoreCase = true) -> MyFieldsModule()
                                title.contains("Drone", ignoreCase = true) || title.contains("History", ignoreCase = true) -> SatelliteHistoryModule()
                                title.contains("Carbon", ignoreCase = true) || title.contains("Organic", ignoreCase = true) || title.contains("Composting", ignoreCase = true) -> OrganicSoilModule()
                                title.contains("Agronomist", ignoreCase = true) || title.contains("Helpline", ignoreCase = true) -> AgronomistChatModule()
                                title.contains("Commodity", ignoreCase = true) || title.contains("Mandi", ignoreCase = true) || title.contains("Market", ignoreCase = true) -> MarketCommodityModule()
                                title.contains("Notification", ignoreCase = true) || title.contains("Alert", ignoreCase = true) || title.contains("Warning", ignoreCase = true) -> NotificationCenterModule()
                                title.contains("Settings", ignoreCase = true) || title.contains("Sync", ignoreCase = true) || title.contains("Appearance", ignoreCase = true) -> AppSettingsSyncModule()
                                title.contains("Privacy", ignoreCase = true) || title.contains("Deletion", ignoreCase = true) || title.contains("Export", ignoreCase = true) -> DataPrivacyDeletionModule()
                                else -> GenericFarmerModule(title)
                            }
                        }

                        // Contextual Primary Action Button
                        if (nextActivityClass != null) {
                            item {
                                Spacer(modifier = Modifier.height(2.dp))
                                Button(
                                    onClick = {
                                        activity.startActivity(Intent(activity, nextActivityClass))
                                        activity.finish()
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Text(nextButtonText, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Secondary Return Action Button (Always returns to Dashboard cleanly)
                        item {
                            OutlinedButton(
                                onClick = {
                                    activity.startActivity(
                                        Intent(activity, Screen7Activity::class.java).apply {
                                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                        }
                                    )
                                    activity.finish()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                            ) {
                                Text("Return to Dashboard", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
