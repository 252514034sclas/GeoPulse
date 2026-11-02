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
                    title = "Farm Budget & Expense Tracker",
                    subtitle = "Track seeds, fertilizers, diesel, and labor expenses to understand net profit per acre.",
                    icon = Icons.Default.AttachMoney,
                    activity = this,
                    primaryActionClass = Screen27Activity::class.java,
                    primaryActionText = "View Mandi Wholesale Rates",
                    secondaryActionClass = Screen30Activity::class.java,
                    secondaryActionText = "Export Financial Records"
                )
            }
        }
    }
}
