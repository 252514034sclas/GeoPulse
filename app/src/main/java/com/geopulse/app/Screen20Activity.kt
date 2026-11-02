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
                    subtitle = "Step-by-step guidance on Urea, DAP, and NPK dosage matching satellite crop vigor zones.",
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
