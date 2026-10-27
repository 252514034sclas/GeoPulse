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
                    title = "Market Commodity Prices",
                    subtitle = "Real-time grain futures, local elevator bids, and supply chain trends.",
                    icon = Icons.Default.ShowChart,
                    activity = this,
                    nextActivityClass = Screen28Activity::class.java,
                    nextButtonText = "View Notifications"
                )
            }
        }
    }
}
