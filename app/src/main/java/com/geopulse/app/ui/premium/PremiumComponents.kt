package com.geopulse.app.ui.premium

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// -------------------------------------------------------------------------
// Design System Tokens (Matching Reference Mock Exactly)
// -------------------------------------------------------------------------
data class PremiumColors(
    val bg: Color = Color(0xFF0A130C),
    val bgGradEnd: Color = Color(0xFF0F1E14),
    val cardBg: Color = Color(0xFF112318),
    val cardBgElevated: Color = Color(0xFF162E20),
    val cardBorder: Color = Color(0xFF1E3F2B),
    val glassBorder: Color = Color(0x22FFFFFF),
    val emerald: Color = Color(0xFF22A06B),
    val lime: Color = Color(0xFF86EFAC),
    val accentGreen: Color = Color(0xFF2E7D32),
    val textPrimary: Color = Color(0xFFFFFFFF),
    val textSecondary: Color = Color(0xFF8FA396),
    val textMuted: Color = Color(0xFF5E7366),
    val amber: Color = Color(0xFFFFB74D),
    val blue: Color = Color(0xFF38BDF8),
    val coral: Color = Color(0xFFFF6B6B),
    // Compatibility aliases:
    val pine: Color = Color(0xFF0A130C),
    val forest: Color = Color(0xFF0F1E14),
    val evergreen: Color = Color(0xFF162E20),
    val mist: Color = Color(0xFFC2D9C8),
    val muted: Color = Color(0xFF8FA396),
    val glass: Color = Color(0x99112318),
    val cardBg2: Color = Color(0xFF162E20),
    val textColor: Color = Color(0xFFFFFFFF)
)

@Composable
fun rememberPremiumColors(isDarkTheme: Boolean = true): PremiumColors {
    return remember(isDarkTheme) {
        if (isDarkTheme) {
            PremiumColors()
        } else {
            PremiumColors(
                bg = Color(0xFFF4F7F4),
                bgGradEnd = Color(0xFFE8EFE8),
                cardBg = Color(0xFFFFFFFF),
                cardBgElevated = Color(0xFFF7FAF7),
                cardBorder = Color(0xFFD6E3D6),
                glassBorder = Color(0x33000000),
                emerald = Color(0xFF2E7D32),
                lime = Color(0xFF1B5E20),
                accentGreen = Color(0xFF2E7D32),
                textPrimary = Color(0xFF111811),
                textSecondary = Color(0xFF5A665A),
                textMuted = Color(0xFF8A998A),
                pine = Color(0xFFF4F7F4),
                forest = Color(0xFFE8EFE8),
                evergreen = Color(0xFFD6E3D6),
                mist = Color(0xFF111811),
                muted = Color(0xFF5A665A),
                glass = Color(0xEEFFFFFF),
                cardBg2 = Color(0xFFF7FAF7),
                textColor = Color(0xFF111811)
            )
        }
    }
}

