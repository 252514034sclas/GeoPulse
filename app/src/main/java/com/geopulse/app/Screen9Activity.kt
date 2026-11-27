package com.geopulse.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.geopulse.app.ui.theme.GeoPulseTheme
import com.geopulse.app.util.GeoLanguageManager
import com.geopulse.app.util.GeoStrings

class Screen9Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GeoLanguageManager.init(this)
        setContent {
            GeoPulseTheme {
                Box(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
                    Scaffold(
                        topBar = {
                            GeoTopBar(
                                title = GeoStrings.screen9Title,
                                subtitle = GeoStrings.screen9Subtitle,
                                onBackClick = { finish() }
                            )
                        },
                        containerColor = MaterialTheme.colorScheme.background
                    ) { padding ->
                        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                            FullAdvisoryTab(
                                onTaskClick = { targetClass ->
                                    startActivity(Intent(this@Screen9Activity, targetClass))
                                },
                                showHeader = false
                            )
                        }
                    }
                }
            }
        }
    }
}
