package com.geopulse.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Water
import com.geopulse.app.ui.theme.GeoPulseTheme

class Screen12Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeoPulseTheme {
                GeoDetailScreenContent(
                    title = "Satellite Soil Moisture Report",
                    subtitle = "Satellite-estimated moisture levels in your soil root-zone. Check if your field is dry or healthy.",
                    icon = Icons.Default.Water,
                    activity = this,
                    nextActivityClass = Screen13Activity::class.java,
                    nextButtonText = "View Safe Watering Schedule"
                )
            }
        }
    }
}
