package com.geopulse.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Science
import com.geopulse.app.ui.theme.GeoPulseTheme

class Screen20Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeoPulseTheme {
                GeoDetailScreenContent(
                    title = "Fertilization Plan",
                    subtitle = "Variable-rate nutrient application mapping to maximize ROI and minimize runoff.",
                    icon = Icons.Default.Science,
                    activity = this,
                    nextActivityClass = Screen21Activity::class.java,
                    nextButtonText = "View Harvest Logistics"
                )
            }
        }
    }
}
