package com.geopulse.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import com.geopulse.app.ui.theme.GeoPulseTheme

class Screen22Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeoPulseTheme {
                GeoDetailScreenContent(
                    title = "Financial Summary",
                    subtitle = "Input cost tracking, yield revenue estimation, and seasonal ROI analytics.",
                    icon = Icons.Default.AttachMoney,
                    activity = this,
                    nextActivityClass = Screen23Activity::class.java,
                    nextButtonText = "View Multi-Farm Portfolio"
                )
            }
        }
    }
}
