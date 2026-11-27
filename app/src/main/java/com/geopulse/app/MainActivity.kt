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
        val ALLOWED_DESTINATIONS: Map<String, Class<out ComponentActivity>> = mapOf(
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

        fun isDestinationAllowed(target: String?): Boolean {
            if (target.isNullOrBlank()) return false
            return ALLOWED_DESTINATIONS.containsKey(target)
        }

        fun isPublicDestination(target: String?): Boolean {
            return target in listOf("WelcomeActivity", "LoginActivity", "SplashActivity")
        }

        fun sanitizeIntentExtras(extras: Map<String, Any?>): Map<String, Any?> {
            val protectedKeys = setOf("auth_token", "token", "user_id", "email", "password", "api_key", "IS_LOGGED_IN")
            return extras.filterKeys { it !in protectedKeys }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemePreferences.init(this)
        com.geopulse.app.util.GeoLanguageManager.init(this)
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
            if (isDestinationAllowed(target)) {
                val destinationClass = ALLOWED_DESTINATIONS[target]!!
                val isLoggedIn = UserSession.isLoggedIn(this)
                val isPublicScreen = isPublicDestination(target)

                // Enforce authentication check: protected screens cannot be accessed anonymously
                if (isLoggedIn || isPublicScreen) {
                    val rawFieldId = intent?.getIntExtra("FIELD_ID", -1) ?: -1
                    if (rawFieldId > 0 && isLoggedIn) {
                        if (UserSession.isFieldOwned(this, rawFieldId)) {
                            UserSession.setSelectedFieldId(this, rawFieldId)
                        } else {
                            Log.w(TAG, "Security: Untrusted/unverified FIELD_ID $rawFieldId rejected. Not in verified owned fields. Preserving existing selection.")
                            intent?.removeExtra("FIELD_ID")
                        }
                    }

                    // Security: Sanitize intent extras to prevent untrusted callers from overriding authentication state
                    val sanitizedIntent = Intent(this, destinationClass).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtras(intent)
                        removeExtra("auth_token")
                        removeExtra("token")
                        removeExtra("user_id")
                        removeExtra("email")
                        removeExtra("password")
                        removeExtra("api_key")
                    }
                    startActivity(sanitizedIntent)
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
