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

class Screen8Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeoPulseTheme {
                Box(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
                    Scaffold(
                        topBar = {
                            GeoTopBar(
                                title = "Geospatial Field Map",
                                subtitle = "Sentinel-2 satellite observation & live GPS boundaries",
                                onBackClick = { finish() }
                            )
                        },
                        containerColor = MaterialTheme.colorScheme.background
                    ) { padding ->
                        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                            FullGisMapTab(
                                onVegetationClick = {
                                    startActivity(Intent(this@Screen8Activity, Screen11Activity::class.java))
                                },
                                onBoundaryClick = {
                                    startActivity(Intent(this@Screen8Activity, Screen17Activity::class.java))
                                },
                                onNavigate = { targetClass ->
                                    startActivity(Intent(this@Screen8Activity, targetClass))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
