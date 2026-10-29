package com.geopulse.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Opacity
import com.geopulse.app.ui.theme.GeoPulseTheme

class Screen13Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeoPulseTheme {
                GeoDetailScreenContent(
                    title = "Safe Watering Timing Guide",
                    subtitle = "Practical irrigation advice based on satellite moisture and rainfall forecast to avoid water waste.",
                    icon = Icons.Default.Opacity,
                    activity = this,
                    nextActivityClass = Screen14Activity::class.java,
                    nextButtonText = "View Rain & Spray Forecast"
                )
            }
        }
    }
}
