package com.geopulse.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WbSunny
import com.geopulse.app.ui.theme.GeoPulseTheme

class Screen14Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeoPulseTheme {
                GeoDetailScreenContent(
                    title = "Weather & Radar",
                    subtitle = "Hyper-local meteorological forecasting, precipitation tracking, and frost warnings.",
                    icon = Icons.Default.WbSunny,
                    activity = this,
                    nextActivityClass = Screen15Activity::class.java,
                    nextButtonText = "View Pest Detection"
                )
            }
        }
    }
}
