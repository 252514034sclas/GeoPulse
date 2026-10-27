package com.geopulse.app.ui.premium

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
    onProfileClick: () -> Unit = {}
) {
    val colors = rememberPremiumColors(isDarkTheme)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = colors.pine
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(colors.pine, colors.forest, colors.pine)
                    )
                ),
            contentPadding = PaddingValues(
                start = 18.dp,
                end = 18.dp,
                top = 20.dp,
                bottom = 90.dp // Extra bottom padding for floating decoupled navigation bar
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Header(colors = colors, onProfileClick = onProfileClick)
            }

            item {
                HeroSection(colors = colors, onClick = onVegetationHealthClick)
            }

            item {
                FieldMapPreview(colors = colors, onClick = onFieldMapClick)
            }

            item {
                WeatherCard(colors = colors, onClick = onWeatherClick)
            }

            item {
                SectionHeading(
                    title = "Field Intelligence",
                    action = "View insights",
                    colors = colors,
                    onClick = onVegetationHealthClick
                )
            }

            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    InsightCard(
                        modifier = Modifier.weight(1f),
                        icon = "✦",
                        title = "Vegetation Health",
                        value = "Good",
                        detail = "NDVI  0.84",
                        accent = colors.lime,
                        colors = colors,
                        onClick = onVegetationHealthClick
                    )

                    InsightCard(
                        modifier = Modifier.weight(1f),
                        icon = "≈",
                        title = "Soil Moisture",
                        value = "72%",
                        detail = "Within target",
                        accent = Color(0xFF6BD9F5),
                        colors = colors,
                        onClick = onSoilMoistureClick
                    )
                }
            }

            item {
                ActionCard(colors = colors, onClick = onActionClick)
            }
        }
    }
}

@Composable
private fun Header(
    colors: PremiumColors,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(colors.lime, colors.emerald)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "G",
                color = colors.pine,
                fontSize = 25.sp,
                fontWeight = FontWeight.Black
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Text(
                "GeoPulse",
                color = colors.textColor,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "AGRICULTURAL INTELLIGENCE",
                color = colors.muted,
                fontSize = 10.sp,
                letterSpacing = 1.2.sp
            )
        }

        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .border(1.dp, colors.emerald.copy(alpha = 0.5f), CircleShape)
                .background(colors.glass)
                .clickable(onClick = onProfileClick),
            contentAlignment = Alignment.Center
        ) {
            Text("●", color = colors.lime, fontSize = 19.sp)
        }
    }
}

@Composable
private fun HeroSection(colors: PremiumColors, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 235.dp)
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
                colors.emerald.copy(alpha = 0.55f),
                RoundedCornerShape(26.dp)
            )
            .clickable(onClick = onClick)
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "YOUR FARM, AT A GLANCE",
                        color = colors.lime,
                        fontSize = 10.sp,
                        letterSpacing = 1.3.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Green Valley Farm",
                        color = colors.textColor,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Field 01  ·  12.5 hectares",
                        color = colors.mist,
                        fontSize = 13.sp
                    )
                }

                Text(
                    "↗",
                    color = colors.lime,
                    fontSize = 28.sp
                )
            }

            Spacer(Modifier.height(28.dp))

            Text(
                "Field performance",
                color = colors.mist,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricPill(
                    modifier = Modifier.weight(1f),
                    label = "SOIL MOISTURE",
                    value = "72%",
                    icon = "≈",
                    colors = colors
                )
                MetricPill(
                    modifier = Modifier.weight(1f),
                    label = "NDVI",
                    value = "0.84",
                    icon = "✦",
                    colors = colors
                )
            }
        }
    }
}

