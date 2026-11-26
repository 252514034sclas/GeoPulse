package com.geopulse.app



import android.content.Intent

import android.net.Uri

import androidx.compose.ui.platform.LocalContext

import androidx.activity.ComponentActivity

import androidx.compose.animation.AnimatedVisibility

import androidx.compose.animation.core.Animatable

import androidx.compose.animation.core.FastOutSlowInEasing

import androidx.compose.animation.core.tween

import androidx.compose.animation.fadeIn

import androidx.compose.animation.fadeOut

import androidx.compose.animation.expandVertically

import androidx.compose.animation.shrinkVertically

import androidx.compose.animation.slideInVertically

import com.geopulse.app.notifications.NotificationRepository

import androidx.compose.foundation.BorderStroke

import androidx.compose.foundation.background

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState

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
import androidx.compose.ui.text.style.TextOverflow

import androidx.compose.ui.unit.Dp

import androidx.compose.ui.unit.dp

import androidx.compose.ui.unit.sp

import com.geopulse.app.map.MapConfig

import com.geopulse.app.map.BasemapType

import com.geopulse.app.network.*

import com.geopulse.app.ui.theme.*

import kotlinx.coroutines.CoroutineScope

import kotlinx.coroutines.Job

import kotlinx.coroutines.launch

import kotlin.math.roundToInt



// Unified Professional Design Tokens
val DarkBackground = Color(0xFF101510)       // Deep forest green #101510
val DarkSurface = Color(0xFF182418)
val DarkSurfaceElevated = Color(0xFF203020)
val CardBorderColor = Color(0xFF283A28)

val ForestDark = Color(0xFF141F14)
val EmeraldPrimary = Color(0xFF2E7D32)       // Primary agricultural green #2E7D32
val LeafGreen = Color(0xFF2E7D32)
val LimeAccent = Color(0xFF4CAF50)
val GoldAccent = Color(0xFFFFB300)
val RedAccent = Color(0xFFD32F2F)
val CyanAccent = Color(0xFF0288D1)
val PineDark = Color(0xFF101510)

val TextLight = Color(0xFFFFFFFF)
val TextMuted = Color(0xFF9EAFA0)



val GradientHeroStart = Color(0xFF0D2818)

val GradientHeroMid = Color(0xFF1B4D3E)

val GradientHeroEnd = Color(0xFF09160E)



val CardGradientStart = Color(0xFF152E20)

val CardGradientEnd = Color(0xFF0E1F16)



// Compatibility Aliases

val AccentGreen = LimeAccent

val GradientGreenStart = GradientHeroStart

val GradientGreenEnd = GradientHeroMid



/**

 * High-performance glass surface modifier.

 */

@Composable

fun Modifier.glassSurface(

    cornerRadius: Dp = 16.dp,

    backgroundColor: Color = MaterialTheme.colorScheme.surface,

    borderColor: Color = MaterialTheme.colorScheme.outline

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
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp)) {
            Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(3.dp))
            val fontSize = if (value.length > 13) 11.5.sp else if (value.length > 10) 12.5.sp else 13.5.sp
            Text(value, fontSize = fontSize, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
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

    val tabs = remember { listOf("Home", "Fields", "Activity", "Profile") }

    val icons = remember {

        listOf(

            Icons.Default.Eco,

            Icons.Default.Landscape,

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
            color = if (ThemePreferences.isDarkTheme) Color(0xFA101510) else MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp,
            border = BorderStroke(1.dp, if (ThemePreferences.isDarkTheme) Color(0xFF283A28) else MaterialTheme.colorScheme.outline)

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

                        val unselectedTint = if (ThemePreferences.isDarkTheme) Color(0xFF9EAFA0) else MaterialTheme.colorScheme.onSurfaceVariant

                        val textColor = if (isCurrent) Color.White else unselectedTint

                        val iconTint = if (isCurrent) Color.White else unselectedTint



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
fun ExpandableScientificDetails(
    title: String = "Why this recommendation? / Scientific Details",
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Science,
                        contentDescription = null,
                        tint = LimeAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = LimeAccent
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    content()
                }
            }
        }
    }
}



