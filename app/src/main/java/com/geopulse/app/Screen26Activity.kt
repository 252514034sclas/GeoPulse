package com.geopulse.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import com.geopulse.app.ui.theme.GeoPulseTheme

class Screen26Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeoPulseTheme {
                GeoDetailScreenContent(
                    title = "Kisan Helpline & Agronomist Chat",
                    subtitle = "Direct connection to Kisan Call Center (1800-180-1551) and certified agriculture experts.",
                    icon = Icons.Default.Phone,
                    activity = this,
                    nextActivityClass = Screen15Activity::class.java,
                    nextButtonText = "Check Pest Warnings"
                )
            }
        }
    }
}
