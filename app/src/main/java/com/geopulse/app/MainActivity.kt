package com.geopulse.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import com.geopulse.app.notifications.GeoPulseFirebaseMessagingService
import com.geopulse.app.ui.theme.ThemePreferences
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemePreferences.init(this)
        com.geopulse.app.network.ApiClient.initFromContext(this)
        com.geopulse.app.network.UserSession.initSession(this)

        // Initialize Android notification channels
        GeoPulseFirebaseMessagingService.createNotificationChannels(this)

        // Retrieve and register FCM token if available
        try {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful && !task.result.isNullOrBlank()) {
                    Log.i("MainActivity", "FCM Device Token retrieved: ${task.result.take(20)}...")
                    GeoPulseFirebaseMessagingService.registerDeviceWithBackend(this, task.result)
                }
            }
        } catch (e: Exception) {
            Log.w("MainActivity", "Firebase Messaging initialization: ${e.message}")
        }

        val target = intent?.getStringExtra("TARGET_SCREEN")
        if (!target.isNullOrEmpty()) {
            val fieldId = intent?.getIntExtra("FIELD_ID", -1) ?: -1
            if (fieldId > 0) {
                com.geopulse.app.network.UserSession.setSelectedFieldId(this, fieldId)
            }
            try {
                val clazz = Class.forName("com.geopulse.app.$target")
                startActivity(Intent(this, clazz).apply {
                    putExtras(intent)
                })
                finish()
                return
            } catch (_: Exception) {}
        }

        startActivity(Intent(this, SplashActivity::class.java))
        finish()
    }
}
