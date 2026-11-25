package com.geopulse.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import com.geopulse.app.ui.theme.GeoPulseTheme

class Screen30Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeoPulseTheme {
                GeoDetailScreenContent(
                    title = "Farm Records & Data Export",
                    subtitle = "Safely export crop history, soil reports, and expense logs as PDF or Excel spreadsheets.",
                    icon = Icons.Default.Description,
                    activity = this,
                    primaryActionClass = Screen7Activity::class.java,
                    primaryActionText = "Return to Farm Dashboard",
                    secondaryActionClass = Screen29Activity::class.java,
                    secondaryActionText = "Manage App Settings & Sync"
                )
            }
        }
    }
}
