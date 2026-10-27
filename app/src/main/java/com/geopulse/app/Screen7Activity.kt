package com.geopulse.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.geopulse.app.ui.premium.PremiumHomeScreen
import com.geopulse.app.ui.theme.GeoPulseTheme
import com.geopulse.app.ui.theme.ThemePreferences

class Screen7Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeoPulseTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    Scaffold(
                        containerColor = MaterialTheme.colorScheme.background
                    ) { padding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding)
                        ) {
                            PremiumHomeScreen(
                                isDarkTheme = ThemePreferences.isDarkTheme,
                                onFieldMapClick = {
                                    startActivity(Intent(this@Screen7Activity, Screen8Activity::class.java))
                                    finish()
                                },
                                onVegetationHealthClick = {
                                    startActivity(Intent(this@Screen7Activity, Screen11Activity::class.java))
                                    finish()
                                },
                                onSoilMoistureClick = {
                                    startActivity(Intent(this@Screen7Activity, Screen12Activity::class.java))
                                    finish()
                                },
                                onWeatherClick = {
                                    startActivity(Intent(this@Screen7Activity, Screen14Activity::class.java))
                                    finish()
                                },
                                onActionClick = {
                                    startActivity(Intent(this@Screen7Activity, Screen13Activity::class.java))
                                    finish()
                                },
                                onProfileClick = {
                                    startActivity(Intent(this@Screen7Activity, Screen10Activity::class.java))
                                    finish()
                                }
                            )
                        }
                    }

                    // Decoupled floating bottom navigation bar
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                    ) {
                        GeoBottomBar(currentScreen = 7, activity = this@Screen7Activity)
                    }
                }
            }
        }
    }
}
