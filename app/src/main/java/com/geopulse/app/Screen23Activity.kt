package com.geopulse.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Domain
import com.geopulse.app.ui.theme.GeoPulseTheme

class Screen23Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeoPulseTheme {
                GeoDetailScreenContent(
                    title = "Multi-Farm Portfolio",
                    subtitle = "Centralized oversight of multiple properties, cooperatives, and regional land parcels.",
                    icon = Icons.Default.Domain,
                    activity = this,
                    nextActivityClass = Screen24Activity::class.java,
                    nextButtonText = "View Drone Multispectral Scan"
                )
            }
        }
    }
}
