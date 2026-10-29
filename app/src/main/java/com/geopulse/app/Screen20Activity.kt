package com.geopulse.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Science
import com.geopulse.app.ui.theme.GeoPulseTheme

class Screen20Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeoPulseTheme {
                GeoDetailScreenContent(
                    title = "Fertilizer & Nutrient Guide",
                    subtitle = "Step-by-step guidance on Urea, DAP, and NPK dosage matching satellite crop vigor zones.",
                    icon = Icons.Default.Science,
                    activity = this,
                    nextActivityClass = Screen12Activity::class.java,
                    nextButtonText = "Check Field Moisture Status"
                )
            }
        }
    }
}
