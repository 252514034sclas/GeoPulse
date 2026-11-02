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
                    title = "My Land Parcels & Crops",
                    subtitle = "List and manage all your registered land plots, survey numbers, and active crops.",
                    icon = Icons.Default.Domain,
                    activity = this,
                    primaryActionClass = Screen17Activity::class.java,
                    primaryActionText = "Edit Field Boundaries & Area",
                    secondaryActionClass = Screen8Activity::class.java,
                    secondaryActionText = "Open Interactive GIS Map"
                )
            }
        }
    }
}
