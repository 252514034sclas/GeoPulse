package com.geopulse.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Water
import com.geopulse.app.ui.theme.GeoPulseTheme

class Screen13Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeoPulseTheme {
                GeoDetailScreenContent(
                    title = "Safe Watering Timing Guide",
                    subtitle = "Practical irrigation advice based on satellite moisture and rainfall forecast to avoid water waste.",
                    icon = Icons.Default.Water,
                    activity = this,
                    primaryActionClass = Screen14Activity::class.java,
                    primaryActionText = "View Rain & Spray Forecast",
                    secondaryActionClass = Screen12Activity::class.java,
                    secondaryActionText = "Check Soil Moisture Report"
                )
            }
        }
    }
}
