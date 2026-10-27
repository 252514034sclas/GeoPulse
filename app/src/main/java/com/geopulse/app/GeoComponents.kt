package com.geopulse.app

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Water
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val PrimaryDark = Color(0xFF101510)
val AccentGreen = Color(0xFF2E7D32)
val LightBackground = Color(0xFFF8F9F5)
val SurfaceCard = Color(0xFFFFFFFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeoTopBar(
    title: String,
    onBackClick: (() -> Unit)? = null,
    onActionClick: (() -> Unit)? = null,
    actionIcon: ImageVector = Icons.Default.Notifications
) {
    TopAppBar(
        title = { Text(title, fontWeight = FontWeight.Bold, color = PrimaryDark) },
        navigationIcon = {
            if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = PrimaryDark)
                }
            }
        },
        actions = {
            if (onActionClick != null) {
                IconButton(onClick = onActionClick) {
                    Icon(actionIcon, contentDescription = "Action", tint = PrimaryDark)
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = LightBackground
        )
    )
}

@Composable
fun GeoBottomBar(currentScreen: Int, activity: ComponentActivity) {
    NavigationBar(
        containerColor = PrimaryDark,
        contentColor = AccentGreen
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home", color = if (currentScreen == 7) AccentGreen else Color.White) },
            selected = currentScreen == 7,
            onClick = {
                if (currentScreen != 7) {
                    activity.startActivity(Intent(activity, Screen7Activity::class.java))
                    activity.finish()
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AccentGreen,
                unselectedIconColor = Color.White.copy(alpha = 0.7f),
                indicatorColor = Color(0xFF1A261A)
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Place, contentDescription = "Map") },
            label = { Text("Map", color = if (currentScreen == 8) AccentGreen else Color.White) },
            selected = currentScreen == 8,
            onClick = {
                if (currentScreen != 8) {
                    activity.startActivity(Intent(activity, Screen8Activity::class.java))
                    activity.finish()
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AccentGreen,
                unselectedIconColor = Color.White.copy(alpha = 0.7f),
                indicatorColor = Color(0xFF1A261A)
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Assessment, contentDescription = "Activity") },
            label = { Text("Activity", color = if (currentScreen == 9) AccentGreen else Color.White) },
            selected = currentScreen == 9,
            onClick = {
                if (currentScreen != 9) {
                    activity.startActivity(Intent(activity, Screen9Activity::class.java))
                    activity.finish()
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AccentGreen,
                unselectedIconColor = Color.White.copy(alpha = 0.7f),
                indicatorColor = Color(0xFF1A261A)
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile", color = if (currentScreen == 10) AccentGreen else Color.White) },
            selected = currentScreen == 10,
            onClick = {
                if (currentScreen != 10) {
                    activity.startActivity(Intent(activity, Screen10Activity::class.java))
                    activity.finish()
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AccentGreen,
                unselectedIconColor = Color.White.copy(alpha = 0.7f),
                indicatorColor = Color(0xFF1A261A)
            )
        )
    }
}

@Composable
fun GeoDetailScreenContent(
    title: String,
    subtitle: String,
    icon: ImageVector,
    activity: ComponentActivity,
    nextActivityClass: Class<*>? = null,
    nextButtonText: String = "Proceed / Next"
) {
    Scaffold(
        topBar = {
            GeoTopBar(
                title = title,
                onBackClick = {
                    activity.startActivity(Intent(activity, Screen7Activity::class.java))
                    activity.finish()
                }
            )
        },
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
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = AccentGreen,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = title,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryDark,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = subtitle,
                            fontSize = 15.sp,
                            color = Color(0xFF505850),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Telemetry & Diagnostics", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PrimaryDark)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Status", color = Color(0xFF606860))
                            Text("Active & Synchronized", fontWeight = FontWeight.Medium, color = AccentGreen)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Last Update", color = Color(0xFF606860))
                            Text("Just Now", fontWeight = FontWeight.Medium, color = PrimaryDark)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        val target = nextActivityClass ?: Screen7Activity::class.java
                        activity.startActivity(Intent(activity, target))
                        activity.finish()
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                ) {
                    Text(nextButtonText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            item {
                OutlinedButton(
                    onClick = {
                        activity.startActivity(Intent(activity, Screen7Activity::class.java))
                        activity.finish()
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryDark)
                ) {
                    Text("Return to Dashboard", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
