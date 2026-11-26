package com.geopulse.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Spa
import com.geopulse.app.ui.theme.GeoPulseTheme

class Screen20Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeoPulseTheme {
                GeoDetailScreenContent(
                    title = "Fertilizer & Nutrient Guide",
                    subtitle = "Crop-specific guideline dosage estimates based on optical Sentinel-2 vigor zones. (Drone imagery unavailable; optical NDVI measures canopy greenness, not soil nutrients. Adjust with Soil Health Card laboratory tests).",
                    icon = Icons.Default.Spa,
                    activity = this,
                    primaryActionClass = Screen12Activity::class.java,
                    primaryActionText = "Check Field Moisture Status",
                    secondaryActionClass = Screen25Activity::class.java,
                    secondaryActionText = "View Organic Soil Health & Composting"
                )
            }
        }
    }
}
