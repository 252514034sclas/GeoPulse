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
                    title = "Field Operations & Plowing Planner",
                    subtitle = "Optimal days for plowing, sowing, weeding, and harrowing according to soil dryness.",
                    icon = Icons.Default.Agriculture,
                    activity = this,
                    nextActivityClass = Screen14Activity::class.java,
                    nextButtonText = "Check Weather Forecast"
                )
            }
        }
    }
}
