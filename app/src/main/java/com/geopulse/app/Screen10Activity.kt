package com.geopulse.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geopulse.app.ui.theme.GeoPulseTheme

class Screen10Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeoPulseTheme {
                var showLogoutDialog by remember { mutableStateOf(false) }

                if (showLogoutDialog) {
                    AlertDialog(
                        onDismissRequest = { showLogoutDialog = false },
                        title = { Text("Sign Out", fontWeight = FontWeight.Bold, color = PrimaryDark) },
                        text = { Text("Are you sure you want to sign out of GeoPulse?", color = Color(0xFF404840)) },
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
                                Text("Cancel", color = AccentGreen)
                            }
                        },
                        containerColor = SurfaceCard
                    )
                }

                Scaffold(
                    topBar = { GeoTopBar(title = "User Profile") },
                    bottomBar = { GeoBottomBar(currentScreen = 10, activity = this@Screen10Activity) },
                    containerColor = LightBackground
                ) { padding ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Icon(Icons.Default.Person, contentDescription = "Profile", tint = AccentGreen, modifier = Modifier.size(80.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Farmer John", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = PrimaryDark)
                            Text("john.farmer@geopulse.com", fontSize = 14.sp, color = Color(0xFF606860))
                        }

                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Account Settings", fontWeight = FontWeight.Bold, color = PrimaryDark)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            startActivity(Intent(this@Screen10Activity, Screen30Activity::class.java))
                                            finish()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Text("Data Privacy & Account Deletion")
                                    }
                                }
                            }
                        }

                        item {
                            Button(
                                onClick = { showLogoutDialog = true },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryDark)
                            ) {
                                Text("Sign Out", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}
