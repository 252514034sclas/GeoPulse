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

class Screen9Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeoPulseTheme {
                Box(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
                    Scaffold(
                        topBar = {
                            GeoTopBar(
                                title = "Precision Agriculture Advisory",
                                subtitle = "Hyperlocal farm tasks, weather forecasts & spray timing",
                                onBackClick = { finish() }
                            )
                        },
                        containerColor = MaterialTheme.colorScheme.background
                    ) { padding ->
                        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                            FullAdvisoryTab(
                                onTaskClick = { targetClass ->
                                    startActivity(Intent(this@Screen9Activity, targetClass))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
