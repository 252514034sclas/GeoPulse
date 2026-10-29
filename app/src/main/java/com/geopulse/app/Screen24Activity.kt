package com.geopulse.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import com.geopulse.app.ui.theme.GeoPulseTheme

class Screen24Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeoPulseTheme {
                GeoDetailScreenContent(
                    title = "Satellite Growth History",
                    subtitle = "Review bi-weekly satellite crop greenness images to see how your crops improved over time.",
                    icon = Icons.Default.History,
                    activity = this,
                    nextActivityClass = Screen11Activity::class.java,
                    nextButtonText = "View Current Crop Vigor"
                )
            }
        }
    }
}
