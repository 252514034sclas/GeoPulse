package com.geopulse.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import com.geopulse.app.ui.theme.GeoPulseTheme

class Screen26Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeoPulseTheme {
                GeoDetailScreenContent(
                    title = "Agronomist Chat",
                    subtitle = "Direct messaging and AI-assisted consultation with certified crop advisors.",
                    icon = Icons.Default.Chat,
                    activity = this,
                    nextActivityClass = Screen27Activity::class.java,
                    nextButtonText = "View Market Prices"
                )
            }
        }
    }
}
