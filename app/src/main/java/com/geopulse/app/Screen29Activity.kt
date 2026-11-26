package com.geopulse.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import com.geopulse.app.ui.theme.GeoPulseTheme

class Screen29Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeoPulseTheme {
                GeoDetailScreenContent(
                    title = "System Settings & Offline Cache",
                    subtitle = "Manage offline satellite cache, rural data saver mode, and advanced display preferences.",
                    icon = Icons.Default.Settings,
                    activity = this,
                    nextActivityClass = Screen7Activity::class.java,
                    nextButtonText = "Return to Farm Dashboard"
                )
            }
        }
    }
}
