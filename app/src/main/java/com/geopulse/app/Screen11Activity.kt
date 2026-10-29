package com.geopulse.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import com.geopulse.app.ui.theme.GeoPulseTheme

class Screen11Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeoPulseTheme {
                GeoDetailScreenContent(
                    title = "Satellite Crop Health & Vigor",
                    subtitle = "Satellite greenness (NDVI) monitoring in simple layman terms. Learn which parts of your field need care.",
                    icon = Icons.Default.Eco,
                    activity = this,
                    nextActivityClass = Screen20Activity::class.java,
                    nextButtonText = "View Fertilizer Recommendations"
                )
            }
        }
    }
}
