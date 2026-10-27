package com.geopulse.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Water
import com.geopulse.app.ui.theme.GeoPulseTheme

class Screen12Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeoPulseTheme {
                GeoDetailScreenContent(
                    title = "Soil Moisture Report",
                    subtitle = "Real-time root-zone moisture tracking across all farm quadrants.",
                    icon = Icons.Default.Water,
                    activity = this,
                    nextActivityClass = Screen13Activity::class.java,
                    nextButtonText = "View Irrigation Schedule"
                )
            }
        }
    }
}
