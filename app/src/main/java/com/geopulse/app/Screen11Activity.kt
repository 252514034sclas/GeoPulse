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
                    title = "Vegetation Health",
                    subtitle = "Normalized Difference Vegetation Index (NDVI) monitoring for optimal crop vigor and chlorophyll content.",
                    icon = Icons.Default.Eco,
                    activity = this,
                    nextActivityClass = Screen12Activity::class.java,
                    nextButtonText = "View Soil Moisture"
                )
            }
        }
    }
}
