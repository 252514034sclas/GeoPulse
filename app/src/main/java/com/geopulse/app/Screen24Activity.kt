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
                    subtitle = "Review bi-weekly Sentinel-2 satellite crop greenness trajectories. (Soil chemical laboratory data is user-entered; no real-time lab probe).",
                    icon = Icons.Default.History,
                    activity = this,
                    primaryActionClass = Screen11Activity::class.java,
                    primaryActionText = "View Current Live Crop Vigor",
                    secondaryActionClass = Screen18Activity::class.java,
                    secondaryActionText = "View Crop Growth Calendar"
                )
            }
        }
    }
}
