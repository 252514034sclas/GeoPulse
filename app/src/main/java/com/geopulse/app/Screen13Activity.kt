package com.geopulse.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Opacity
import com.geopulse.app.ui.theme.GeoPulseTheme

class Screen13Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeoPulseTheme {
                GeoDetailScreenContent(
                    title = "Irrigation Control",
                    subtitle = "Automated pivot and drip irrigation schedules based on evapotranspiration data.",
                    icon = Icons.Default.Opacity,
                    activity = this,
                    nextActivityClass = Screen14Activity::class.java,
                    nextButtonText = "View Weather Forecast"
                )
            }
        }
    }
}
