package com.geopulse.app.ui.premium

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class PremiumColors(
    val pine: Color,
    val forest: Color,
    val evergreen: Color,
    val emerald: Color,
    val lime: Color,
    val mist: Color,
    val muted: Color,
    val glass: Color,
    val cardBg: Color,
    val cardBg2: Color,
    val textColor: Color
)

@Composable
fun rememberPremiumColors(isDarkTheme: Boolean = true): PremiumColors {
    return remember(isDarkTheme) {
        if (isDarkTheme) {
            PremiumColors(
                pine = Color(0xFF0D1B12),
                forest = Color(0xFF123A2A),
                evergreen = Color(0xFF1B4D3E),
                emerald = Color(0xFF22A06B),
                lime = Color(0xFFB7E36A),
                mist = Color(0xFFE7F4E9),
                muted = Color(0xFFB3C9BA),
                glass = Color(0xCC153D2C),
                cardBg = Color(0xFF194734),
                cardBg2 = Color(0xFF102B20),
                textColor = Color.White
            )
        } else {
            PremiumColors(
                pine = Color(0xFFF4F7F4),
                forest = Color(0xFFE2EFE2),
                evergreen = Color(0xFFC8E6C9),
                emerald = Color(0xFF2E7D32),
                lime = Color(0xFF1B5E20),
                mist = Color(0xFF111811),
                muted = Color(0xFF5A665A),
                glass = Color(0xCCFFFFFF),
                cardBg = Color(0xFFFFFFFF),
                cardBg2 = Color(0xFFF9FBF9),
                textColor = Color(0xFF111811)
            )
        }
    }
}

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
        color = colors.pine
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(colors.pine, colors.forest, colors.pine)
                    )
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            val isTablet = maxWidth > 600.dp
            val contentWidth = if (isTablet) 640.dp else maxWidth
            val horizontalPadding = if (maxWidth < 360.dp) 12.dp else 18.dp

            LazyColumn(
                modifier = Modifier
                    .width(contentWidth)
                    .fillMaxHeight(),
                contentPadding = PaddingValues(
                    start = horizontalPadding,
                    end = horizontalPadding,
                    top = 16.dp,
                    bottom = 100.dp // Clear bottom navigation dock
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Header with Farm Dropdown & User Avatar
                item {
                    HomeHeader(
                        colors = colors,
                        onProfileClick = onProfileClick,
                        onNotificationClick = onNotificationClick
                    )
                }

                // 2. Greeting Hero Card ("Good Morning, Nithish 👋") with 3 Working Metric Pills
                item {
                    GreetingHeroSection(
                        colors = colors,
                        onVegetationHealthClick = onVegetationHealthClick,
                        onSoilMoistureClick = onSoilMoistureClick,
                        onCropHealthClick = onCropHealthClick
                    )
                }

                // 3. FieldMapCard with Satellite NDVI Heatmap, Legend, and Interactive Buttons
                item {
                    FieldMapCard(
                        colors = colors,
                        onMapClick = onFieldMapClick,
                        onFieldBoundaryClick = onFieldBoundaryClick
                    )
                }

                // 4. WeatherCard (Chennai TN, 28°C, 3-day forecast)
                item {
                    WeatherForecastCard(colors = colors, onClick = onWeatherClick)
                }

                // 5. Field Insights (Section Header with "View All >") + 2x2 Grid
                item {
                    FieldInsightsSection(
                        colors = colors,
                        onVegetationClick = onVegetationHealthClick,
                        onSoilMoistureClick = onSoilMoistureClick,
                        onYieldClick = onYieldClick,
                        onAlertsClick = onNotificationClick
                    )
                }

                // 6. Next Best Action Card (Sprout image, recommendation, green button)
                item {
                    NextBestActionCard(colors = colors, onClick = onActionClick)
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// 1. Home Header (Farm selector dialog, notification bell, profile avatar)
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
                    color = colors.textColor
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    farmsList.forEach { (farm, field) ->
                        val isCurrent = farm == selectedFarmName
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isCurrent) colors.emerald.copy(alpha = 0.25f) else colors.cardBg,
                            border = BorderStroke(1.dp, if (isCurrent) colors.lime else colors.cardBg2),
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
                                    Text(farm, fontWeight = FontWeight.Bold, color = colors.textColor, fontSize = 14.sp)
                                    Text(field, fontSize = 12.sp, color = colors.muted)
                                }
                                if (isCurrent) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = colors.lime, modifier = Modifier.size(20.dp))
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
            containerColor = colors.forest
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Green leaf circle icon
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(listOf(colors.lime, colors.emerald))
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Eco,
                contentDescription = "Farm Logo",
                tint = colors.pine,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        // Farm name dropdown and subtitle (Interactive button)
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
                    color = colors.textColor,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Select farm",
                    tint = colors.textColor.copy(alpha = 0.8f),
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = selectedFieldSubtitle,
                color = colors.muted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(Modifier.width(8.dp))

        // Notification Bell Icon with Red Dot (Working button)
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .border(1.dp, colors.emerald.copy(alpha = 0.4f), CircleShape)
                .background(colors.glass)
                .clickable(onClick = onNotificationClick)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notifications",
                tint = colors.textColor,
                modifier = Modifier.size(20.dp)
            )
            // Unread notification badge dot
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF5252))
            )
        }

        Spacer(Modifier.width(10.dp))

        // Profile Avatar Circle (Working button)
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .border(1.5.dp, colors.lime.copy(alpha = 0.8f), CircleShape)
                .background(colors.glass)
                .clickable(onClick = onProfileClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile",
                tint = colors.lime,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

// -------------------------------------------------------------------------
// 2. Greeting Hero Card ("Good Morning, Nithish 👋") with 3 Working Metric Pills
// -------------------------------------------------------------------------
@Composable
private fun GreetingHeroSection(
    colors: PremiumColors,
    onVegetationHealthClick: () -> Unit,
    onSoilMoistureClick: () -> Unit,
    onCropHealthClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        colors.evergreen,
                        colors.forest,
                        colors.pine
                    )
                )
            )
            .border(
                1.dp,
                colors.emerald.copy(alpha = 0.5f),
                RoundedCornerShape(26.dp)
            )
            .padding(18.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Good Morning, Nithish 👋",
                color = colors.textColor,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Your fields are performing well today!",
                color = colors.mist.copy(alpha = 0.85f),
                fontSize = 13.sp
            )

            Spacer(Modifier.height(16.dp))

            // 3 Working Metric Pills Row (Soil Moisture, NDVI, Crop Health)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Soil Moisture Pill -> Navigates to Soil Moisture Screen
                FrostedMetricPill(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Water,
                    iconColor = Color(0xFF4FC3F7),
                    label = "Soil Moisture",
                    value = "72%",
                    colors = colors,
                    onClick = onSoilMoistureClick
                )

                // NDVI Pill -> Navigates to Vegetation Health Screen
                FrostedMetricPill(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Eco,
                    iconColor = colors.lime,
                    label = "NDVI",
                    value = "0.84",
                    colors = colors,
                    onClick = onVegetationHealthClick
                )

                // Crop Health Pill -> Navigates to Crop Health / Pest Screen
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
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(colors.cardBg.copy(alpha = 0.85f))
            .border(1.dp, colors.textColor.copy(alpha = 0.14f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(14.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                color = colors.mist,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                color = colors.textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

// -------------------------------------------------------------------------
// 3. FieldMapCard with Satellite NDVI Heatmap, Legend, Location Pin & GPS Button
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
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF0F2619))
            .border(1.dp, colors.emerald.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
            .clickable(onClick = onMapClick)
    ) {
        // Satellite Map Canvas with rendered NDVI Heatmap polygons
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Base lush aerial field color
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF1E4620), Color(0xFF0D2314)),
                    center = Offset(w * 0.4f, h * 0.5f),
                    radius = w * 0.8f
                )
            )

            // Farm Sector Polygons with NDVI heat gradient overlays
            val field1 = Path().apply {
                moveTo(w * 0.08f, h * 0.12f)
                lineTo(w * 0.72f, h * 0.08f)
                lineTo(w * 0.65f, h * 0.85f)
                lineTo(w * 0.12f, h * 0.82f)
                close()
            }
            // Healthy NDVI green zone
            drawPath(field1, Color(0x992E7D32))
            drawPath(field1, Color(0xFF81C784), style = Stroke(width = 2f))

            // Orange/Yellow stress hotspot zone inside field
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xDDE65100), Color(0xBBFBC02D), Color(0x00000000)),
                    center = Offset(w * 0.52f, h * 0.42f),
                    radius = w * 0.22f
                ),
                radius = w * 0.22f,
                center = Offset(w * 0.52f, h * 0.42f)
            )

            // Second mild hotspot
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xAAF57F17), Color(0x00000000)),
                    center = Offset(w * 0.28f, h * 0.62f),
                    radius = w * 0.16f
                ),
                radius = w * 0.16f,
                center = Offset(w * 0.28f, h * 0.62f)
            )
        }

        // Bottom-Left Location Chip: Pin + "Field 1 · 12.5 ha" (Clickable button)
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(14.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(colors.cardBg.copy(alpha = 0.92f))
                .border(1.dp, colors.textColor.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                .clickable { onFieldBoundaryClick() }
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Place,
                contentDescription = "Field boundary",
                tint = colors.lime,
                modifier = Modifier.size(16.dp)
            )
            Column {
                Text("Field 1", color = colors.textColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("12.5 ha", color = colors.mist, fontSize = 9.sp)
            }
        }

        // Bottom-Right GPS Recenter Target Button (Clickable button with feedback)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(14.dp)
                .size(38.dp)
                .clip(CircleShape)
                .background(colors.cardBg.copy(alpha = 0.92f))
                .border(1.dp, colors.textColor.copy(alpha = 0.2f), CircleShape)
                .clickable {
                    recenterTrigger++
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = "Recenter GPS",
                tint = if (recenterTrigger % 2 == 1) colors.emerald else colors.lime,
                modifier = Modifier.size(20.dp)
            )
        }

        // Right-Side Vertical NDVI Scale Legend Bar (Matches Reference Image)
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.cardBg2.copy(alpha = 0.92f))
                .border(1.dp, colors.textColor.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                .padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("NDVI", color = colors.mist, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Vertical gradient line
                Box(
                    modifier = Modifier
                        .width(6.dp)
                        .height(84.dp)
                        .clip(RoundedCornerShape(3.dp))
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
                    modifier = Modifier.height(84.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("1.0", color = colors.textColor, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    Text("0.8", color = colors.mist, fontSize = 8.sp)
                    Text("0.6", color = colors.mist, fontSize = 8.sp)
                    Text("0.4", color = colors.mist, fontSize = 8.sp)
                    Text("0.2", color = colors.mist, fontSize = 8.sp)
                    Text("0.0", color = colors.textColor, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// 4. WeatherCard (Weather Forecast, Chennai TN, 28°C, 3-day forecast)
// -------------------------------------------------------------------------
@Composable
private fun WeatherForecastCard(
    colors: PremiumColors,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(colors.evergreen, colors.cardBg2)
                )
            )
            .border(1.dp, colors.textColor.copy(alpha = 0.12f), RoundedCornerShape(22.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sun behind cloud icon with ambient glow
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFFFA726).copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = "Weather",
                        tint = Color(0xFFFFB74D),
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(Modifier.width(12.dp))

                // Location & Temperature
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Weather Forecast",
                        color = colors.textColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Chennai, TN",
                        color = colors.muted,
                        fontSize = 11.sp
                    )
                    Spacer(Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "28°C",
                            color = colors.textColor,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Partly Cloudy",
                            color = colors.mist,
                            fontSize = 11.sp
                        )
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
}

@Composable
private fun WeatherMiniDay(
    day: String,
    highLow: String,
    isSun: Boolean,
    colors: PremiumColors
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(day, color = colors.mist, fontSize = 9.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(2.dp))
        Icon(
            imageVector = if (isSun) Icons.Default.WbSunny else Icons.Default.Cloud,
            contentDescription = null,
            tint = if (isSun) Color(0xFFFFB74D) else colors.mist,
            modifier = Modifier.size(15.dp)
        )
        Spacer(Modifier.height(2.dp))
        Text(highLow, color = colors.textColor, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
    }
}

// -------------------------------------------------------------------------
// 5. Field Insights (Section Header with "View All >") + 2x2 Grid
// -------------------------------------------------------------------------
@Composable
private fun FieldInsightsSection(
    colors: PremiumColors,
    onVegetationClick: () -> Unit,
    onSoilMoistureClick: () -> Unit,
    onYieldClick: () -> Unit,
    onAlertsClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Section Header with working "View All >" button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Field Insights",
                modifier = Modifier.weight(1f),
                color = colors.textColor,
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
            horizontalArrangement = Arrangement.spacedBy(12.dp)
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
                icon = Icons.Default.Water,
                iconColor = Color(0xFF4FC3F7),
                title = "Soil Moisture",
                value = "72%",
                trend = "+5%",
                trendColor = Color(0xFF4FC3F7),
                sparklinePoints = listOf(0.5f, 0.3f, 0.6f, 0.4f, 0.8f, 0.7f),
                colors = colors,
                onClick = onSoilMoistureClick
            )
        }

        // Row 2: Crop Yield Prediction & Alerts
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
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
                iconColor = Color(0xFFFF9800),
                title = "Alerts",
                value = "2 Active",
                trend = "● Active",
                trendColor = Color(0xFFFF5252),
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
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(listOf(colors.cardBg, colors.cardBg2))
            )
            .border(1.dp, colors.textColor.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        // Icon badge
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(Modifier.height(10.dp))

        Text(
            text = title,
            color = colors.mist,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = value,
            color = colors.textColor,
            fontSize = 17.sp,
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
                // Wave sparkline canvas
                Canvas(
                    modifier = Modifier
                        .widthIn(min = 36.dp, max = 56.dp)
                        .height(20.dp)
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
                    drawPath(path, trendColor, style = Stroke(width = 2.5f))
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

// -------------------------------------------------------------------------
// 6. Next Best Action Card (Working card & button click)
// -------------------------------------------------------------------------
@Composable
private fun NextBestActionCard(colors: PremiumColors, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(colors.cardBg, colors.cardBg2)
                )
            )
            .border(
                1.dp,
                colors.emerald.copy(alpha = 0.45f),
                RoundedCornerShape(22.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail seedling sprout graphic
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF14301E))
                .border(1.dp, colors.lime.copy(alpha = 0.3f), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Spa,
                contentDescription = null,
                tint = colors.lime,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Text(
                text = "NEXT BEST ACTION",
                color = colors.lime,
                fontSize = 10.sp,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Apply nitrogen fertilizer in Field 1 (recommended)",
                color = colors.textColor,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 17.sp
            )
        }

        Spacer(Modifier.width(8.dp))

        // Circular Green Action Arrow Button (Clickable button with ripple)
        Box(
            modifier = Modifier
                .size(40.dp)
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
                tint = colors.pine,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
