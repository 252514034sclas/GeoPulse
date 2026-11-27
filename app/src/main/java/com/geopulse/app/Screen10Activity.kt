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
import com.geopulse.app.util.GeoLanguageManager
import com.geopulse.app.util.GeoStrings

class Screen10Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GeoLanguageManager.init(this)
        setContent {
            GeoPulseTheme {
                var showLogoutDialog by remember { mutableStateOf(false) }

                Box(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
                    Scaffold(
                        topBar = {
                            GeoTopBar(
                                title = GeoStrings.screen10Title,
                                subtitle = GeoStrings.screen10Subtitle,
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
                                },
                                showHeader = false
                            )
                        }
                    }

                    if (showLogoutDialog) {
                        AlertDialog(
                            onDismissRequest = { showLogoutDialog = false },
                            title = { Text(GeoStrings.signOutConfirmTitle, fontWeight = FontWeight.Bold) },
                            text = { Text(GeoStrings.signOutConfirmText) },
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
                                    Text(GeoStrings.signOut, fontWeight = FontWeight.Bold)
                                }
                            },
                            dismissButton = {
                                OutlinedButton(onClick = { showLogoutDialog = false }) {
                                    Text(GeoStrings.cancel)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
