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
                    title = "Notification Center",
                    subtitle = "Comprehensive audit log of system alerts, weather advisories, and sensor triggers.",
                    icon = Icons.Default.Notifications,
                    activity = this,
                    nextActivityClass = Screen29Activity::class.java,
                    nextButtonText = "View App Settings"
                )
            }
        }
    }
}