@Composable
private fun MetricPill(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: String,
    colors: PremiumColors
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(17.dp))
            .background(colors.cardBg.copy(alpha = 0.85f))
            .border(
                1.dp,
                colors.textColor.copy(alpha = 0.16f),
                RoundedCornerShape(17.dp)
            )
            .padding(12.dp)
    ) {
        Text(icon, color = colors.lime, fontSize = 19.sp)
        Spacer(Modifier.height(7.dp))
        Text(
            value,
            color = colors.textColor,
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            label,
            color = colors.mist,
            fontSize = 9.sp,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun FieldMapPreview(
    colors: PremiumColors,
    onClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionHeading(
            title = "Satellite field map",
            action = "Explore ↗",
            colors = colors,
            onClick = onClick
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            colors.evergreen,
                            colors.forest,
                            colors.emerald
                        )
                    )
                )
                .border(
                    1.dp,
                    colors.emerald.copy(alpha = 0.5f),
                    RoundedCornerShape(22.dp)
                )
                .clickable(onClick = onClick)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.align(Alignment.BottomStart)
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.glass)
                    .padding(12.dp)
            ) {
                Text(
                    "FIELD 01",
                    color = colors.lime,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "12.5 hectares",
                    color = colors.textColor,
                    fontSize = 14.sp
                )
            }

            Column(
                modifier = Modifier.align(Alignment.TopEnd)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.cardBg2)
                    .padding(10.dp)
            ) {
                Text("NDVI", color = colors.mist, fontSize = 10.sp)
                Text("● Healthy", color = colors.lime, fontSize = 11.sp)
                Text("● Moderate", color = Color(0xFFFFCF55), fontSize = 11.sp)
                Text("● Low", color = Color(0xFFFF795C), fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun WeatherCard(colors: PremiumColors, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(colors.evergreen, colors.cardBg2)
                )
            )
            .border(
                1.dp,
                colors.textColor.copy(alpha = 0.12f),
                RoundedCornerShape(22.dp)
            )
            .clickable(onClick = onClick)
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("☀", fontSize = 32.sp)
        Spacer(Modifier.width(14.dp))

        Column(Modifier.weight(1f)) {
            Text(
                "Weather conditions",
                color = colors.textColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "Connect live weather data",
                color = colors.muted,
                fontSize = 12.sp
            )
        }

        Text(
            "↗",
            color = colors.lime,
            fontSize = 24.sp
        )
    }
}

@Composable
private fun SectionHeading(
    title: String,
    action: String,
    colors: PremiumColors,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            modifier = Modifier.weight(1f),
            color = colors.textColor,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            action,
            modifier = Modifier.clickable(onClick = onClick),
            color = colors.lime,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun InsightCard(
    modifier: Modifier = Modifier,
    icon: String,
    title: String,
    value: String,
    detail: String,
    accent: Color,
    colors: PremiumColors,
    onClick: () -> Unit
) {
    var selected by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (selected) 0.97f else 1f,
        animationSpec = tween(
            durationMillis = 160,
            easing = FastOutSlowInEasing
        ),
        label = "cardPressScale"
    )

    val borderColor by animateColorAsState(
        targetValue = if (selected) accent
        else colors.textColor.copy(alpha = 0.12f),
        animationSpec = tween(180),
        label = "cardBorder"
    )

    Column(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(21.dp))
            .background(
                Brush.linearGradient(
                    listOf(colors.cardBg, colors.cardBg2)
                )
            )
            .border(1.dp, borderColor, RoundedCornerShape(21.dp))
            .clickable {
                selected = !selected
                onClick()
            }
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Text(icon, color = accent, fontSize = 19.sp)
        }

        Spacer(Modifier.height(14.dp))

        Text(
            title,
            color = colors.mist,
            fontSize = 12.sp
        )
        Spacer(Modifier.height(5.dp))

        Text(
            value,
            color = colors.textColor,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            detail,
            color = accent,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun ActionCard(colors: PremiumColors, onClick: () -> Unit) {
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
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                "NEXT BEST ACTION",
                color = colors.lime,
                fontSize = 10.sp,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Your next farming decision starts here.",
                color = colors.textColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                "Automated Irrigation & Nutrient Advisory",
                color = colors.muted,
                fontSize = 11.sp
            )
        }

        Box(
            modifier = Modifier.size(42.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(listOf(colors.lime, colors.emerald))
                ),
            contentAlignment = Alignment.Center
        ) {
            Text("→", color = colors.pine, fontSize = 23.sp)
        }
    }
}
