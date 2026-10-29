package com.geopulse.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory
import com.geopulse.app.ui.theme.GeoPulseTheme

class Screen21Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeoPulseTheme {
                GeoDetailScreenContent(
                    title = "Harvest Preparation & Storage Tips",
                    subtitle = "Practical post-harvest steps: drying methods, safe grain moisture, and pest-free storage.",
                    icon = Icons.Default.Inventory,
                    activity = this,
                    nextActivityClass = Screen27Activity::class.java,
                    nextButtonText = "View Mandi Selling Prices"
                )
            }
        }
    }
}
