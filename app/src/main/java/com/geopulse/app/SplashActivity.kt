package com.geopulse.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.geopulse.app.ui.theme.GeoPulseTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            delay(1800.milliseconds)
            if (!isFinishing && !isDestroyed) {
                startActivity(Intent(this@SplashActivity, WelcomeActivity::class.java))
                finish()
            }
        }
        setContent {
            GeoPulseTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = PrimaryDark
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Eco,
                                contentDescription = "GeoPulse Logo",
                                tint = AccentGreen,
                                modifier = Modifier.size(80.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "GeoPulse",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Agricultural & Geospatial Intelligence",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}
