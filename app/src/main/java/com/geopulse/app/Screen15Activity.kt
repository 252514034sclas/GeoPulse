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
                    title = "Pest & Disease Advisory Guide",
                    subtitle = "Early seasonal risk warnings, biological controls, and practical prevention steps for common regional crop pests.",
                    icon = Icons.Default.Warning,
                    activity = this,
                    primaryActionClass = Screen11Activity::class.java,
                    primaryActionText = "View Satellite Crop Health (NDVI)",
                    secondaryActionClass = Screen26Activity::class.java,
                    secondaryActionText = "Consult Kisan Helpline (1800-180-1551)"
                )
            }
        }
    }
}
