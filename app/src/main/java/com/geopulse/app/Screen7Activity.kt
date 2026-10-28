package com.geopulse.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geopulse.app.ui.premium.*
import com.geopulse.app.ui.theme.*
import kotlinx.coroutines.launch

class Screen7Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeoPulseTheme {
                val pagerState = rememberPagerState(pageCount = { 4 })
                val coroutineScope = rememberCoroutineScope()
                var showLogoutDialog by remember { mutableStateOf(false) }
                var isDark by remember { mutableStateOf(ThemePreferences.isDarkTheme) }

                if (showLogoutDialog) {
                    AlertDialog(
                        onDismissRequest = { showLogoutDialog = false },
                        title = { Text("Sign Out", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
                        text = { Text("Are you sure you want to sign out of GeoPulse?", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)) },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showLogoutDialog = false
                                    startActivity(Intent(this@Screen7Activity, LoginActivity::class.java))
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

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    // Horizontal Pager with explicit userScrollEnabled = true for smooth drag sliding
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        userScrollEnabled = true
                    ) { page ->
                        when (page) {
                            0 -> {
                                PremiumHomeScreen(
                                    isDarkTheme = isDark,
                                    onFieldMapClick = {
                                        coroutineScope.launch { pagerState.scrollToPage(1) }
                                    },
                                    onVegetationHealthClick = {
                                        startActivity(Intent(this@Screen7Activity, Screen11Activity::class.java))
                                    },
                                    onSoilMoistureClick = {
                                        startActivity(Intent(this@Screen7Activity, Screen12Activity::class.java))
                                    },
                                    onWeatherClick = {
                                        startActivity(Intent(this@Screen7Activity, Screen13Activity::class.java))
                                    },
                                    onActionClick = {
                                        startActivity(Intent(this@Screen7Activity, Screen19Activity::class.java))
                                    },
                                    onProfileClick = {
                                        coroutineScope.launch { pagerState.scrollToPage(3) }
                                    }
                                )
                            }
                            1 -> {
                                MapScreenContent(
                                    onInspectAlerts = {
                                        startActivity(Intent(this@Screen7Activity, Screen15Activity::class.java))
                                    }
                                )
                            }
                            2 -> {
                                ActivityScreenContent(
                                    onViewReports = {
                                        startActivity(Intent(this@Screen7Activity, Screen11Activity::class.java))
                                    }
                                )
                            }
                            3 -> {
                                ProfileScreenContent(
                                    isDark = isDark,
                                    onThemeToggle = {
                                        isDark = it
                                        ThemePreferences.isDarkTheme = it
                                    },
                                    onDeleteAccount = {
                                        startActivity(Intent(this@Screen7Activity, Screen30Activity::class.java))
                                    },
                                    onSignOut = {
                                        showLogoutDialog = true
                                    }
                                )
                            }
                        }
                    }

                    // Finger-Dragged Bottom Navigation Dock Overlay (No Tapping - Physically drag and pull)
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                    ) {
                        DraggableBottomDock(pagerState = pagerState, coroutineScope = coroutineScope)
                    }
                }
            }
        }
    }
}

@Composable
fun MapScreenContent(onInspectAlerts: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(EmeraldPrimary.copy(alpha = 0.25f), DarkBackground),
                    radius = 900f
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                GeoTopBar(title = "Geospatial Map")
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().height(220.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, CardBorderColor)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50.dp),
                            color = EmeraldPrimary.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Place, contentDescription = "Map View", tint = LimeAccent, modifier = Modifier.size(32.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Interactive Farm Boundary Map", color = TextLight, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Lat: 37.7749° N, Lon: 122.4194° W", color = TextMuted, fontSize = 13.sp)
                    }
                }
            }
            item {
                Text("Active Farm Zones", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextLight)
            }
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassSurface(cornerRadius = 18.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("North Sector - Corn & Soybeans", fontWeight = FontWeight.Bold, color = TextLight)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Size: 120 Acres | Status: Monitored", fontSize = 13.sp, color = TextMuted)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onInspectAlerts,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = LeafGreen)
                        ) {
                            Text("Inspect Zone Alerts", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(90.dp)) }
        }
    }
}

@Composable
fun ActivityScreenContent(onViewReports: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(EmeraldPrimary.copy(alpha = 0.25f), DarkBackground),
                    radius = 900f
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                GeoTopBar(title = "Activity & Telemetry")
            }
            item {
                Text("Recent Telemetry Logs", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextLight)
            }
            items(5) { index ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassSurface(cornerRadius = 16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Sensor Cluster #${index + 1}", fontWeight = FontWeight.Bold, color = TextLight)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Soil Moisture optimal at 74%", fontSize = 13.sp, color = TextMuted)
                        }
                        Text("2h ago", fontSize = 12.sp, color = LimeAccent, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onViewReports,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LeafGreen)
                ) {
                    Text("View Detailed Reports", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
            item { Spacer(modifier = Modifier.height(90.dp)) }
        }
    }
}

@Composable
fun ProfileScreenContent(
    isDark: Boolean,
    onThemeToggle: (Boolean) -> Unit,
    onDeleteAccount: () -> Unit,
    onSignOut: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(EmeraldPrimary.copy(alpha = 0.25f), DarkBackground),
                    radius = 900f
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                GeoTopBar(title = "User Profile & Settings")
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Icon(Icons.Default.Person, contentDescription = "Profile", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(80.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Farmer John", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onBackground)
                Text("john.farmer@geopulse.com", fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassSurface(cornerRadius = 16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Dark Mode Theme", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text("Toggle between Dark & Light themes", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                        Switch(
                            checked = isDark,
                            onCheckedChange = onThemeToggle,
                            colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassSurface(cornerRadius = 16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Account Settings", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onDeleteAccount,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Data Privacy & Account Deletion")
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = onSignOut,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onBackground)
                ) {
                    Text("Sign Out", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.background)
                }
            }
            item { Spacer(modifier = Modifier.height(90.dp)) }
        }
    }
}