// -------------------------------------------------------------------------
// Premium Home Screen: Clean, Uncluttered, Pixel-Matched Layout
// -------------------------------------------------------------------------
@Composable
fun PremiumHomeScreen(
    isDarkTheme: Boolean = true,
    onFieldMapClick: () -> Unit = {},
    onVegetationHealthClick: () -> Unit = {},
    onSoilMoistureClick: () -> Unit = {},
    onWeatherClick: () -> Unit = {},
    onActionClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onCropHealthClick: () -> Unit = {},
    onFieldBoundaryClick: () -> Unit = {},
    onYieldClick: () -> Unit = {}
) {
    val colors = rememberPremiumColors(isDarkTheme)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = colors.bg
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(colors.bg, colors.bgGradEnd, colors.bg)
                    )
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            val isTablet = maxWidth > 600.dp
            val contentWidth = if (isTablet) 600.dp else maxWidth
            val horizontalPadding = if (maxWidth < 360.dp) 12.dp else 16.dp

            LazyColumn(
                modifier = Modifier
                    .width(contentWidth)
                    .fillMaxHeight(),
                contentPadding = PaddingValues(
                    start = horizontalPadding,
                    end = horizontalPadding,
                    top = 8.dp,
                    bottom = 104.dp // Generous clearance for floating bottom dock
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Clean Top Header: Farm dropdown, notification bell, profile avatar
                item {
                    HomeHeader(
                        colors = colors,
                        onProfileClick = onProfileClick,
                        onNotificationClick = onNotificationClick
                    )
                }

                // 2. Open Hero Greeting: Sits cleanly on background (no heavy outer card box)
                item {
                    GreetingHeroSection(
                        colors = colors,
                        onVegetationHealthClick = onVegetationHealthClick,
                        onSoilMoistureClick = onSoilMoistureClick,
                        onCropHealthClick = onCropHealthClick
                    )
                }

                // 3. Satellite Field Map Card (NDVI heatzones, legend, location chip, GPS target)
                item {
                    FieldMapCard(
                        colors = colors,
                        onMapClick = onFieldMapClick,
                        onFieldBoundaryClick = onFieldBoundaryClick
                    )
                }

                // 4. Compact Weather Forecast Card (Chennai TN, 28°C, 3-day micro forecast)
                item {
                    WeatherForecastCard(
                        colors = colors,
                        onClick = onWeatherClick
                    )
                }

                // 5. Field Insights (Header with "View All >" + clean 2x2 grid of cards)
                item {
                    FieldInsightsSection(
                        colors = colors,
                        onVegetationClick = onVegetationHealthClick,
                        onSoilMoistureClick = onSoilMoistureClick,
                        onYieldClick = onYieldClick,
                        onAlertsClick = onNotificationClick
                    )
                }

                // 6. Next Best Action Card (Sprout graphic, recommendation, green arrow button)
                item {
                    NextBestActionCard(
                        colors = colors,
                        onClick = onActionClick
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// 1. Home Header: Farm Selector, Notification Dot, Profile Avatar
// -------------------------------------------------------------------------
@Composable
private fun HomeHeader(
    colors: PremiumColors,
    onProfileClick: () -> Unit,
    onNotificationClick: () -> Unit
) {
    var showFarmDialog by remember { mutableStateOf(false) }
    var selectedFarmName by remember { mutableStateOf("Green Valley Farm") }
    var selectedFieldSubtitle by remember { mutableStateOf("Field 1 · 12.5 ha") }

    val farmsList = remember {
        listOf(
            "Green Valley Farm" to "Field 1 · 12.5 ha",
            "East Field Beta" to "Sector 2 · 84.2 ha",
            "North Sector Alpha" to "Sector 1 · 120.4 ha",
            "South Center Pivot" to "Sector 3 · 145.0 ha"
        )
    }

    if (showFarmDialog) {
        AlertDialog(
            onDismissRequest = { showFarmDialog = false },
            title = {
                Text(
                    "Switch Farm Property",
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    farmsList.forEach { (farm, field) ->
                        val isCurrent = farm == selectedFarmName
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isCurrent) colors.emerald.copy(alpha = 0.2f) else colors.cardBg,
                            border = BorderStroke(1.dp, if (isCurrent) colors.lime else colors.cardBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedFarmName = farm
                                    selectedFieldSubtitle = field
                                    showFarmDialog = false
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(farm, fontWeight = FontWeight.Bold, color = colors.textPrimary, fontSize = 14.sp)
                                    Text(field, fontSize = 12.sp, color = colors.textSecondary)
                                }
                                if (isCurrent) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = colors.lime, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFarmDialog = false }) {
                    Text("Close", color = colors.lime, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = colors.cardBg
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Circular Leaf Logo Icon
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(colors.lime, colors.emerald))),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Eco,
                contentDescription = "Farm Logo",
                tint = colors.bg,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(Modifier.width(10.dp))

        // Farm name dropdown and subtitle
        Column(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .clickable { showFarmDialog = true }
                .padding(vertical = 2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = selectedFarmName,
                    color = colors.textPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Select farm",
                    tint = colors.textSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = selectedFieldSubtitle,
                color = colors.textSecondary,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(Modifier.width(8.dp))

        // Notification Bell Icon with Red Dot
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(colors.cardBg)
                .border(1.dp, colors.cardBorder, CircleShape)
                .clickable(onClick = onNotificationClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notifications",
                tint = colors.textPrimary,
                modifier = Modifier.size(19.dp)
            )
            // Red alert badge dot
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 8.dp, end = 8.dp)
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF5252))
            )
        }

        Spacer(Modifier.width(10.dp))

        // Profile Avatar Circle
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .border(1.5.dp, colors.lime, CircleShape)
                .background(colors.cardBg)
                .clickable(onClick = onProfileClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile",
                tint = colors.lime,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// -------------------------------------------------------------------------
// 2. Greeting Hero Section: Clean, Breathable, Sits Directly on Background
// -------------------------------------------------------------------------
@Composable
private fun GreetingHeroSection(
    colors: PremiumColors,
    onVegetationHealthClick: () -> Unit,
    onSoilMoistureClick: () -> Unit,
    onCropHealthClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "Good Morning, Nithish 👋",
            color = colors.textPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = "Your fields are performing well today!",
            color = colors.textSecondary,
            fontSize = 13.sp
        )

        Spacer(Modifier.height(14.dp))

        // 3 Working Frosted Metric Pills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FrostedMetricPill(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.WaterDrop,
                iconColor = colors.blue,
                label = "Soil Moisture",
                value = "72%",
                colors = colors,
                onClick = onSoilMoistureClick
            )

            FrostedMetricPill(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Eco,
                iconColor = colors.lime,
                label = "NDVI",
                value = "0.84",
                colors = colors,
                onClick = onVegetationHealthClick
            )

            FrostedMetricPill(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Favorite,
                iconColor = colors.emerald,
                label = "Crop Health",
                value = "Optimal",
                colors = colors,
                onClick = onCropHealthClick
            )
        }
    }
}

@Composable
private fun FrostedMetricPill(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconColor: Color,
    label: String,
    value: String,
    colors: PremiumColors,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = colors.cardBg,
        border = BorderStroke(1.dp, colors.cardBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(15.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    color = colors.textSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(1.dp))
                Text(
                    text = value,
                    color = colors.textPrimary,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }
}

// -------------------------------------------------------------------------
// 3. FieldMapCard: High-Res Satellite View, Heatmap, Legend & Target Button
// -------------------------------------------------------------------------
@Composable
private fun FieldMapCard(
    colors: PremiumColors,
    onMapClick: () -> Unit,
    onFieldBoundaryClick: () -> Unit
) {
    var recenterTrigger by remember { mutableStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0C1F13))
            .border(1.dp, colors.cardBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onMapClick)
    ) {
        // Satellite Map Canvas with rendered NDVI Heatmap polygons
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Base lush aerial landscape with field contours
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF1B3D22), Color(0xFF0A180E)),
                    center = Offset(w * 0.45f, h * 0.5f),
                    radius = w * 0.85f
                )
            )

            // Crop row texture lines (natural agricultural appearance)
            var lineX = 0f
            while (lineX < w + h) {
                drawLine(
                    color = Color(0x0CFFFFFF),
                    start = Offset(lineX, 0f),
                    end = Offset(lineX - h * 0.6f, h),
                    strokeWidth = 1.2f
                )
                lineX += 28f
            }

            // Neighboring field parcel boundaries (subtle)
            val neighborField = Path().apply {
                moveTo(w * 0.70f, h * 0.05f)
                lineTo(w * 0.95f, h * 0.10f)
                lineTo(w * 0.92f, h * 0.75f)
                lineTo(w * 0.68f, h * 0.70f)
                close()
            }
            drawPath(neighborField, Color(0x441B4324))
            drawPath(neighborField, Color(0x444CAF50), style = Stroke(width = 1f))

            // Main Field 1 Polygon
            val field1 = Path().apply {
                moveTo(w * 0.06f, h * 0.10f)
                lineTo(w * 0.68f, h * 0.06f)
                lineTo(w * 0.62f, h * 0.88f)
                lineTo(w * 0.10f, h * 0.84f)
                close()
            }
            // Healthy NDVI green zone fill
            drawPath(field1, Color(0xAA2E7D32))
            drawPath(field1, Color(0xFF81C784), style = Stroke(width = 2.2f))

            // Amber/Yellow stress hotspot zone inside field
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xEED84315), Color(0xBBFBC02D), Color(0x00000000)),
                    center = Offset(w * 0.48f, h * 0.40f),
                    radius = w * 0.20f
                ),
                radius = w * 0.20f,
                center = Offset(w * 0.48f, h * 0.40f)
            )

            // Secondary mild moisture hotspot
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x99F57F17), Color(0x00000000)),
                    center = Offset(w * 0.26f, h * 0.60f),
                    radius = w * 0.15f
                ),
                radius = w * 0.15f,
                center = Offset(w * 0.26f, h * 0.60f)
            )
        }

        // Bottom-Left Location Chip: Pin + "Field 1 · 12.5 ha"
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.cardBg.copy(alpha = 0.92f))
                .border(1.dp, colors.cardBorder, RoundedCornerShape(12.dp))
                .clickable { onFieldBoundaryClick() }
                .padding(horizontal = 9.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Place,
                contentDescription = "Field boundary",
                tint = colors.lime,
                modifier = Modifier.size(15.dp)
            )
            Column {
                Text("Field 1", color = colors.textPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("12.5 ha", color = colors.textSecondary, fontSize = 9.sp)
            }
        }

        // Bottom-Right GPS Recenter Target Button
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp)
                .size(36.dp)
                .clip(CircleShape)
                .background(colors.cardBg.copy(alpha = 0.92f))
                .border(1.dp, colors.cardBorder, CircleShape)
                .clickable { recenterTrigger++ },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = "Recenter GPS",
                tint = if (recenterTrigger % 2 == 1) colors.emerald else colors.lime,
                modifier = Modifier.size(18.dp)
            )
        }

        // Right-Side Vertical NDVI Scale Legend Bar
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(colors.cardBg.copy(alpha = 0.92f))
                .border(1.dp, colors.cardBorder, RoundedCornerShape(10.dp))
                .padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("NDVI", color = colors.textSecondary, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Vertical gradient line
                Box(
                    modifier = Modifier
                        .width(5.dp)
                        .height(80.dp)
                        .clip(RoundedCornerShape(2.5.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF2E7D32), // 1.0 Green
                                    Color(0xFF81C784), // 0.8 Lime
                                    Color(0xFFFFEE58), // 0.6 Yellow
                                    Color(0xFFFFA726), // 0.4 Orange
                                    Color(0xFFE53935)  // 0.0 Red
                                )
                            )
                        )
                )

                Spacer(Modifier.width(4.dp))

                // Scale labels
                Column(
                    modifier = Modifier.height(80.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("1.0", color = colors.textPrimary, fontSize = 7.5.sp, fontWeight = FontWeight.Bold)
                    Text("0.8", color = colors.textSecondary, fontSize = 7.5.sp)
                    Text("0.6", color = colors.textSecondary, fontSize = 7.5.sp)
                    Text("0.4", color = colors.textSecondary, fontSize = 7.5.sp)
                    Text("0.2", color = colors.textSecondary, fontSize = 7.5.sp)
                    Text("0.0", color = colors.textPrimary, fontSize = 7.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// 4. WeatherCard: Chennai TN, 28°C, Partly Cloudy, 3-Day Micro Forecast
// -------------------------------------------------------------------------
@Composable
private fun WeatherForecastCard(
    colors: PremiumColors,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = colors.cardBg,
        border = BorderStroke(1.dp, colors.cardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sun behind cloud icon with ambient glow
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.amber.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.WbSunny,
                    contentDescription = "Weather",
                    tint = colors.amber,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            // Location & Temperature Column
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(Modifier.width(2.dp))
                    Text(
                        text = "Chennai, TN",
                        color = colors.textSecondary,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "28°C",
                        color = colors.textPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Partly Cloudy",
                        color = colors.textSecondary,
                        fontSize = 11.5.sp
                    )
                    Spacer(Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = colors.emerald.copy(alpha = 0.18f)
                    ) {
                        Text(
                            text = "+8%",
                            color = colors.lime,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // 3-day forecast columns
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                WeatherMiniDay(day = "Today", highLow = "28°/22°", isSun = false, colors = colors)
                WeatherMiniDay(day = "Mon", highLow = "29°/23°", isSun = false, colors = colors)
                WeatherMiniDay(day = "Tue", highLow = "30°/24°", isSun = true, colors = colors)
            }
        }
    }
}

@Composable
private fun WeatherMiniDay(
    day: String,
    highLow: String,
    isSun: Boolean,
    colors: PremiumColors
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(day, color = colors.textSecondary, fontSize = 9.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(2.dp))
        Icon(
            imageVector = if (isSun) Icons.Default.WbSunny else Icons.Default.Cloud,
            contentDescription = null,
            tint = if (isSun) colors.amber else colors.textSecondary,
            modifier = Modifier.size(15.dp)
        )
        Spacer(Modifier.height(2.dp))
        Text(highLow, color = colors.textPrimary, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
    }
}

// -------------------------------------------------------------------------
// 5. Field Insights: Section Header + 2x2 Grid of Cards
// -------------------------------------------------------------------------
@Composable
private fun FieldInsightsSection(
    colors: PremiumColors,
    onVegetationClick: () -> Unit,
    onSoilMoistureClick: () -> Unit,
    onYieldClick: () -> Unit,
    onAlertsClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Section Header with working "View All >" button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Field Insights",
                modifier = Modifier.weight(1f),
                color = colors.textPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "View All >",
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(onClick = onVegetationClick)
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                color = colors.lime,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Row 1: Vegetation Health & Soil Moisture
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            InsightGridCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Eco,
                iconColor = colors.lime,
                title = "Vegetation Health",
                value = "Good",
                trend = "+12%",
                trendColor = colors.lime,
                sparklinePoints = listOf(0.3f, 0.5f, 0.4f, 0.7f, 0.6f, 0.9f),
                colors = colors,
                onClick = onVegetationClick
            )

            InsightGridCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.WaterDrop,
                iconColor = colors.blue,
                title = "Soil Moisture",
                value = "72%",
                trend = "+5%",
                trendColor = colors.blue,
                sparklinePoints = listOf(0.5f, 0.3f, 0.6f, 0.4f, 0.8f, 0.7f),
                colors = colors,
                onClick = onSoilMoistureClick
            )
        }

        // Row 2: Crop Yield Prediction & Alerts
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            InsightGridCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Spa,
                iconColor = Color(0xFFC0CA33),
                title = "Crop Yield Prediction",
                value = "8.4 t/ha",
                trend = "+8%",
                trendColor = Color(0xFFC0CA33),
                sparklinePoints = listOf(0.4f, 0.45f, 0.6f, 0.55f, 0.75f, 0.85f),
                colors = colors,
                onClick = onYieldClick
            )

            InsightGridCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Notifications,
                iconColor = colors.coral,
                title = "Alerts",
                value = "2 Active",
                trend = "● Active",
                trendColor = colors.coral,
                sparklinePoints = null,
                colors = colors,
                onClick = onAlertsClick
            )
        }
    }
}

