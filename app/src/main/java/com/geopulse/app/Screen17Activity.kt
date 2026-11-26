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
                    title = "Field Boundary & GPS Polygon Survey",
                    subtitle = "Survey parcel GPS vertices, walk boundaries, and calculate parcel acreage.",
                    icon = Icons.Default.Map,
                    activity = this,
                    primaryActionClass = Screen7Activity::class.java,
                    primaryActionText = "Return to Farm Dashboard"
                )
            }
        }
    }
}
