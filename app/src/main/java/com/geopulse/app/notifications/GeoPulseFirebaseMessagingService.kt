package com.geopulse.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.geopulse.app.MainActivity
import com.geopulse.app.R
import com.geopulse.app.network.ApiClient
import com.geopulse.app.network.DeviceRegisterRequestDto
import com.geopulse.app.network.UserSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GeoPulseFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "GeoPulseFCM"
        const val PREFS_FCM = "geopulse_fcm_prefs"
        const val KEY_FCM_TOKEN = "fcm_device_token"

        const val CHANNEL_IRRIGATION = "geopulse_irrigation"
        const val CHANNEL_WEATHER = "geopulse_weather"
        const val CHANNEL_CROP_HEALTH = "geopulse_crop_health"
        const val CHANNEL_AGRONOMY = "geopulse_agronomy"

        fun getSavedToken(context: Context): String? {
            return context.getSharedPreferences(PREFS_FCM, Context.MODE_PRIVATE)
                .getString(KEY_FCM_TOKEN, null)
        }

        fun saveTokenLocally(context: Context, token: String) {
            context.getSharedPreferences(PREFS_FCM, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_FCM_TOKEN, token)
                .apply()
        }

        fun registerDeviceWithBackend(context: Context, token: String) {
            saveTokenLocally(context, token)
            val authToken = UserSession.getToken(context)
            if (authToken.isNullOrEmpty()) {
                Log.d(TAG, "User not authenticated yet. Deferred device token registration.")
                return
            }

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val api = ApiClient.service
                    val req = DeviceRegisterRequestDto(
                        fcmToken = token,
                        deviceType = "android",
                        deviceName = "${Build.MANUFACTURER} ${Build.MODEL}",
                        appVersion = "1.0.0"
                    )
                    val response = api.registerDevice(req)
                    if (response.isSuccessful) {
                        Log.i(TAG, "Device token registered with backend successfully: ${response.body()?.message}")
                    } else {
                        Log.w(TAG, "Device token registration returned error: ${response.code()} ${response.errorBody()?.string()}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to register device token with backend: ${e.message}")
                }
            }
        }

        fun createNotificationChannels(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                val irrigationChannel = NotificationChannel(
                    CHANNEL_IRRIGATION,
                    "Irrigation Advisories",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "FAO-56 Soil Moisture & Water Balance Irrigation Alerts"
                    enableVibration(true)
                }

                val weatherChannel = NotificationChannel(
                    CHANNEL_WEATHER,
                    "Weather & Spray Warnings",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Rain forecasts, high wind squalls, spray windows & severe weather warnings"
                    enableVibration(true)
                }

                val cropHealthChannel = NotificationChannel(
                    CHANNEL_CROP_HEALTH,
                    "Crop Health & Satellite Alerts",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Copernicus Sentinel-2 NDVI vigor alerts & canopy decline notifications"
                }

                val agronomyChannel = NotificationChannel(
                    CHANNEL_AGRONOMY,
                    "Farm Management & Tasks",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Phenological crop stage, split fertilizer & field inspection reminders"
                }

                notificationManager.createNotificationChannels(listOf(
                    irrigationChannel,
                    weatherChannel,
                    cropHealthChannel,
                    agronomyChannel
                ))
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels(this)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.i(TAG, "New FCM device registration token received: ${token.take(20)}...")
        registerDeviceWithBackend(applicationContext, token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.i(TAG, "FCM Push notification received from: ${remoteMessage.from}")

        val data = remoteMessage.data
        val title = remoteMessage.notification?.title
            ?: data["title"]
            ?: "GeoPulse Farm Advisory"

        val body = remoteMessage.notification?.body
            ?: data["message"]
            ?: "New agricultural field advisory received."

        val category = data["category"] ?: "Agronomy"
        val priority = data["priority"] ?: "HIGH"
        val fieldIdStr = data["field_id"]
        val fieldId = fieldIdStr?.toIntOrNull()
        val notifIdStr = data["notification_id"] ?: System.currentTimeMillis().toString()
        val notifId = notifIdStr.hashCode()

        val geoNotif = GeoNotification(
            id = notifIdStr,
            title = title,
            message = body,
            timestamp = "Just now",
            isRead = false,
            priority = priority,
            category = category,
            fieldId = fieldId,
            actionNeeded = data["action_needed"],
            inputQuantity = data["input_quantity"]
        )

        // Store notification in repository for in-app display
        NotificationRepository.addNotification(geoNotif)

        // Display system notification
        displaySystemNotification(
            notificationId = notifId,
            title = title,
            body = body,
            category = category,
            fieldId = fieldId,
            priority = priority
        )
    }

    private fun displaySystemNotification(
        notificationId: Int,
        title: String,
        body: String,
        category: String,
        fieldId: Int?,
        priority: String
    ) {
        val channelId = when (category) {
            "Irrigation" -> CHANNEL_IRRIGATION
            "Weather", "Spraying" -> CHANNEL_WEATHER
            "CropHealth" -> CHANNEL_CROP_HEALTH
            else -> CHANNEL_AGRONOMY
        }

        // Tap deep link intent
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("TARGET_SCREEN", if (fieldId != null) "Screen7Activity" else "Screen28Activity")
            putExtra("FIELD_ID", fieldId ?: -1)
            putExtra("NOTIFICATION_ID", notificationId)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setColor(0xFF2E7D32.toInt())
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(
                if (priority in listOf("HIGH", "CRITICAL")) NotificationCompat.PRIORITY_HIGH
                else NotificationCompat.PRIORITY_DEFAULT
            )

        try {
            NotificationManagerCompat.from(this).notify(notificationId, builder.build())
        } catch (se: SecurityException) {
            Log.w(TAG, "Notification permission not granted: ${se.message}")
        }
    }
}
