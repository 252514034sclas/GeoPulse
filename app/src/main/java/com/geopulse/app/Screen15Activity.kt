package com.geopulse.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import com.geopulse.app.ui.theme.GeoPulseTheme

class Screen15Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeoPulseTheme {
                GeoDetailScreenContent(
                    title = "Pest Detection",
                    subtitle = "AI-powered early warning system for crop disease outbreaks and pest infestation.",
                    icon = Icons.Default.Warning,
                    activity = this,
                    nextActivityClass = Screen16Activity::class.java,
                    nextButtonText = "View Yield Analytics"
                )
            }
        }
    }
}
