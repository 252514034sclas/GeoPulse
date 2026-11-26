package com.geopulse.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import com.geopulse.app.ui.theme.GeoPulseTheme

class Screen24Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeoPulseTheme {
                GeoDetailScreenContent(
                    title = "Satellite Growth History & Trends",
                    subtitle = "Review bi-weekly Sentinel-2 optical satellite crop greenness trajectories. (Optical NDVI measures canopy vigor, not soil nutrients. Soil chemical laboratory data is user-entered from Soil Health Card).",
                    icon = Icons.Default.History,
                    activity = this,
                    primaryActionClass = Screen18Activity::class.java,
                    primaryActionText = "View Crop Growth Calendar",
                    secondaryActionClass = Screen7Activity::class.java,
                    secondaryActionText = "Return to Farm Dashboard"
                )
            }
        }
    }
}
