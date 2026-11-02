package com.geopulse.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import com.geopulse.app.ui.theme.GeoPulseTheme

class Screen16Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeoPulseTheme {
                GeoDetailScreenContent(
                    title = "Expected Harvest & Yield Estimator",
                    subtitle = "Forecast harvest yield based on seasonal satellite growth patterns and historic weather.",
                    icon = Icons.Default.Assessment,
                    activity = this,
                    primaryActionClass = Screen21Activity::class.java,
                    primaryActionText = "View Harvest Storage Tips",
                    secondaryActionClass = Screen27Activity::class.java,
                    secondaryActionText = "Check Local Mandi Prices"
                )
            }
        }
    }
}