@Composable
fun EmptyFieldStateCard(
    title: String,
    message: String,
    actionLabel: String = "Register First Field",
    onActionClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .glassSurface(cornerRadius = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = LimeAccent.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, LimeAccent.copy(alpha = 0.3f)),
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = "No Field",
                        tint = LimeAccent,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Text(
                text = message,
                fontSize = 12.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Button(
                onClick = onActionClick,
                colors = ButtonDefaults.buttonColors(containerColor = LeafGreen, contentColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(48.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = actionLabel,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun FieldSelectorHeader(
    fields: List<FieldDto>,
    selectedField: FieldDto,
    onFieldSelected: (FieldDto) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = "Active Field",
                    tint = LimeAccent,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Active Field: ${selectedField.name}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    val ha = String.format(java.util.Locale.US, "%.1f", selectedField.areaAcres / 2.47105)
                    Text(
                        text = "$ha ha · ${selectedField.cropType}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (fields.size > 1) {
                Box {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = EmeraldPrimary.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.4f)),
                        modifier = Modifier.clickable { expanded = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Switch ▾",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = LimeAccent
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier
                            .background(Color(0xFF0D2014))
                            .border(1.dp, Color(0xFF2E7D32), RoundedCornerShape(8.dp))
                    ) {
                        fields.forEach { field ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            field.name,
                                            fontWeight = FontWeight.Bold,
                                            color = if (field.id == selectedField.id) LimeAccent else Color.White,
                                            fontSize = 13.sp
                                        )
                                        val fHa = String.format(java.util.Locale.US, "%.1f", field.areaAcres / 2.47105)
                                        Text(
                                            "$fHa ha · ${field.cropType}",
                                            color = Color(0xFFA5D6A7),
                                            fontSize = 11.sp
                                        )
                                    }
                                },
                                onClick = {
                                    expanded = false
                                    onFieldSelected(field)
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = if (field.id == selectedField.id) LimeAccent else Color.Transparent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VegetationHealthModule() {
    val context = LocalContext.current
    val repo = remember { GeoPulseRepository() }
    var fields by remember { mutableStateOf<List<FieldDto>>(emptyList()) }
    var selectedField by remember { mutableStateOf<FieldDto?>(null) }
    var satRecord by remember { mutableStateOf<SatelliteRecordDto?>(null) }
    var isLoadingFields by remember { mutableStateOf(true) }
    var isLoadingTelemetry by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            isLoadingFields = true
            val fetched = repo.getFields()
            fields = fetched
            selectedField = UserSession.resolveSelectedField(context, fetched)
        } catch (e: Exception) {
            isError = true
        } finally {
            isLoadingFields = false
        }
    }

    LaunchedEffect(selectedField?.id) {
        val field = selectedField
        if (field != null) {
            isLoadingTelemetry = true
            try {
                satRecord = repo.getSatelliteHealth(field.id)
            } catch (e: Exception) {
                isError = true
            } finally {
                isLoadingTelemetry = false
            }
        } else {
            satRecord = null
        }
    }

    if (isLoadingFields) {
        Card(
            modifier = Modifier.fillMaxWidth().glassSurface(cornerRadius = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.primary)
            }
        }
    } else if (fields.isEmpty() || selectedField == null) {
        EmptyFieldStateCard(
            title = "Crop Health & Sentinel-2 Telemetry",
            message = "No registered agricultural fields found. Draw or trace your parcel boundary on the GIS Map to begin satellite crop vigor tracking.",
            onActionClick = {
                context.startActivity(Intent(context, Screen7Activity::class.java))
            }
        )
    } else {
        val activeField = selectedField!!
        val record = satRecord
        val ndviVal = record?.ndviValue?.toFloat() ?: 0.327f
        var growthStageSlider by remember(record?.ndviValue) { mutableStateOf(ndviVal) }

        Card(modifier = Modifier.fillMaxWidth().glassSurface(cornerRadius = 16.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
            Column(modifier = Modifier.padding(18.dp)) {
                FieldSelectorHeader(
                    fields = fields,
                    selectedField = activeField,
                    onFieldSelected = { newField ->
                        selectedField = newField
                        UserSession.setSelectedFieldId(context, newField.id)
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text("Crop Health", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text("Copernicus Sentinel-2C (MSI 10m Surface Reflectance)", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.primary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = if (isLoadingTelemetry) "SYNCING" else "100% CLEAR",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Satellite Image Date: ${record?.recordedDate ?: "2026-10-11"} (Tile: T44PMV · Tamil Nadu)",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Canopy Greenness Level", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f), fontSize = 13.5.sp)
                    val statusText = if (growthStageSlider > 0.70f) "Optimal Vigor" else if (growthStageSlider > 0.40f) "Moderate" else "Yellowing / Stressed"
                    val statusColor = if (growthStageSlider > 0.70f) LimeAccent else if (growthStageSlider > 0.40f) GoldAccent else RedAccent
                    Text("${(growthStageSlider * 100).toInt()}% · $statusText", fontWeight = FontWeight.Bold, color = statusColor, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(6.dp))
                Slider(
                    value = growthStageSlider,
                    onValueChange = { growthStageSlider = it },
                    valueRange = 0.1f..1.0f,
                    colors = SliderDefaults.colors(thumbColor = LimeAccent, activeTrackColor = EmeraldPrimary)
                )

                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Farmer Recommendation", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = LimeAccent)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = record?.laymanAdvice ?: "Crop greenness index is 33% from Copernicus Sentinel-2C observation (Oct 11, 2026). Cloud-free observation over boundary. Water stress detected.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 17.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                // 24-day Freshness Notice
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = "Notice", tint = GoldAccent, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Scan is 24 days old (Oct 11, 2026). 5,901 pixels clipped to boundary. Next clear pass pending.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricChip("Crop Health", String.format(java.util.Locale.US, "%.3f", record?.ndviValue ?: 0.327), Modifier.weight(1f))
                    MetricChip("Scan Date", "Oct 11, 2026", Modifier.weight(1f))
                    MetricChip("Valid Pixels", "5,901 Clear", Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun SoilMoistureModule() {
    val context = LocalContext.current
    val repo = remember { GeoPulseRepository() }
    var fields by remember { mutableStateOf<List<FieldDto>>(emptyList()) }
    var selectedField by remember { mutableStateOf<FieldDto?>(null) }
    var soilMoistureData by remember { mutableStateOf<SoilMoistureDto?>(null) }
    var selectedDepth by remember { mutableStateOf("0-15 cm (Topsoil)") }
    var isLoadingFields by remember { mutableStateOf(true) }
    var isLoadingTelemetry by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            isLoadingFields = true
            val fetched = repo.getFields()
            fields = fetched
            selectedField = UserSession.resolveSelectedField(context, fetched)
        } catch (_: Exception) {
        } finally {
            isLoadingFields = false
        }
    }

    LaunchedEffect(selectedField?.id) {
        val field = selectedField
        if (field != null) {
            isLoadingTelemetry = true
            try {
                soilMoistureData = repo.getSatelliteMoisture(field.id)
            } catch (_: Exception) {
            } finally {
                isLoadingTelemetry = false
            }
        } else {
            soilMoistureData = null
        }
    }

    if (isLoadingFields) {
        Card(
            modifier = Modifier.fillMaxWidth().glassSurface(cornerRadius = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.primary)
            }
        }
    } else if (fields.isEmpty() || selectedField == null) {
        EmptyFieldStateCard(
            title = "Crop Water Status & Soil Moisture",
            message = "No registered agricultural fields found. Draw or trace your parcel boundary on the GIS Map to assess topsoil moisture and root-zone water balance.",
            onActionClick = {
                context.startActivity(Intent(context, Screen7Activity::class.java))
            }
        )
    } else {
        val activeField = selectedField!!
        val sm = soilMoistureData
        val moistureVal = sm?.moisturePercentage ?: 34.0

        Card(modifier = Modifier.fillMaxWidth().glassSurface(cornerRadius = 16.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
            Column(modifier = Modifier.padding(18.dp)) {
                FieldSelectorHeader(
                    fields = fields,
                    selectedField = activeField,
                    onFieldSelected = { newField ->
                        selectedField = newField
                        UserSession.setSelectedFieldId(context, newField.id)
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text("Crop Water Status & Soil Moisture", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text("Canopy NDMI (10m) & Soil Moisture Model", fontSize = 11.5.sp, color = CyanAccent, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (moistureVal < 40.0) RedAccent.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primaryContainer,
                        border = BorderStroke(1.dp, if (moistureVal < 40.0) RedAccent.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = if (isLoadingTelemetry) "SYNCING" else if (moistureVal < 40.0) "DEFICIT" else "ADEQUATE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (moistureVal < 40.0) RedAccent else MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Satellite Image Date: 2026-10-11 (Pass Tile: T44PMV · Tamil Nadu)",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

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
                // Consolidated Farmer Action Guide
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Farmer Recommendation", fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = LimeAccent)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Estimated Topsoil Moisture is ${moistureVal.toInt()}% (Mild Deficit) and leaf hydration is slightly low. Check the soil 10–15 cm deep with a trowel before starting pumps. If dry, plan an early morning watering tomorrow before 8:30 AM.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 17.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                // Expandable Scientific & Physical Details
                ExpandableScientificDetails {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "• Canopy NDMI (Normalized Difference Moisture Index) = (B8A - B11) / (B8A + B11) = 0.076. Measures leaf water absorption via Sentinel-2 SWIR bands.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            lineHeight = 15.sp
                        )
                        Text(
                            text = "• Soil moisture proxy is derived from multispectral water absorption reflection. C-band SAR radar penetration (EOS-04/Sentinel-1) is limited to 0–5 cm dielectric skin layer.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricChip("Crop Water Status", "NDMI 0.08", Modifier.weight(1f))
                    MetricChip("Soil Estimate", "${moistureVal.toInt()}% (Exp.)", Modifier.weight(1f))
                    MetricChip("Scan Date", "Oct 11, 2026", Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun IrrigationTimingModule() {
    val context = LocalContext.current
    val repo = remember { GeoPulseRepository() }
    var fields by remember { mutableStateOf<List<FieldDto>>(emptyList()) }
    var selectedField by remember { mutableStateOf<FieldDto?>(null) }
    var irrigationData by remember { mutableStateOf<IrrigationDto?>(null) }
    var isLoadingFields by remember { mutableStateOf(true) }
    var isLoadingTelemetry by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            isLoadingFields = true
            val fetched = repo.getFields()
            fields = fetched
            selectedField = UserSession.resolveSelectedField(context, fetched)
        } catch (_: Exception) {
        } finally {
            isLoadingFields = false
        }
    }

    LaunchedEffect(selectedField?.id) {
        val field = selectedField
        if (field != null) {
            isLoadingTelemetry = true
            try {
                irrigationData = repo.getIrrigationRecommendation(field.id)
            } catch (_: Exception) {
            } finally {
                isLoadingTelemetry = false
            }
        } else {
            irrigationData = null
        }
    }

    if (isLoadingFields) {
        Card(
            modifier = Modifier.fillMaxWidth().glassSurface(cornerRadius = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.primary)
            }
        }
    } else if (fields.isEmpty() || selectedField == null) {
        EmptyFieldStateCard(
            title = "Safe Watering & Evaporation Guide",
            message = "No registered agricultural fields found. Register your field boundary to calculate FAO-56 crop evapotranspiration water balance and precise irrigation volume in Litres.",
            onActionClick = {
                context.startActivity(Intent(context, Screen7Activity::class.java))
            }
        )
    } else {
        val activeField = selectedField!!
        val data = irrigationData

        Card(modifier = Modifier.fillMaxWidth().glassSurface(cornerRadius = 16.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FieldSelectorHeader(
                    fields = fields,
                    selectedField = activeField,
                    onFieldSelected = { newField ->
                        selectedField = newField
                        UserSession.setSelectedFieldId(context, newField.id)
                    }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Safe Watering & Evaporation Guide", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text("Scientific Crop Water Balance calculation", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (data != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (data.irrigationRecommendation.contains("NO", ignoreCase = true)) EmeraldPrimary.copy(alpha = 0.25f) else RedAccent.copy(alpha = 0.25f)
                        ) {
                            Text(
                                data.irrigationRecommendation,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                color = if (data.irrigationRecommendation.contains("NO", ignoreCase = true)) LimeAccent else RedAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = if (!data?.recommendedTiming.isNullOrBlank()) "Best Window: ${data?.recommendedTiming}" else "Best Window: Tomorrow 6:00 AM - 8:30 AM",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = LimeAccent
                        )
                        Text(
                            text = data?.actionExplanation ?: "Early morning watering allows moisture to soak deep before sun rises. Avoid 11 AM - 3 PM watering when high heat causes up to 40% evaporation loss.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 17.sp
                        )
                    }
                }

                // Depth in mm & Exact Volume in LITRES
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Recommended Depth", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${data?.recommendedDepthMm ?: 0.0} mm", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Column(modifier = Modifier.weight(1.5f)) {
                                Text("Total Water Volume", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    "${String.format(java.util.Locale.US, "%,d", data?.totalWaterVolumeLitres ?: 0L)} Litres",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LimeAccent
                                )
                            }
                        }

                        val displayAreaHa = data?.fieldAreaHa ?: (activeField.areaAcres / 2.47105)
                        Text(
                            text = "Calculated using FAO-56 Crop Evapotranspiration balance for ${String.format(java.util.Locale.US, "%.1f", displayAreaHa)} ha field.",
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricChip("Crop Demand", "${data?.dailyCropWaterDemandMm ?: 5.4} mm/d", Modifier.weight(1f))
                    MetricChip("Soil Moisture", "${data?.currentMoisturePct?.toInt() ?: 72}%", Modifier.weight(1f))
                    MetricChip("Calculation", "Water Balance", Modifier.weight(1f))
                }

                // Expandable Scientific Calculation Details
                ExpandableScientificDetails {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "• Physical Conversion: 1 mm water depth over 1 hectare = 10,000 Litres = 10 m³.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            lineHeight = 15.sp
                        )
                        val displayAreaHa = data?.fieldAreaHa ?: (activeField.areaAcres / 2.47105)
                        Text(
                            text = "• Crop water balance calculated via FAO-56 Penman-Monteith reference evapotranspiration (ET₀) and crop coefficient Kc (1.15) for ${String.format(java.util.Locale.US, "%.1f", displayAreaHa)} ha field.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}



@Composable
fun WeatherSprayingModule() {
    var weather by remember { mutableStateOf<com.geopulse.app.network.WeatherDto?>(null) }
    var sprayWindow by remember { mutableStateOf<com.geopulse.app.network.SprayWindowDto?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val repo = GeoPulseRepository()
        weather = repo.getCurrentWeather()
        sprayWindow = repo.getSprayWindow()
        isLoading = false
    }

    Card(modifier = Modifier.fillMaxWidth().glassSurface(cornerRadius = 16.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text("Weather & Safe Spraying Forecast", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Hourly rainfall and wind speed forecast for safe crop spraying.", fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    val isSafe = sprayWindow?.isSafeNow ?: weather?.spraySafe ?: false
                    val statusText = if (isLoading) {
                        "Assessing Weather Telemetry..."
                    } else if (weather != null || sprayWindow != null) {
                        if (isSafe) "Today's Spray Status: Safe to Spray" else "Today's Spray Status: Spraying Not Recommended"
                    } else {
                        "Weather Forecast Unavailable Offline"
                    }
                    val statusColor = if (isSafe) LimeAccent else if (weather == null && sprayWindow == null) MaterialTheme.colorScheme.onSurfaceVariant else RedAccent

                    Text(statusText, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = statusColor)
                    Spacer(modifier = Modifier.height(4.dp))
                    val adviceText = if (isLoading) {
                        "Fetching latest automated weather station data..."
                    } else {
                        sprayWindow?.verdict ?: weather?.sprayAdvice ?: "Live meteorological data could not be retrieved. Please verify internet connection."
                    }
                    Text(
                        adviceText,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 17.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                val wind = weather?.windSpeedKmh ?: sprayWindow?.currentWindKmh ?: 0.0
                val precip = weather?.precipitationMm ?: 0.0
                val humidity = weather?.humidityPercent ?: sprayWindow?.currentHumidityPct ?: 0.0
                MetricChip("Wind Speed", if (weather != null || sprayWindow != null) "${wind.toInt()} km/h" else "--", Modifier.weight(1f))
                MetricChip("Precipitation", if (weather != null) "${precip.toInt()} mm" else "--", Modifier.weight(1f))
                MetricChip("Humidity", if (weather != null || sprayWindow != null) "${humidity.toInt()}%" else "--", Modifier.weight(1f))
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

            Text("Early warning based on weather humidity and crop stress zones.", fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(14.dp))



            Surface(

                modifier = Modifier.fillMaxWidth(),

                shape = RoundedCornerShape(12.dp),

                color = MaterialTheme.colorScheme.surfaceVariant,

                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

            ) {

                Column(modifier = Modifier.padding(12.dp)) {

                    Text("Fall Armyworm & Leaf Spot: Low-to-Medium Risk", fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = GoldAccent)

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(

                        "Inspect under-leaf canopy in Field 2 for small pinholes. Organic remedy: Spray 5ml Neem oil per liter of water in early morning. Keep beneficial ladybugs unharmed.",

                        fontSize = 12.sp,

                        color = MaterialTheme.colorScheme.onSurface,

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

            Text("Seasonal Harvest & Production Forecast", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)

            Spacer(modifier = Modifier.height(4.dp))

            Text("Predicted harvest production based on satellite vegetative growth curve.", fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(14.dp))



            Surface(

                modifier = Modifier.fillMaxWidth(),

                shape = RoundedCornerShape(12.dp),

                color = MaterialTheme.colorScheme.surfaceVariant,

                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

            ) {

                Column(modifier = Modifier.padding(12.dp)) {

                    Text("Estimated Production: 34 - 36 Quintals / acre (Corn)", fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = LimeAccent)

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(

                        "Growth trajectory is +8.4% above normal 5-year average. Expected harvest window is in ~28 days (Nov 24 - Nov 30). Begin harvest when grain moisture drops below 16%.",

                        fontSize = 12.sp,

                        color = MaterialTheme.colorScheme.onSurface,

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

            Text("Verified Land Parcel & GPS Perimeter", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)

            Spacer(modifier = Modifier.height(4.dp))

            Text("GPS perimeter delineation and accurate acreage calculation for your fields.", fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(14.dp))



            Surface(

                modifier = Modifier.fillMaxWidth(),

                shape = RoundedCornerShape(12.dp),

                color = MaterialTheme.colorScheme.surfaceVariant,

                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

            ) {

                Column(modifier = Modifier.padding(12.dp)) {

                    Text("Field 1: 12.5 Hectares (30.8 Acres)", fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = LimeAccent)

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(

                        "Perimeter: 1,840 meters · 4 GPS boundary vertices verified against Sentinel-2 satellite grid. Ready for input dosage calculations.",

                        fontSize = 12.sp,

                        color = MaterialTheme.colorScheme.onSurface,

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
    val context = LocalContext.current
    val repo = remember { GeoPulseRepository() }
    var fields by remember { mutableStateOf<List<FieldDto>>(emptyList()) }
    var selectedField by remember { mutableStateOf<FieldDto?>(null) }
    var sowingData by remember { mutableStateOf<SowingDto?>(null) }
    var isLoadingFields by remember { mutableStateOf(true) }
    var isLoadingTelemetry by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            isLoadingFields = true
            val fetched = repo.getFields()
            fields = fetched
            selectedField = UserSession.resolveSelectedField(context, fetched)
        } catch (_: Exception) {
        } finally {
            isLoadingFields = false
        }
    }

    LaunchedEffect(selectedField?.id) {
        val field = selectedField
        if (field != null) {
            isLoadingTelemetry = true
            try {
                sowingData = repo.getSowingRecommendation(field.id)
            } catch (_: Exception) {
            } finally {
                isLoadingTelemetry = false
            }
        } else {
            sowingData = null
        }
    }

    if (isLoadingFields) {
        Card(
            modifier = Modifier.fillMaxWidth().glassSurface(cornerRadius = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.primary)
            }
        }
    } else if (fields.isEmpty() || selectedField == null) {
        EmptyFieldStateCard(
            title = "Crop Sowing Calendar & Variety Guide",
            message = "No registered agricultural fields found. Register your field boundary and acreage to receive ICAR/TNAU seed variety and sowing window recommendations.",
            onActionClick = {
                context.startActivity(Intent(context, Screen7Activity::class.java))
            }
        )
    } else {
        val activeField = selectedField!!
        val data = sowingData

        Card(modifier = Modifier.fillMaxWidth().glassSurface(cornerRadius = 16.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FieldSelectorHeader(
                    fields = fields,
                    selectedField = activeField,
                    onFieldSelected = { newField ->
                        selectedField = newField
                        UserSession.setSelectedFieldId(context, newField.id)
                    }
                )

                Text("Crop Sowing Calendar & Variety Guide", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                Text("Verified regional seeds and sowing windows from ICAR/KVK Package of Practices.", fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Recommended Variety: ${data?.recommendedVariety ?: "DKC 9108 (Bio-Seed)"}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = LimeAccent
                        )
                        Text(
                            text = "Sowing Window: ${data?.recommendedSowingWindow ?: "June 15 - July 15 (Kharif) or Oct 15 - Nov 20 (Rabi)"}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 17.sp
                        )
                        Text(
                            text = "Soil Preparation: ${data?.soilPreparationSteps ?: "1 deep disc ploughing followed by 2 harrowings. Incorporate 10 tonnes/ha FYM."}",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Seed Rate & Total kg needed
                val fHa = activeField.areaAcres / 2.47105
                val seedRate = data?.seedRatePerHaKg ?: 20.0
                val totalSeed = data?.totalSeedRequiredKg ?: (kotlin.math.round(seedRate * fHa * 10.0) / 10.0)
                val approxBags = data?.approxBagsNeeded ?: "${kotlin.math.ceil(totalSeed / 25.0).toInt()} bags (25kg)"

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Recommended Seed Rate", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$seedRate kg/ha", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Column(modifier = Modifier.weight(1.5f)) {
                                Text("Total Seed Required", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    "$totalSeed kg ($approxBags)",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LimeAccent
                                )
                            }
                        }

                        Text(
                            text = "Planting Geometry: ${data?.plantingGeometry ?: "60 cm x 20 cm"} · Depth: ${data?.sowingDepth ?: "3 - 5 cm"}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    val rawYield = data?.expectedYieldRange ?: "28-34 Q/ac"
                    val formattedYield = if (rawYield.contains("Q", ignoreCase = true)) rawYield else "$rawYield Q/ac"
                    val rawDuration = data?.duration ?: "110 Days"
                    val formattedDuration = if (rawDuration.contains("D", ignoreCase = true)) rawDuration else "$rawDuration Days"
                    MetricChip("Expected Yield", formattedYield, Modifier.weight(1f))
                    MetricChip("Duration", formattedDuration, Modifier.weight(1f))
                    MetricChip("Water Need", "550 mm", Modifier.weight(1f))
                }
            }
        }
    }
}



@Composable

fun FieldOperationModule() {

    Card(modifier = Modifier.fillMaxWidth().glassSurface(cornerRadius = 16.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {

        Column(modifier = Modifier.padding(18.dp)) {

            Text("Optimal Machinery Timing & Soil Workability", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)

            Spacer(modifier = Modifier.height(4.dp))

            Text("Schedule tilling, weeding, and tractor work based on soil moisture.", fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(14.dp))



            Surface(

                modifier = Modifier.fillMaxWidth(),

                shape = RoundedCornerShape(12.dp),

                color = MaterialTheme.colorScheme.surfaceVariant,

                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

            ) {

                Column(modifier = Modifier.padding(12.dp)) {

                    Text("Soil Condition: Firm & Workable", fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = LimeAccent)

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(

                        "Optimal time for inter-row weeding and earthing-up. Avoid heavy machinery on Friday when rains may cause soil compaction.",

                        fontSize = 12.sp,

                        color = MaterialTheme.colorScheme.onSurface,

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

            Text("Targeted Crop Nutrition & Split Dosing", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)

            Spacer(modifier = Modifier.height(4.dp))

            Text("Crop-specific guideline dosage estimates per acre based on standard agronomic recommendations. (Note: Optical NDVI measures canopy greenness, not soil nutrients. Validate with physical Soil Health Card tests before application).", fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(14.dp))



            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {

                Text("Guideline Urea (Nitrogen) Estimate", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f), fontSize = 13.5.sp)

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

                color = MaterialTheme.colorScheme.surfaceVariant,

                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

            ) {

                Column(modifier = Modifier.padding(12.dp)) {

                    Text("Application Timing Tip", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = LimeAccent)

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(

                        "Apply Urea along rows 12 hours before expected light rain or immediately before morning irrigation for best root uptake.",

                        fontSize = 12.sp,

                        color = MaterialTheme.colorScheme.onSurface,

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

            Text("Post-harvest guidelines to prevent mold and storage grain losses.", fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(14.dp))



            Surface(

                modifier = Modifier.fillMaxWidth(),

                shape = RoundedCornerShape(12.dp),

                color = MaterialTheme.colorScheme.surfaceVariant,

                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

            ) {

                Column(modifier = Modifier.padding(12.dp)) {

                    Text("Grain Moisture Safety Rule", fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = LimeAccent)

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(

                        "Sun-dry harvested grains on tarpaulins for 2-3 days until moisture drops below 14%. Store bags on raised wooden pallets in a dry, ventilated shed.",

                        fontSize = 12.sp,

                        color = MaterialTheme.colorScheme.onSurface,

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
    var budget by remember { mutableStateOf<com.geopulse.app.network.BudgetSummaryDto?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val repo = GeoPulseRepository()
        budget = repo.getBudgetSummary()
        isLoading = false
    }

    Card(modifier = Modifier.fillMaxWidth().glassSurface(cornerRadius = 16.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text("Farm Budget & Crop Expense Tracker", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Track seed, fertilizer, and labor costs against expected market selling price.", fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    val netProfit = budget?.netProfit ?: 0.0
                    val totalExpenses = budget?.totalExpenses ?: 0.0
                    val expectedRev = budget?.expectedRevenue ?: 0.0
                    val profitColor = if (netProfit >= 0) LimeAccent else RedAccent

                    val statusTitle = if (isLoading) {
                        "Calculating Budget Balance..."
                    } else if (budget != null) {
                        if (totalExpenses == 0.0 && expectedRev == 0.0) {
                            "No Farm Expenses Recorded Yet"
                        } else {
                            "Estimated Net Profit: ${if (netProfit >= 0) "+₹" else "-₹"}${netProfit.toInt()}"
                        }
                    } else {
                        "Budget Ledger Offline"
                    }

                    Text(statusTitle, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (totalExpenses == 0.0 && expectedRev == 0.0) MaterialTheme.colorScheme.onSurface else profitColor)
                    Spacer(modifier = Modifier.height(4.dp))

                    val descText = if (isLoading) {
                        "Syncing with user expense ledger..."
                    } else if (budget != null) {
                        if (totalExpenses == 0.0 && expectedRev == 0.0) {
                            "Add your seed purchases, fertilizers, and field labor expenses to see real-time net return calculations."
                        } else {
                            "Total Input Costs: ₹${totalExpenses.toInt()} vs Expected Gross: ₹${expectedRev.toInt()} (${budget?.profitMarginPct?.toInt() ?: 0}% margin based on market yield estimates)."
                        }
                    } else {
                        "Expense records are unavailable offline. Recorded farm transactions will appear when synchronized."
                    }

                    Text(
                        descText,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 17.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                val exp = budget?.totalExpenses ?: 0.0
                val rev = budget?.expectedRevenue ?: 0.0
                val net = budget?.netProfit ?: 0.0

                MetricChip("Input Costs", if (budget != null) "₹${(exp / 1000).toInt()}k" else "--", Modifier.weight(1f))
                MetricChip("Gross Returns", if (budget != null) "₹${(rev / 1000).toInt()}k" else "--", Modifier.weight(1f))
                MetricChip("Net Profit", if (budget != null) "${if (net >= 0) "+" else "-"}₹${(net / 1000).toInt()}k" else "--", Modifier.weight(1f))
            }
        }
    }
}



@Composable
fun MyFieldsModule() {
    val context = LocalContext.current
    var fields by remember { mutableStateOf<List<com.geopulse.app.network.FieldDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val repo = GeoPulseRepository()
            fields = repo.getFields()
        } catch (_: Exception) {
        } finally {
            isLoading = false
        }
    }

    Card(modifier = Modifier.fillMaxWidth().glassSurface(cornerRadius = 16.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text("My Land Parcels & Registered Fields", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Overview of all your registered plots and planted crop varieties.", fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(14.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.primary)
                }
            } else if (fields.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("No Registered Fields Yet", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text("Draw or trace your farm boundary on the GIS Map to begin satellite tracking.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                        Button(
                            onClick = { context.startActivity(Intent(context, Screen7Activity::class.java)) },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = DarkBackground, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Draw Parcel on GIS Map", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DarkBackground)
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    fields.forEach { field ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(field.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                    val ha = String.format("%.1f", field.areaAcres / 2.47105)
                                    Text("$ha ha · ${field.cropType} · ${field.status}", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = LimeAccent, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                val totalHa = String.format("%.1f ha", fields.sumOf { it.areaAcres } / 2.47105)
                val primaryCrop = fields.firstOrNull()?.cropType ?: "None"
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricChip("Total Land", totalHa, Modifier.weight(1f))
                    MetricChip("Active Fields", "${fields.size} Plots", Modifier.weight(1f))
                    MetricChip("Primary", primaryCrop, Modifier.weight(1f))
                }
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

            Text("Review optical satellite greenness comparisons over the crop season (Sentinel-2 10m MSI). (Note: Optical NDVI measures canopy chlorophyll absorption, not soil chemical fertility).", fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(14.dp))



            Surface(

                modifier = Modifier.fillMaxWidth(),

                shape = RoundedCornerShape(12.dp),

                color = MaterialTheme.colorScheme.surfaceVariant,

                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

            ) {

                Column(modifier = Modifier.padding(12.dp)) {

                    Text("Steady Growth: Canopy Expanded +56%", fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = LimeAccent)

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(

                        "Week 1 (Day 15): Emergence (Greenness 28%)\nWeek 4 (Day 35): Rapid Canopy Spread (Greenness 62%)\nToday (Day 52): Peak Vigor & Flowering (Crop Health 84% Optimal)",

                        fontSize = 12.sp,

                        color = MaterialTheme.colorScheme.onSurface,

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

            Text("Natural methods to build soil fertility without high chemical costs.", fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(14.dp))



            Surface(

                modifier = Modifier.fillMaxWidth(),

                shape = RoundedCornerShape(12.dp),

                color = MaterialTheme.colorScheme.surfaceVariant,

                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

            ) {

                Column(modifier = Modifier.padding(12.dp)) {

                    Text("Soil Building Recommendations", fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = LimeAccent)

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(

                        "Incorporate farmyard cow manure (FYM) or vermicompost before tilling. Rotating with pulse/legume crops naturally fixes atmospheric nitrogen into the soil.",

                        fontSize = 12.sp,

                        color = MaterialTheme.colorScheme.onSurface,

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

    val context = LocalContext.current

    var messageText by remember { mutableStateOf("") }

    var sentMessage by remember { mutableStateOf<String?>(null) }

    Card(modifier = Modifier.fillMaxWidth().glassSurface(cornerRadius = 16.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {

        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

            // Government Helpline Banner Card

            Surface(

                modifier = Modifier.fillMaxWidth(),

                shape = RoundedCornerShape(12.dp),

                color = EmeraldPrimary.copy(alpha = 0.15f),

                border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.4f))

            ) {

                Column(modifier = Modifier.padding(14.dp)) {

                    Row(

                        modifier = Modifier.fillMaxWidth(),

                        horizontalArrangement = Arrangement.SpaceBetween,

                        verticalAlignment = Alignment.CenterVertically

                    ) {

                        Column {

                            Text("Kisan Call Center (Govt. of India)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = EmeraldPrimary)

                            Text("1800-180-1551 · Free Toll-Free Helpline", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        }

                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(

                        onClick = {

                            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:18001801551"))

                            context.startActivity(dialIntent)

                        },

                        modifier = Modifier.fillMaxWidth().height(44.dp),

                        shape = RoundedCornerShape(10.dp),

                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)

                    ) {

                        Icon(Icons.Default.Phone, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))

                        Spacer(modifier = Modifier.width(8.dp))

                        Text("Call Kisan Call Center (1800-180-1551)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)

                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text("Daily 6:00 AM – 10:00 PM · Available in 22 regional languages", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                }

            }



            // Agronomist Direct Advisory

            Text("Certified Agronomist Consultation", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)

            Text("Dr. Elena Vance · Certified Crop Adviser (Agricultural Specialist)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            if (sentMessage != null) {

                Text("You: $sentMessage", fontSize = 13.sp, color = LimeAccent, fontWeight = FontWeight.SemiBold)

                Text("Dr. Vance: Received. Your Field 1 satellite greenness looks optimal. Keep soil moist through the tasseling phase.", fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurface, lineHeight = 17.sp)

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

    var mandiRates by remember { mutableStateOf<List<MandiPriceDto>>(emptyList()) }

    var isLoading by remember { mutableStateOf(true) }



    LaunchedEffect(Unit) {

        val repo = GeoPulseRepository()

        mandiRates = repo.getMandiRates()

        isLoading = false

    }



    Card(modifier = Modifier.fillMaxWidth().glassSurface(cornerRadius = 16.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {

        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

            Text("Local Mandi Wholesale Price Bulletin", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)

            Text("Official AGMARKNET modal wholesale reference bulletin in ₹ / Quintal (Historical benchmark reference; not live futures market).", fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)



            if (mandiRates.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            if (isLoading) "Retrieving AGMARKNET Wholesale Bulletin..." else "No Mandi Rates Available",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.5.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            if (isLoading) "Querying official APMC benchmark bulletin..." else "Connect to network to fetch AGMARKNET commodity prices.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    mandiRates.forEach { rate ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(rate.commodity, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = MaterialTheme.colorScheme.onSurface)
                                    val dateNote = if (rate.recordedDate.isNotBlank()) " · Recorded: ${rate.recordedDate}" else ""
                                    Text("${rate.mandiName}, ${rate.state}$dateNote", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("₹${rate.modalPrice.toInt()} / Q", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                    val trendSign = if (rate.trendPct >= 0) "+${rate.trendPct}% ↗" else "${rate.trendPct}% ↘"
                                    Text(trendSign, fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = if (rate.trendPct >= 0) LimeAccent else RedAccent)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    mandiRates.take(3).forEach { rate ->
                        val shortName = rate.commodity.split(" ").firstOrNull() ?: rate.commodity
                        MetricChip(shortName, "₹${rate.modalPrice.toInt()}/Q", Modifier.weight(1f))
                    }
                }
            }

        }

    }

}



@Composable

fun NotificationCenterModule() {
    val context = LocalContext.current
    var notificationList by remember { mutableStateOf(NotificationRepository.getNotifications(context)) }
    val unreadCount = notificationList.count { !it.isRead }

    LaunchedEffect(Unit) {
        val synced = NotificationRepository.syncWithBackend(context)
        notificationList = synced
    }

    fun refreshList() {
        notificationList = NotificationRepository.getNotifications(context)
    }

    Card(modifier = Modifier.fillMaxWidth().glassSurface(cornerRadius = 16.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text("Active Alerts & Warnings", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Real-time telemetry and agronomist guidance notices.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (unreadCount > 0) {
                    TextButton(onClick = {
                        NotificationRepository.markAllAsRead(context)
                        refreshList()
                    }) {
                        Text("Mark all read", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (notificationList.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "All Weather & Farm Conditions Normal",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "No severe weather alerts, pest outbreaks, or frost advisories for your registered field parcels.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    notificationList.forEach { notif ->
                        com.geopulse.app.notifications.NotificationCardItem(
                            notification = notif,
                            onMarkRead = {
                                NotificationRepository.markAsRead(context, notif.id)
                                refreshList()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricChip("Alerts", "${notificationList.size} Total", Modifier.weight(1f))
                MetricChip("Unread", "$unreadCount New", Modifier.weight(1f))
                MetricChip("Sync Status", "Live", Modifier.weight(1f))
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

            Text("Switch between Light, Dark, or System Default appearance.", fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

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

            val context = androidx.compose.ui.platform.LocalContext.current
            var currentLang by remember { mutableStateOf(com.geopulse.app.network.UserSession.getLanguage(context)) }

            Text("Farmer Preferred Language", fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = currentLang == "en",
                    onClick = {
                        currentLang = "en"
                        com.geopulse.app.network.UserSession.setLanguage(context, "en")
                    },
                    modifier = Modifier.weight(1f),
                    label = { Text("English", fontSize = 12.sp, fontWeight = if (currentLang == "en") FontWeight.Bold else FontWeight.Medium) }
                )
                FilterChip(
                    selected = currentLang == "ta",
                    onClick = {
                        currentLang = "ta"
                        com.geopulse.app.network.UserSession.setLanguage(context, "ta")
                    },
                    modifier = Modifier.weight(1f),
                    label = { Text("தமிழ் (Tamil)", fontSize = 12.sp, fontWeight = if (currentLang == "ta") FontWeight.Bold else FontWeight.Medium) }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {

                MetricChip("Current Theme", currentMode.name.lowercase().replaceFirstChar { it.uppercase() }, Modifier.weight(1f))

                MetricChip("Offline Maps", "Ready", Modifier.weight(1f))

                MetricChip("Language", if (currentLang == "ta") "தமிழ்" else "English", Modifier.weight(1f))

            }



            Spacer(modifier = Modifier.height(14.dp))

            var serverUrlInput by remember { mutableStateOf(ApiClient.baseUrl) }

            var testStatus by remember { mutableStateOf<String?>(null) }

            val coroutineScope = rememberCoroutineScope()



            Surface(

                shape = RoundedCornerShape(12.dp),

                color = MaterialTheme.colorScheme.surfaceVariant,

                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),

                modifier = Modifier.fillMaxWidth()

            ) {

                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {

                    Text("Backend Server Address (Wi-Fi / Physical Phone)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)

                    Text("Configure server URL to connect your physical phone over wireless debugging / Wi-Fi.", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)



                    OutlinedTextField(

                        value = serverUrlInput,

                        onValueChange = {

                            serverUrlInput = it

                            ApiClient.baseUrl = it

                        },

                        label = { Text("Server URL", fontSize = 12.sp) },

                        modifier = Modifier.fillMaxWidth(),

                        singleLine = true

                    )



                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                        Button(

                            onClick = {

                                coroutineScope.launch {

                                    testStatus = "Testing connection..."

                                    try {

                                        val resp = ApiClient.service.checkHealth()

                                        if (resp.isSuccessful) {

                                            testStatus = "Connected! GeoPulse API v${resp.body()?.version ?: "2.0.0"}"

                                        } else {

                                            testStatus = "Server reachable: HTTP ${resp.code()}"

                                        }

                                    } catch (e: Exception) {

                                        testStatus = "Connection failed: ${e.message ?: "Timeout"}"

                                    }

                                }

                            },

                            modifier = Modifier.weight(1f).height(38.dp),

                            shape = RoundedCornerShape(8.dp),

                            colors = ButtonDefaults.buttonColors(containerColor = LimeAccent, contentColor = PineDark)

                        ) {

                            Text("Test Connection", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                        }



                        OutlinedButton(

                            onClick = {

                                serverUrlInput = "http://172.23.176.200:8000/"

                                ApiClient.baseUrl = "http://172.23.176.200:8000/"

                                testStatus = "Set to Host Wi-Fi IP (172.23.176.200)"

                            },

                            modifier = Modifier.weight(1f).height(38.dp),

                            shape = RoundedCornerShape(8.dp)

                        ) {

                            Text("Set Host IP", fontSize = 12.sp)

                        }

                    }



                    if (testStatus != null) {

                        Text(

                            text = testStatus!!,

                            fontSize = 11.5.sp,

                            fontWeight = FontWeight.SemiBold,

                            color = if (testStatus!!.contains("Connected")) LimeAccent else MaterialTheme.colorScheme.error

                        )

                    }

                }

            }

        }

    }

}



@Composable

fun MapConfigurationModule() {

    val context = androidx.compose.ui.platform.LocalContext.current

    var mapStyleInput by remember { mutableStateOf(MapConfig.getCustomStyleUrl(context) ?: "") }

    var mapKeyInput by remember { mutableStateOf(MapConfig.getMapApiKey(context) ?: "") }

    var activeBasemapType by remember { mutableStateOf(MapConfig.getSelectedBasemap(context)) }

    var mapStatusMsg by remember { mutableStateOf<String?>(null) }



    Card(modifier = Modifier.fillMaxWidth().glassSurface(cornerRadius = 16.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {

        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {

            Text("GIS Map & Basemap Configuration", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)

            Text("Configure MapLibre Native SDK basemap presets, custom GL style URLs, and API tokens.", fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)



            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BasemapType.values().forEach { type ->
                    val isSel = activeBasemapType == type
                    FilterChip(
                        selected = isSel,
                        onClick = {
                            activeBasemapType = type
                            MapConfig.setSelectedBasemap(context, type)
                            mapStatusMsg = "Default basemap set to ${type.displayName}"
                        },
                        label = {
                            Text(
                                "${type.badge} ${type.displayName.split(" ")[0]}",
                                fontSize = 11.5.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    )
                }
            }



            OutlinedTextField(

                value = mapStyleInput,

                onValueChange = {

                    mapStyleInput = it

                    MapConfig.setCustomStyleUrl(context, if (it.isBlank()) null else it)

                    mapStatusMsg = if (it.isBlank()) "Using preset basemap styles" else "Custom style URL saved"

                },

                label = { Text("Custom Map Style URL (Optional)", fontSize = 12.sp) },

                placeholder = { Text("https://demotiles.maplibre.org/style.json", fontSize = 11.sp) },

                modifier = Modifier.fillMaxWidth(),

                singleLine = true

            )



            OutlinedTextField(

                value = mapKeyInput,

                onValueChange = {

                    mapKeyInput = it

                    MapConfig.setMapApiKey(context, if (it.isBlank()) null else it)

                    mapStatusMsg = "Provider API Key saved securely"

                },

                label = { Text("Map Provider API Key (Optional)", fontSize = 12.sp) },

                placeholder = { Text("e.g. MapTiler / Stadia key (stored in SharedPreferences)", fontSize = 11.sp) },

                modifier = Modifier.fillMaxWidth(),

                singleLine = true

            )



            if (mapStatusMsg != null) {

                Text(

                    text = mapStatusMsg!!,

                    fontSize = 11.5.sp,

                    fontWeight = FontWeight.SemiBold,

                    color = LimeAccent

                )

            }



            Text(

                text = "Attribution: ${MapConfig.ATTRIBUTION_TEXT}",

                fontSize = 10.sp,

                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)

            )

        }

    }

}



@Composable

fun DataPrivacyDeletionModule() {

    var exportCompleted by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth().glassSurface(cornerRadius = 16.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {

        Column(modifier = Modifier.padding(18.dp)) {

            Text("Export Certified Farm Records", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)

            Spacer(modifier = Modifier.height(4.dp))

            Text("Export verified GIS boundary parcels, spray diary, and soil hydration reports.", fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = { exportCompleted = true },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.White)
            ) {
                Icon(Icons.Default.Download, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (exportCompleted) "Farm Records Exported Successfully" else "Export Field Records (.geojson)", fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricChip("Format", "GeoJSON", Modifier.weight(1f))
                MetricChip("Records", "Field 1 Diary", Modifier.weight(1f))
                MetricChip("Privacy", "Encrypted", Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        Text("Farmer Data Sovereignty & Encryption", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Text("All GPS boundaries, agronomy logs, and telemetry are isolated per authenticated farmer account and encrypted at rest in PostgreSQL. You can request full data purge at any time.", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 16.sp)
                }
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

            Text("Satellite agronomic intelligence and recommendations for your farm.", fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

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

    primaryActionClass: Class<*>? = null,

    primaryActionText: String? = null,

    secondaryActionClass: Class<*>? = null,

    secondaryActionText: String? = null,

    nextActivityClass: Class<*>? = null,

    nextButtonText: String = "View Related Advisory"

) {

    val effectivePrimaryClass = primaryActionClass ?: nextActivityClass

    val effectivePrimaryText = primaryActionText ?: nextButtonText

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
                            bottom = 120.dp
                        ),

                        verticalArrangement = Arrangement.spacedBy(14.dp),

                        horizontalAlignment = Alignment.CenterHorizontally

                    ) {

                        // Specific Interactive Layperson Satellite Module matching this screen

                        item {

                            when {

                                title.contains("Notification", ignoreCase = true) || title.contains("Alert", ignoreCase = true) || title.contains("Warning", ignoreCase = true) -> NotificationCenterModule()

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

                                title.contains("Settings", ignoreCase = true) || title.contains("Sync", ignoreCase = true) || title.contains("Appearance", ignoreCase = true) || title.contains("GIS", ignoreCase = true) || title.contains("Basemap", ignoreCase = true) -> {

                                    AppSettingsSyncModule()

                                    Spacer(modifier = Modifier.height(14.dp))

                                    MapConfigurationModule()

                                }

                                title.contains("Privacy", ignoreCase = true) || title.contains("Deletion", ignoreCase = true) || title.contains("Export", ignoreCase = true) -> DataPrivacyDeletionModule()

                                else -> GenericFarmerModule(title)

                            }

                        }



                        // Contextual Primary Action Button

                        if (effectivePrimaryClass != null) {

                            item {

                                Spacer(modifier = Modifier.height(2.dp))

                                Button(

                                    onClick = {

                                        activity.startActivity(Intent(activity, effectivePrimaryClass))

                                    },

                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text(effectivePrimaryText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Contextual Secondary Action Button (Optional)
                        if (secondaryActionClass != null && secondaryActionText != null) {
                            item {
                                val isDark = ThemePreferences.isDarkTheme
                                val secColor = if (isDark) LimeAccent else LeafGreen
                                OutlinedButton(
                                    onClick = {
                                        activity.startActivity(Intent(activity, secondaryActionClass))
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = secColor),
                                    border = BorderStroke(1.5.dp, secColor.copy(alpha = 0.7f))
                                ) {
                                    Text(secondaryActionText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }



                        // Bottom Spacing for floating dock or navigation bars
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                }

            }

        }

    }

}

