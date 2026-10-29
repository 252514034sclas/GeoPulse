package com.geopulse.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShowChart
import com.geopulse.app.ui.theme.GeoPulseTheme

class Screen27Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeoPulseTheme {
                GeoDetailScreenContent(
                    title = "Local Mandi Market Rates",
                    subtitle = "Daily wholesale mandi prices for Wheat, Cotton, Paddy, Mustard, and Soybeans.",
                    icon = Icons.Default.ShowChart,
                    activity = this,
                    nextActivityClass = Screen22Activity::class.java,
                    nextButtonText = "View Farm Budget Tracker"
                )
            }
        }
    }
}
