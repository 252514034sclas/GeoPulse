package com.geopulse.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Memory
import com.geopulse.app.ui.theme.GeoPulseTheme

class Screen18Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeoPulseTheme {
                GeoDetailScreenContent(
                    title = "Sensor Node Status",
                    subtitle = "Battery health, signal strength, and firmware status of deployed IoT soil sensors.",
                    icon = Icons.Default.Memory,
                    activity = this,
                    nextActivityClass = Screen19Activity::class.java,
                    nextButtonText = "View Machinery Tracking"
                )
            }
        }
    }
}
