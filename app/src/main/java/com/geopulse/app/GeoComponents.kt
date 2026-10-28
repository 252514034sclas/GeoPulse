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
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geopulse.app.ui.theme.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

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
 * Crisp, high-end glass surface modifier without runtime blur filters.
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
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
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

@Composable
fun GeoBottomBar(currentScreen: Int, activity: ComponentActivity) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        shape = RoundedCornerShape(28.dp),
        color = DarkSurface.copy(alpha = 0.96f),
        shadowElevation = 12.dp,
        border = BorderStroke(1.dp, CardBorderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
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

/**
 * Finger-Dragged Bottom Navigation Dock (No Tapping).
 * Users physically drag and pull the dock horizontally to scrub and switch between screens.
 */
@Composable
fun DraggableBottomDock(
    pagerState: PagerState,
    coroutineScope: CoroutineScope
) {
    val totalPages = 4

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .pointerInput(pagerState) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        coroutineScope.launch {
                            val offset = pagerState.currentPageOffsetFraction
                            val targetPage = when {
                                offset > 0.25f -> (pagerState.currentPage + 1).coerceAtMost(totalPages - 1)
                                offset < -0.25f -> (pagerState.currentPage - 1).coerceAtLeast(0)
                                else -> pagerState.currentPage
                            }
                            pagerState.animateScrollToPage(targetPage)
                        }
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        coroutineScope.launch {
                            val screenWidth = size.width.toFloat().coerceAtLeast(1f)
                            val deltaPages = -dragAmount / screenWidth * 1.5f
                            val newOffset = (pagerState.currentPage + pagerState.currentPageOffsetFraction + deltaPages)
                                .coerceIn(0f, (totalPages - 1).toFloat())
                            val pageToScroll = newOffset.toInt()
                            val pageOffset = newOffset - pageToScroll
                            pagerState.scrollToPage(pageToScroll, pageOffset)
                        }
                    }
                )
            },
        shape = RoundedCornerShape(28.dp),
        color = DarkSurface.copy(alpha = 0.96f),
        shadowElevation = 12.dp,
        border = BorderStroke(1.dp, CardBorderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tabs = listOf("Home", "Map", "Activity", "Profile")
            val icons = listOf(Icons.Default.Home, Icons.Default.Place, Icons.Default.Assessment, Icons.Default.Person)

            tabs.forEachIndexed { index, label ->
                val isSelected = pagerState.currentPage == index
                val contentColor = if (isSelected) LimeAccent else TextMuted
                val backgroundColor = if (isSelected) EmeraldPrimary else Color.Transparent

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = backgroundColor
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(icons[index], contentDescription = label, tint = contentColor, modifier = Modifier.size(20.dp))
                        if (isSelected) {
                            Text(label, color = contentColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
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
    val backgroundColor = if (isSelected) EmeraldPrimary else Color.Transparent
    val contentColor = if (isSelected) LimeAccent else TextMuted

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
            Icon(icon, contentDescription = label, tint = contentColor, modifier = Modifier.size(20.dp))
            if (isSelected) {
                Text(label, color = contentColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

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
                    .padding(padding)
            ) {
                val isTablet = maxWidth > 600.dp
                val horizontalPadding = if (isTablet) 32.dp else 16.dp
                val heroGradient = remember {
                    Brush.linearGradient(colors = listOf(GradientHeroStart, GradientHeroMid, GradientHeroEnd))
                }

                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(animationSpec = tween(400)) + slideInVertically(animationSpec = tween(400), initialOffsetY = { 30 })
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = horizontalPadding, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            // Crisp Hero Card with professional gradient
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .then(if (isTablet) Modifier.widthIn(max = 800.dp) else Modifier)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(heroGradient)
                                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                                    .padding(if (isTablet) 32.dp else 24.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = Color.White.copy(alpha = 0.15f),
                                        modifier = Modifier.size(72.dp),
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = title,
                                                tint = Color.White,
                                                modifier = Modifier.size(36.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = title,
                                        fontSize = if (isTablet) 26.sp else 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = subtitle,
                                        fontSize = if (isTablet) 16.sp else 15.sp,
                                        color = Color.White.copy(alpha = 0.85f),
                                        textAlign = TextAlign.Center,
                                        lineHeight = 22.sp
                                    )
                                }
                            }
                        }

                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .then(if (isTablet) Modifier.widthIn(max = 800.dp) else Modifier)
                                    .glassSurface(cornerRadius = 18.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
                                        Text("Optimal & Synchronized", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Cloud Telemetry", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 14.sp)
                                        Text("Live Feed Active", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(8.dp))
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
                                    Text(nextButtonText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
    }
}
