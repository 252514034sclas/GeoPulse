package com.geopulse.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import com.geopulse.app.ui.theme.GeoPulseTheme

class Screen25Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeoPulseTheme {
                GeoDetailScreenContent(
                    title = "Organic Soil Health & Composting",
                    subtitle = "Improve long-term soil fertility naturally using compost, green manure, and mulching.",
                    icon = Icons.Default.Eco,
                    activity = this,
                    nextActivityClass = Screen20Activity::class.java,
                    nextButtonText = "View Fertilizer Recommendations"
                )
            }
        }
    }
}
