package com.geopulse.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flight
import com.geopulse.app.ui.theme.GeoPulseTheme

class Screen24Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeoPulseTheme {
                GeoDetailScreenContent(
                    title = "Drone Multispectral Scan",
                    subtitle = "High-resolution aerial imagery and thermal scan processing for stress detection.",
                    icon = Icons.Default.Flight,
                    activity = this,
                    nextActivityClass = Screen25Activity::class.java,
                    nextButtonText = "View Carbon Sustainability"
                )
            }
        }
    }
}
