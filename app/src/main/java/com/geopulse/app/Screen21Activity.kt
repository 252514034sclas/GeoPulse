package com.geopulse.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalShipping
import com.geopulse.app.ui.theme.GeoPulseTheme

class Screen21Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeoPulseTheme {
                GeoDetailScreenContent(
                    title = "Harvest Logistics",
                    subtitle = "Storage capacity planning, transport coordination, and delivery scheduling.",
                    icon = Icons.Default.LocalShipping,
                    activity = this,
                    nextActivityClass = Screen22Activity::class.java,
                    nextButtonText = "View Financial Summary"
                )
            }
        }
    }
}
