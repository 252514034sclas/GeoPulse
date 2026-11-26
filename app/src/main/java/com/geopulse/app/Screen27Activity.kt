package com.geopulse.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import com.geopulse.app.ui.theme.GeoPulseTheme

class Screen27Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeoPulseTheme {
                GeoDetailScreenContent(
                    title = "Local Mandi Market Rates",
                    subtitle = "Wholesale APMC modal market rates from AGMARKNET bulletin. (Subject to local quality grading; not futures exchange prices).",
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    activity = this,
                    primaryActionClass = Screen21Activity::class.java,
                    primaryActionText = "Harvest Preparation & Storage",
                    secondaryActionClass = Screen7Activity::class.java,
                    secondaryActionText = "Return to Farm Dashboard"
                )
            }
        }
    }
}
