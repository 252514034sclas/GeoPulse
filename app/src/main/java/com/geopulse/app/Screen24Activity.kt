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
                    primaryActionClass = Screen11Activity::class.java,
                    primaryActionText = "View Current Crop Vigor (NDVI)",
                    secondaryActionClass = Screen18Activity::class.java,
                    secondaryActionText = "View Crop Growth Calendar"
                )
            }
        }
    }
}
