package com.geopulse.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import com.geopulse.app.ui.theme.GeoPulseTheme

class Screen19Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeoPulseTheme {
                GeoDetailScreenContent(
                    title = "Machinery Tracking",
                    subtitle = "Live telemetry, fuel consumption, and operational status of tractors and harvesters.",
                    icon = Icons.Default.Agriculture,
                    activity = this,
                    nextActivityClass = Screen20Activity::class.java,
                    nextButtonText = "View Fertilization Plan"
                )
            }
        }
    }
}