@Composable
private fun InsightGridCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconColor: Color,
    title: String,
    value: String,
    trend: String,
    trendColor: Color,
    sparklinePoints: List<Float>?,
    colors: PremiumColors,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = colors.cardBg,
        border = BorderStroke(1.dp, colors.cardBorder)
    ) {
        Column(modifier = Modifier.padding(13.dp)) {
            // Icon badge
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(Modifier.height(9.dp))

            Text(
                text = title,
                color = colors.textSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = value,
                color = colors.textPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            // Sparkline and trend indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                if (sparklinePoints != null) {
                    // Smooth wave sparkline canvas
                    Canvas(
                        modifier = Modifier
                            .widthIn(min = 36.dp, max = 56.dp)
                            .height(18.dp)
                    ) {
                        val w = size.width
                        val h = size.height
                        val step = w / (sparklinePoints.size - 1)
                        val path = Path().apply {
                            moveTo(0f, h - (sparklinePoints[0] * h))
                            for (i in 1 until sparklinePoints.size) {
                                val prevX = (i - 1) * step
                                val prevY = h - (sparklinePoints[i - 1] * h)
                                val curX = i * step
                                val curY = h - (sparklinePoints[i] * h)
                                cubicTo(
                                    (prevX + curX) / 2f, prevY,
                                    (prevX + curX) / 2f, curY,
                                    curX, curY
                                )
                            }
                        }
                        drawPath(path, trendColor, style = Stroke(width = 2.2f))
                    }
                } else {
                    Spacer(Modifier.width(1.dp))
                }

                Text(
                    text = trend,
                    color = trendColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// -------------------------------------------------------------------------
// 6. Next Best Action Card: Clean Horizontal Card with Seedling & Action Arrow
// -------------------------------------------------------------------------
@Composable
private fun NextBestActionCard(
    colors: PremiumColors,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = colors.cardBg,
        border = BorderStroke(1.dp, colors.cardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Seedling Sprout Graphic Container
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF132F1D))
                    .border(1.dp, colors.lime.copy(alpha = 0.25f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Spa,
                    contentDescription = null,
                    tint = colors.lime,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = "NEXT BEST ACTION",
                    color = colors.lime,
                    fontSize = 10.sp,
                    letterSpacing = 0.8.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = "Apply nitrogen fertilizer in Field 1 (recommended)",
                    color = colors.textPrimary,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 17.sp
                )
            }

            Spacer(Modifier.width(8.dp))

            // Circular Green Action Arrow Button
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(colors.lime, colors.emerald))
                    )
                    .clickable(onClick = onClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Action",
                    tint = colors.bg,
                    modifier = Modifier.size(19.dp)
                )
            }
        }
    }
}
