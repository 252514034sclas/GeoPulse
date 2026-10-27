package com.geopulse.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingUp
import com.geopulse.app.ui.theme.GeoPulseTheme

class Screen16Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeoPulseTheme {
                GeoDetailScreenContent(
                    title = "Yield Analytics",
                    subtitle = "Predictive harvest modeling and historical productivity benchmarking.",
                    icon = Icons.Default.TrendingUp,
                    activity = this,
                    nextActivityClass = Screen17Activity::class.java,
                    nextButtonText = "View Field Boundaries"
                )
            }
        }
    }
}
