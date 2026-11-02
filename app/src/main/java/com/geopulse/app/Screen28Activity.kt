package com.geopulse.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import com.geopulse.app.ui.theme.GeoPulseTheme

class Screen28Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeoPulseTheme {
                GeoDetailScreenContent(
                    title = "Weather & Crop Warning Center",
                    subtitle = "Immediate alerts for severe thunderstorms, heatwaves, frost, and unseasonal rainfall.",
                    icon = Icons.Default.Warning,
                    activity = this,
                    primaryActionClass = Screen14Activity::class.java,
                    primaryActionText = "View 7-Day Weather & Spray Forecast",
                    secondaryActionClass = Screen13Activity::class.java,
                    secondaryActionText = "Safe Watering Timing Guide"
                )
            }
        }
    }
}
