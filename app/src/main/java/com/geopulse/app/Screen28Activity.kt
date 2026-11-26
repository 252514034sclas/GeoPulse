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
                    subtitle = "Active emergency alerts for thunderstorms, heatwaves, high winds, and heavy deluge. (Manage alert tones & quiet hours in Profile Settings).",
                    icon = Icons.Default.Warning,
                    activity = this,
                    primaryActionClass = Screen14Activity::class.java,
                    primaryActionText = "View 7-Day Weather & Spray Forecast",
                    secondaryActionClass = Screen10Activity::class.java,
                    secondaryActionText = "Manage Notification Preferences"
                )
            }
        }
    }
}
