package com.geopulse.app.ui.premium
import com.geopulse.app.map.*

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.automirrored.filled.TrendingUp
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geopulse.app.*

// -------------------------------------------------------------------------
// Design System Tokens (Farmer-First Palette with High-Contrast Readability)
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
// Strict 5-Tier Decision-Oriented Farmer-First Home Screen
// 1. Header (GeoPulse, greeting, farm switcher, notification bell)
// 2. Farm Map (250dp, real satellite/normal basemap toggle, boundary, image date)
// 3. Farm Condition (4 simple cards: Crop Health, Soil Water, Weather, Farm Alerts)
// 4. Today's Advice (What is happening, What to do, When, Why + Action)
// 5. Quick Actions (6 primary cards + expandable "View All Tools" sheet)
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
    onYieldClick: () -> Unit = {},
    onNavigate: (Class<*>) -> Unit = {}
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
                    top = 10.dp,
                    bottom = 108.dp // Space for floating bottom dock
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Tier 1: Top Header (Logo, greeting, farm switcher dropdown, notifications, profile)
                item {
                    HomeHeader(
                        colors = colors,
                        onProfileClick = onProfileClick,
                        onNotificationClick = onNotificationClick
                    )
                }

                // Tier 2: Farm Map (250dp, Satellite/Normal Map toggle, observation date, field area)
                item {
                    FieldMapCard(
                        colors = colors,
                        onMapClick = onFieldMapClick,
                        onFieldBoundaryClick = onFieldBoundaryClick
                    )
                }

                // Tier 3: Farm Condition (4 simple cards: Crop Health, Soil Water, Weather, Farm Alerts)
                item {
                    FarmConditionSection(
                        colors = colors,
                        onCropHealthClick = onCropHealthClick,
                        onSoilMoistureClick = onSoilMoistureClick,
                        onWeatherClick = onWeatherClick,
                        onAlertsClick = onNotificationClick
                    )
                }

                // Tier 4: Today's Advice (What is happening, What to do, When, Why + Action)
                item {
                    TodaysAdviceSection(
                        colors = colors,
                        onActionClick = onActionClick
                    )
                }

                // Tier 5: Quick Actions (6 primary cards + expandable "View All Tools" sheet)
                item {
                    QuickActionsSection(
                        colors = colors,
                        onNavigate = onNavigate
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// 1. Home Header: Farm Selector, Notification Bell, User Avatar
// -------------------------------------------------------------------------
@Composable
private fun HomeHeader(
    colors: PremiumColors,
    onProfileClick: () -> Unit,
    onNotificationClick: () -> Unit
) {
    var showFarmDialog by remember { mutableStateOf(false) }
    var selectedFarmName by remember { mutableStateOf("Green Valley Farm") }
    var selectedFieldSubtitle by remember { mutableStateOf("Field 1 · 12.5 ha (30.9 ac)") }

    val farmsList = remember {
        listOf(
            "Green Valley Farm" to "Field 1 · 12.5 ha (30.9 ac)",
            "East Field Beta" to "Sector 2 · 34.1 ha (84.2 ac)",
            "North Sector Alpha" to "Sector 1 · 48.7 ha (120.4 ac)",
            "South Center Pivot" to "Sector 3 · 58.7 ha (145.0 ac)"
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
                .size(40.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(colors.lime, colors.emerald))),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Eco,
                contentDescription = "Farm Logo",
                tint = colors.bg,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(Modifier.width(10.dp))

        // Farm name dropdown and greeting subtitle
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
                    fontSize = 16.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Select farm",
                    tint = colors.lime,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = "Good Morning, Nithish 👋 · $selectedFieldSubtitle",
                color = colors.textSecondary,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.width(8.dp))

        // Notification Bell Icon with Red Alert Indicator
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

        Spacer(Modifier.width(8.dp))

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
// 2. FieldMapCard: Real Interactive MapLibre Satellite Farm Map
// -------------------------------------------------------------------------
@Composable
private fun FieldMapCard(
    colors: PremiumColors,
    onMapClick: () -> Unit,
    onFieldBoundaryClick: () -> Unit
) {
    InteractiveSatelliteFarmMap(
        modifier = Modifier.fillMaxWidth(),
        farmName = "Green Valley Farm",
        onOpenFullGisMap = onMapClick,
        onDetailedHealthClick = { onFieldBoundaryClick() }
    )
}

// -------------------------------------------------------------------------
// 3. Farm Condition: 4 Simple Status Cards in a 2x2 Grid (Plain Language)
// -------------------------------------------------------------------------
@Composable
private fun FarmConditionSection(
    colors: PremiumColors,
    onCropHealthClick: () -> Unit,
    onSoilMoistureClick: () -> Unit,
    onWeatherClick: () -> Unit,
    onAlertsClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Farm Condition",
                color = colors.textPrimary,
                fontSize = 16.5.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Live Satellite & Sensor Status",
                color = colors.textSecondary,
                fontSize = 11.sp
            )
        }

        // 2x2 Grid: Row 1
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ConditionStatusCard(
                modifier = Modifier.weight(1f),
                title = "Crop Health",
                value = "84% · Good",
                subtitle = "Sentinel-2C NDVI: 0.33",
                icon = Icons.Default.Eco,
                tint = colors.lime,
                colors = colors,
                onClick = onCropHealthClick
            )

            ConditionStatusCard(
                modifier = Modifier.weight(1f),
                title = "Soil Water",
                value = "72% · Moist",
                subtitle = "NDMI: 0.08 ? Water needed",
                icon = Icons.Default.WaterDrop,
                tint = colors.blue,
                colors = colors,
                onClick = onSoilMoistureClick
            )
        }

        // 2x2 Grid: Row 2
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ConditionStatusCard(
                modifier = Modifier.weight(1f),
                title = "Weather",
                value = "28°C · Spray Safe",
                subtitle = "Wind 8 km/h · Calm",
                icon = Icons.Default.WbSunny,
                tint = colors.amber,
                colors = colors,
                onClick = onWeatherClick
            )

            ConditionStatusCard(
                modifier = Modifier.weight(1f),
                title = "Farm Alerts",
                value = "1 Notice",
                subtitle = "Irrigate root zone",
                icon = Icons.Default.Notifications,
                tint = colors.coral,
                colors = colors,
                onClick = onAlertsClick
            )
        }
    }
}

