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
                    title = "App Settings & Sync",
                    subtitle = "Offline data synchronization preferences, cloud backup, and security configurations.",
                    icon = Icons.Default.Settings,
                    activity = this,
                    nextActivityClass = Screen30Activity::class.java,
                    nextButtonText = "View Privacy & Deletion"
                )
            }
        }
    }
}
