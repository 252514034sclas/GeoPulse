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
                    title = "Field Boundary & Land Area",
                    subtitle = "Satellite GPS mapping of your farm parcels, boundaries, and estimated acreage.",
                    icon = Icons.Default.Map,
                    activity = this,
                    nextActivityClass = Screen23Activity::class.java,
                    nextButtonText = "View All Farm Parcels"
                )
            }
        }
    }
}