@Composable
private fun ConditionStatusCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    tint: Color,
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
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = colors.textSecondary,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(tint.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = value,
                color = colors.textPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(2.dp))

            Text(
                text = subtitle,
                color = tint,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// -------------------------------------------------------------------------
// 4. Today's Advice: Clear Practical Answers (What, What to do, When, Why)
// -------------------------------------------------------------------------
@Composable
private fun TodaysAdviceSection(
    colors: PremiumColors,
    onActionClick: () -> Unit
) {
    val context = LocalContext.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = colors.cardBgElevated,
        border = BorderStroke(1.dp, colors.lime.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header with Sprout Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(colors.lime.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Spa,
                            contentDescription = null,
                            tint = colors.lime,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "TODAY'S FARM ADVICE",
                            color = colors.lime,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp
                        )
                        Text(
                            text = "Timely guidance based on soil and weather",
                            color = colors.textSecondary,
                            fontSize = 10.5.sp
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = colors.emerald.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "High Priority",
                        color = colors.lime,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            HorizontalDivider(color = colors.cardBorder)

            // 4 Clear Farmer Guidance Rows
            AdviceRow(label = "What is happening:", detail = "Soil moisture is low (34%) and Sentinel-2 NDVI confirms canopy water stress.", colors = colors)
            AdviceRow(label = "What to do:", detail = "Apply 25-30 mm irrigation to Field 1 before applying top-dress Nitrogen fertilizer.", colors = colors)
            AdviceRow(label = "When:", detail = "Early morning before 10:00 AM to prevent evaporative water loss.", colors = colors)
            AdviceRow(label = "Why:", detail = "Adequate soil moisture is required for nitrogen uptake; dry application causes fertilizer burn.", colors = colors)

            Spacer(Modifier.height(4.dp))

            // Primary Action Button
            Button(
                onClick = onActionClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.lime,
                    contentColor = Color(0xFF07140B)
                )
            ) {
                Text(
                    text = "View Fertilizer Recommendation >",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp
                )
            }

            // Secondary Call Kisan Helpline Prompt
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:18001801551"))
                        context.startActivity(dialIntent)
                    },
                shape = RoundedCornerShape(10.dp),
                color = colors.cardBg,
                border = BorderStroke(1.dp, colors.cardBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        tint = colors.emerald,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Need agronomist guidance? Call Kisan Helpline 1800-180-1551",
                        fontSize = 11.sp,
                        color = colors.textSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun AdviceRow(
    label: String,
    detail: String,
    colors: PremiumColors
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
            fontSize = 12.sp,
            modifier = Modifier.width(115.dp)
        )
        Text(
            text = detail,
            color = colors.textSecondary,
            fontSize = 12.sp,
            lineHeight = 16.5.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

// -------------------------------------------------------------------------
// 5. Quick Actions: Exactly 6 Primary Cards + Expandable "All 14 Tools"
// -------------------------------------------------------------------------
data class HubTileItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val tint: Color,
    val targetClass: Class<*>
)

@Composable
private fun QuickActionsSection(
    colors: PremiumColors,
    onNavigate: (Class<*>) -> Unit
) {
    var showAllTools by remember { mutableStateOf(false) }

    // 6 Primary Daily Operations Tools (Never overwhelming)
    val primaryTools = remember {
        listOf(
            HubTileItem("Watering Advice", "Next in 3 days", Icons.Default.WaterDrop, colors.blue, Screen13Activity::class.java),
            HubTileItem("Crop Health", "Optimal vigor · 84%", Icons.Default.Eco, colors.lime, Screen11Activity::class.java),
            HubTileItem("Weather Forecast", "28°C · Safe to Spray", Icons.Default.WbSunny, colors.amber, Screen14Activity::class.java),
            HubTileItem("Fertilizer Guide", "Urea 45 kg/ac needed", Icons.Default.Spa, colors.emerald, Screen20Activity::class.java),
            HubTileItem("Pest Advisory", "Low risk · Neem spray", Icons.Default.BugReport, colors.coral, Screen15Activity::class.java),
            HubTileItem("Mandi Prices", "Corn ₹2,380/Q · Steady", Icons.AutoMirrored.Filled.TrendingUp, colors.lime, Screen27Activity::class.java)
        )
    }

    // Additional Specialized Tools (Cleanly accessible when needed)
    val secondaryTools = remember {
        listOf(
            HubTileItem("Yield Predictor", "35 Q/ac (+8.4%)", Icons.Default.Assessment, colors.lime, Screen16Activity::class.java),
            HubTileItem("GPS Boundary", "12.5 ha Verified", Icons.Default.Place, colors.amber, Screen17Activity::class.java),
            HubTileItem("Crop Calendar", "Tasseling (Day 48)", Icons.Default.CalendarToday, colors.blue, Screen18Activity::class.java),
            HubTileItem("Field Plowing", "Workable soil", Icons.Default.Agriculture, colors.amber, Screen19Activity::class.java),
            HubTileItem("Farm Budget", "+₹23.5k/ac profit", Icons.Default.AttachMoney, colors.emerald, Screen22Activity::class.java),
            HubTileItem("Kisan Helpline", "1800-180-1551", Icons.Default.Phone, colors.emerald, Screen26Activity::class.java),
            HubTileItem("Alert Center", "Rain Friday", Icons.Default.Warning, colors.coral, Screen28Activity::class.java),
            HubTileItem("Organic Soil", "Compost & Biochar", Icons.Default.Spa, colors.lime, Screen25Activity::class.java)
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Quick Farm Actions",
                    color = colors.textPrimary,
                    fontSize = 16.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Direct 1-tap access to everyday farming operations",
                    color = colors.textSecondary,
                    fontSize = 11.sp
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = colors.emerald.copy(alpha = 0.18f)
            ) {
                Text(
                    text = "6 Core Actions",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.emerald,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        // 6 Primary Cards (2 Columns x 3 Rows)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            primaryTools.chunked(2).forEach { rowPair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowPair.forEach { tile ->
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onNavigate(tile.targetClass) },
                            shape = RoundedCornerShape(12.dp),
                            color = colors.cardBg,
                            border = BorderStroke(1.dp, colors.cardBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 11.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(9.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(tile.tint.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = tile.icon,
                                        contentDescription = tile.title,
                                        tint = tile.tint,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = tile.title,
                                        color = colors.textPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = tile.subtitle,
                                        color = tile.tint,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 10.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Expandable / Bottom Sheet Drawer Button for Remaining Tools
        OutlinedButton(
            onClick = { showAllTools = !showAllTools },
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, colors.lime.copy(alpha = 0.4f)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.lime)
        ) {
            Text(
                text = if (showAllTools) "Hide Additional Tools ▴" else "View All 14 Farming Tools ▾",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Expandable Additional Tools
        AnimatedVisibility(
            visible = showAllTools,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                secondaryTools.chunked(2).forEach { rowPair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowPair.forEach { tile ->
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onNavigate(tile.targetClass) },
                                shape = RoundedCornerShape(12.dp),
                                color = colors.cardBgElevated,
                                border = BorderStroke(1.dp, colors.cardBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(9.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(tile.tint.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = tile.icon,
                                            contentDescription = tile.title,
                                            tint = tile.tint,
                                            modifier = Modifier.size(17.dp)
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = tile.title,
                                            color = colors.textPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = tile.subtitle,
                                            color = tile.tint,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 9.5.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
