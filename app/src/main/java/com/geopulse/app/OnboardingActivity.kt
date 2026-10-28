package com.geopulse.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Water
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geopulse.app.ui.theme.*

data class OnboardingSlide(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val tag: String
)

class OnboardingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeoPulseTheme {
                val slides = remember {
                    listOf(
                        OnboardingSlide(
                            title = "Precision Satellite Mapping",
                            description = "Delineate high-precision field boundaries, visualize multispectral NDVI layers, and monitor crop health directly from orbit.",
                            icon = Icons.Default.Map,
                            tag = "STEP 1 OF 3"
                        ),
                        OnboardingSlide(
                            title = "Real-Time Soil & Root Telemetry",
                            description = "Keep root-zone moisture, soil temperature, and LoRaWAN mesh sensor clusters within optimal thresholds 24/7.",
                            icon = Icons.Default.Water,
                            tag = "STEP 2 OF 3"
                        ),
                        OnboardingSlide(
                            title = "AI Yield & Harvest Intelligence",
                            description = "Maximize harvest yields with machine-learning pest detection, variable-rate fertilizer planning, and automated pivot controls.",
                            icon = Icons.Default.AutoAwesome,
                            tag = "STEP 3 OF 3"
                        )
                    )
                }

                var currentSlideIndex by remember { mutableStateOf(0) }
                val currentSlide = slides[currentSlideIndex]

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .navigationBarsPadding()
                            .statusBarsPadding()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 500.dp)
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                        // Header with Skip action
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "GeoPulse",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 18.sp
                            )
                            TextButton(
                                onClick = {
                                    startActivity(Intent(this@OnboardingActivity, LoginActivity::class.java))
                                    finish()
                                }
                            ) {
                                Text(
                                    text = "Skip",
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Animated Content Card
                        AnimatedContent(
                            targetState = currentSlide,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "onboardingSlideAnimation"
                        ) { slide ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .glassSurface(cornerRadius = 24.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(28.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                        modifier = Modifier.size(80.dp),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = slide.icon,
                                                contentDescription = slide.title,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(44.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(20.dp))
                                    Text(
                                        text = slide.tag,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.2.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = slide.title,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = slide.description,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                        textAlign = TextAlign.Center,
                                        lineHeight = 21.sp
                                    )
                                }
                            }
                        }

                        // Bottom Actions: Dots & Next Button
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            // Page Indicator Dots
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                slides.indices.forEach { index ->
                                    val isSelected = index == currentSlideIndex
                                    Box(
                                        modifier = Modifier
                                            .size(
                                                width = if (isSelected) 24.dp else 8.dp,
                                                height = 8.dp
                                            )
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                                            )
                                    )
                                }
                            }

                            // Next / Get Started Button
                            Button(
                                onClick = {
                                    if (currentSlideIndex < slides.size - 1) {
                                        currentSlideIndex++
                                    } else {
                                        startActivity(Intent(this@OnboardingActivity, LoginActivity::class.java))
                                        finish()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text(
                                    text = if (currentSlideIndex == slides.size - 1) "Get Started" else "Next",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary
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
