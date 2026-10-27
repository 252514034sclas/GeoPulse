package com.geopulse.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geopulse.app.ui.theme.*

class Screen10Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeoPulseTheme {
                var showLogoutDialog by remember { mutableStateOf(false) }
                var isDarkTheme by remember { mutableStateOf(ThemePreferences.isDarkTheme) }
                var pushNotificationsEnabled by remember { mutableStateOf(true) }
                var offlineCachingEnabled by remember { mutableStateOf(true) }

                if (showLogoutDialog) {
                    AlertDialog(
                        onDismissRequest = { showLogoutDialog = false },
                        title = { Text("Sign Out of GeoPulse?", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
                        text = { Text("You will need to sign in again to access live telemetry and farm diagnostics.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)) },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showLogoutDialog = false
                                    startActivity(Intent(this@Screen10Activity, LoginActivity::class.java))
                                    finish()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Sign Out")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showLogoutDialog = false }) {
                                Text("Cancel", color = MaterialTheme.colorScheme.primary)
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    Scaffold(
                        topBar = {
                            GeoTopBar(
                                title = "Profile & Settings",
                                onBackClick = {
                                    startActivity(Intent(this@Screen10Activity, Screen7Activity::class.java))
                                    finish()
                                }
                            )
                        },
                        containerColor = MaterialTheme.colorScheme.background
                    ) { padding ->
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding),
                            contentPadding = PaddingValues(bottom = 90.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Profile Header Card
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(
                                            Brush.linearGradient(
                                                listOf(EvergreenDark, ForestDark, PineDark)
                                            )
                                        )
                                        .padding(20.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(68.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    Brush.linearGradient(listOf(LimeAccent, EmeraldPrimary))
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("J", color = PineDark, fontSize = 32.sp, fontWeight = FontWeight.Black)
                                        }

                                        Column {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text("Johnathan Doe", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                Icon(Icons.Default.Verified, contentDescription = "Verified", tint = LimeAccent, modifier = Modifier.size(18.dp))
                                            }
                                            Text("Green Valley Farm · Lead Agronomist", fontSize = 13.sp, color = MistGreen.copy(alpha = 0.85f))
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("1,250 Acres · Pro Tier Enterprise", fontSize = 11.sp, color = LimeAccent, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }

                            // Theme & System Preferences Card
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .glassSurface(cornerRadius = 20.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                                ) {
                                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Text("Display & Sync Preferences", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)

                                        // Dark Mode Switch
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text("Dark Mode Theme", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                                Text("Deep emerald dark aesthetic", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                            }
                                            Switch(
                                                checked = isDarkTheme,
                                                onCheckedChange = {
                                                    isDarkTheme = it
                                                    ThemePreferences.isDarkTheme = it
                                                }
                                            )
                                        }

                                        HorizontalDivider()

                                        // Push Notifications
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text("Telemetry & Frost Advisories", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                                Text("Instant push alerts for moisture deficits", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                            }
                                            Switch(
                                                checked = pushNotificationsEnabled,
                                                onCheckedChange = { pushNotificationsEnabled = it }
                                            )
                                        }

                                        HorizontalDivider()

                                        // Offline Cache
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text("Offline GIS Map Caching", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                                Text("Cache satellite raster tiles locally", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                            }
                                            Switch(
                                                checked = offlineCachingEnabled,
                                                onCheckedChange = { offlineCachingEnabled = it }
                                            )
                                        }
                                    }
                                }
                            }

                            // Quick Navigation Hub Card
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .glassSurface(cornerRadius = 20.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                                ) {
                                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("Farm Operations & Intelligence", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                                        Spacer(modifier = Modifier.height(4.dp))

                                        ProfileNavRow(icon = Icons.Default.Landscape, title = "Multi-Farm Portfolio", subtitle = "Switch properties & regional parcels") {
                                            startActivity(Intent(this@Screen10Activity, Screen23Activity::class.java))
                                            finish()
                                        }
                                        ProfileNavRow(icon = Icons.Default.MonetizationOn, title = "Financial Summary & ROI", subtitle = "Seasonal cost vs yield revenue") {
                                            startActivity(Intent(this@Screen10Activity, Screen22Activity::class.java))
                                            finish()
                                        }
                                        ProfileNavRow(icon = Icons.Default.TrendingUp, title = "Market Commodity Futures", subtitle = "Live Corn, Soy & Wheat ticker") {
                                            startActivity(Intent(this@Screen10Activity, Screen27Activity::class.java))
                                            finish()
                                        }
                                        ProfileNavRow(icon = Icons.Default.Chat, title = "Agronomist Direct Consultation", subtitle = "Chat with Dr. Elena Vance") {
                                            startActivity(Intent(this@Screen10Activity, Screen26Activity::class.java))
                                            finish()
                                        }
                                        ProfileNavRow(icon = Icons.Default.Settings, title = "App Settings & Sync Configuration", subtitle = "API keys & sync frequency") {
                                            startActivity(Intent(this@Screen10Activity, Screen29Activity::class.java))
                                            finish()
                                        }
                                        ProfileNavRow(icon = Icons.Default.Security, title = "Data Privacy & Account Deletion", subtitle = "Export GeoJSON & GDPR options") {
                                            startActivity(Intent(this@Screen10Activity, Screen30Activity::class.java))
                                            finish()
                                        }
                                    }
                                }
                            }

                            // Sign Out Button
                            item {
                                Button(
                                    onClick = { showLogoutDialog = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .height(52.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Icon(Icons.Default.ExitToApp, contentDescription = null, tint = MaterialTheme.colorScheme.onError)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Sign Out of Account", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onError)
                                }
                            }
                        }
                    }

                    // Decoupled floating bottom navigation bar
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                    ) {
                        GeoBottomBar(currentScreen = 10, activity = this@Screen10Activity)
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileNavRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(text = subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            modifier = Modifier.size(20.dp)
        )
    }
}
