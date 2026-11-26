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
                    subtitle = "Live Copernicus Sentinel-2 NDVI spectral greenness, canopy health, and active stress zones.",
                    icon = Icons.Default.Eco,
                    activity = this,
                    primaryActionClass = Screen24Activity::class.java,
                    primaryActionText = "View Historical Growth Trends",
                    secondaryActionClass = Screen12Activity::class.java,
                    secondaryActionText = "Check Soil Hydration Status"
                )
            }
        }
    }
}
