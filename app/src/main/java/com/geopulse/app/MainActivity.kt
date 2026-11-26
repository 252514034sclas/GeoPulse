package com.geopulse.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import com.geopulse.app.network.ApiClient
import com.geopulse.app.network.UserSession
import com.geopulse.app.notifications.GeoPulseFirebaseMessagingService
import com.geopulse.app.ui.theme.ThemePreferences
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"

        // Explicit security allowlist for internal notification and deep-link destinations.
        // Replaces arbitrary dynamic reflection with strict compile-time class mapping.
        private val ALLOWED_DESTINATIONS: Map<String, Class<out ComponentActivity>> = mapOf(
            "Screen7Activity" to Screen7Activity::class.java,
            "Screen8Activity" to Screen8Activity::class.java,
            "Screen9Activity" to Screen9Activity::class.java,
            "Screen10Activity" to Screen10Activity::class.java,
            "Screen11Activity" to Screen11Activity::class.java,
            "Screen12Activity" to Screen12Activity::class.java,
            "Screen13Activity" to Screen13Activity::class.java,
            "Screen14Activity" to Screen14Activity::class.java,
            "Screen15Activity" to Screen15Activity::class.java,
            "Screen16Activity" to Screen16Activity::class.java,
            "Screen17Activity" to Screen17Activity::class.java,
            "Screen18Activity" to Screen18Activity::class.java,
            "Screen19Activity" to Screen19Activity::class.java,
            "Screen20Activity" to Screen20Activity::class.java,
            "Screen21Activity" to Screen21Activity::class.java,
            "Screen22Activity" to Screen22Activity::class.java,
            "Screen23Activity" to Screen23Activity::class.java,
            "Screen24Activity" to Screen24Activity::class.java,
            "Screen25Activity" to Screen25Activity::class.java,
            "Screen26Activity" to Screen26Activity::class.java,
            "Screen27Activity" to Screen27Activity::class.java,
            "Screen28Activity" to Screen28Activity::class.java,
            "Screen29Activity" to Screen29Activity::class.java,
            "Screen30Activity" to Screen30Activity::class.java,
            "WelcomeActivity" to WelcomeActivity::class.java,
            "LoginActivity" to LoginActivity::class.java,
            "SplashActivity" to SplashActivity::class.java
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemePreferences.init(this)
        ApiClient.initFromContext(this)
        UserSession.initSession(this)

        // Initialize Android notification channels
        GeoPulseFirebaseMessagingService.createNotificationChannels(this)

        // Retrieve and register FCM token if available
        try {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful && !task.result.isNullOrBlank()) {
                    Log.i(TAG, "FCM Device Token retrieved: ${task.result.take(20)}...")
                    GeoPulseFirebaseMessagingService.registerDeviceWithBackend(this, task.result)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firebase Messaging initialization: ${e.message}")
        }

        val target = intent?.getStringExtra("TARGET_SCREEN")
        if (!target.isNullOrEmpty()) {
            val destinationClass = ALLOWED_DESTINATIONS[target]
            if (destinationClass != null) {
                val isLoggedIn = UserSession.isLoggedIn(this)
                val isPublicScreen = target in listOf("WelcomeActivity", "LoginActivity", "SplashActivity")

                // Enforce authentication check: protected screens cannot be accessed anonymously
                if (isLoggedIn || isPublicScreen) {
                    val fieldId = intent?.getIntExtra("FIELD_ID", -1) ?: -1
                    if (fieldId > 0 && isLoggedIn) {
                        UserSession.setSelectedFieldId(this, fieldId)
                    }
                    startActivity(Intent(this, destinationClass).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtras(intent)
                    })
                    finish()
                    return
                } else {
                    Log.w(TAG, "Unauthorized deep-link routing attempt to $target blocked. Redirecting to LoginActivity.")
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                    return
                }
            } else {
                Log.w(TAG, "Rejecting unlisted target destination '$target' not in security allowlist.")
            }
        }

        startActivity(Intent(this, SplashActivity::class.java))
        finish()
    }
}
