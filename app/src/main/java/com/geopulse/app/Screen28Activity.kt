package com.geopulse.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import com.geopulse.app.ui.theme.GeoPulseTheme

class Screen28Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeoPulseTheme {
                GeoDetailScreenContent(
                    title = "Weather & Crop Warning Center",
                    subtitle = "Immediate alerts for severe thunderstorms, heatwaves, frost, and unseasonal rainfall.",
                    icon = Icons.Default.Notifications,
                    activity = this,
                    nextActivityClass = Screen14Activity::class.java,
                    nextButtonText = "View 7-Day Weather Forecast"
                )
            }
        }
    }
}
