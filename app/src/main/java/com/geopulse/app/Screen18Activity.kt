package com.geopulse.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import com.geopulse.app.ui.theme.GeoPulseTheme

class Screen18Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeoPulseTheme {
                GeoDetailScreenContent(
                    title = "Crop Calendar & Growth Stages",
                    subtitle = "Simple timetable showing current crop stage (Tillering / Flowering) and recommended care.",
                    icon = Icons.Default.DateRange,
                    activity = this,
                    nextActivityClass = Screen12Activity::class.java,
                    nextButtonText = "Check Soil Moisture Status"
                )
            }
        }
    }
}
