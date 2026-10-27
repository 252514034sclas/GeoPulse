package com.geopulse.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import com.geopulse.app.ui.theme.GeoPulseTheme

class Screen17Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeoPulseTheme {
                GeoDetailScreenContent(
                    title = "Field Boundary Editor",
                    subtitle = "Geospatial polygon editing and acreage calculation for precise field management.",
                    icon = Icons.Default.Map,
                    activity = this,
                    nextActivityClass = Screen18Activity::class.java,
                    nextButtonText = "View Sensor Status"
                )
            }
        }
    }
}
