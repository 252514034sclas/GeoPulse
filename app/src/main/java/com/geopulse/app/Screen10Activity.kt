package com.geopulse.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.geopulse.app.ui.theme.GeoPulseTheme
import com.geopulse.app.ui.theme.ThemePreferences

class Screen10Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeoPulseTheme {
                var showLogoutDialog by remember { mutableStateOf(false) }

                Box(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
                    Scaffold(
                        topBar = {
                            GeoTopBar(
                                title = "Farm Profile & Settings",
                                subtitle = "Farmer credentials, app appearance & farm parcels",
                                onBackClick = { finish() }
                            )
                        },
                        containerColor = MaterialTheme.colorScheme.background
                    ) { padding ->
                        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                            FullProfileSettingsTab(
                                currentThemeMode = ThemePreferences.themeMode,
                                onThemeModeSelected = { mode ->
                                    ThemePreferences.updateThemeMode(this@Screen10Activity, mode)
                                },
                                onNavigate = { targetClass ->
                                    startActivity(Intent(this@Screen10Activity, targetClass))
                                },
                                onSignOut = {
                                    showLogoutDialog = true
                                }
                            )
                        }
                    }

                    if (showLogoutDialog) {
                        AlertDialog(
                            onDismissRequest = { showLogoutDialog = false },
                            title = { Text("Sign Out of GeoPulse?", fontWeight = FontWeight.Bold) },
                            text = { Text("You will need your registered credentials to access your satellite farm telemetry.") },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        showLogoutDialog = false
                                        startActivity(Intent(this@Screen10Activity, LoginActivity::class.java).apply {
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        })
                                        finish()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("Sign Out", fontWeight = FontWeight.Bold)
                                }
                            },
                            dismissButton = {
                                OutlinedButton(onClick = { showLogoutDialog = false }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
