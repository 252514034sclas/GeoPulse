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
                    title = "Weather & Spraying Advisory",
                    subtitle = "Hyperlocal weather with safe spray windows (wind & rain advice) for effective chemical application.",
                    icon = Icons.Default.WbSunny,
                    activity = this,
                    nextActivityClass = Screen15Activity::class.java,
                    nextButtonText = "Check Pest & Disease Risks"
                )
            }
        }
    }
}
