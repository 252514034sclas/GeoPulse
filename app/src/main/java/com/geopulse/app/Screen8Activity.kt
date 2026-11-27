package com.geopulse.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.geopulse.app.ui.theme.GeoPulseTheme
import com.geopulse.app.util.GeoLanguageManager
import com.geopulse.app.util.GeoStrings

class Screen8Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GeoLanguageManager.init(this)
        setContent {
            GeoPulseTheme {
                var isFullscreen by remember { mutableStateOf(false) }

                BackHandler(enabled = isFullscreen) {
                    isFullscreen = false
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(if (!isFullscreen) Modifier.statusBarsPadding() else Modifier)
                ) {
                    Scaffold(
                        topBar = {
                            if (!isFullscreen) {
                                GeoTopBar(
                                    title = GeoStrings.screen8Title,
                                    subtitle = GeoStrings.screen8Subtitle,
                                    onBackClick = { finish() }
                                )
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.background
                    ) { padding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(if (isFullscreen) PaddingValues(0.dp) else padding)
                        ) {
                            FullGisMapTab(
                                onVegetationClick = {
                                    startActivity(Intent(this@Screen8Activity, Screen11Activity::class.java))
                                },
                                onBoundaryClick = {
                                    startActivity(Intent(this@Screen8Activity, Screen17Activity::class.java))
                                },
                                onNavigate = { targetClass ->
                                    startActivity(Intent(this@Screen8Activity, targetClass))
                                },
                                showHeader = false,
                                isExternalFullscreen = isFullscreen,
                                onFullscreenChange = { isFullscreen = it }
                            )
                        }
                    }
                }
            }
        }
    }
}
