package com.geopulse.app.notifications

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.geopulse.app.Screen7Activity
import com.geopulse.app.network.ApiClient
import com.geopulse.app.network.NotificationDto
import com.geopulse.app.network.UserSession
import com.geopulse.app.ui.theme.EmeraldPrimary
import com.geopulse.app.ui.theme.LimeAccent
import com.geopulse.app.ui.theme.RedAccent
import com.geopulse.app.ui.theme.GoldAccent
import com.geopulse.app.ui.theme.CyanAccent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class GeoNotification(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: String,
    val isRead: Boolean = false,
    val priority: String = "MEDIUM",
    val category: String = "Advisory",
    val fieldId: Int? = null,
    val fieldName: String? = null,
    val actionNeeded: String? = null,
    val recommendedDatetime: String? = null,
    val inputQuantity: String? = null,
    val reason: String? = null,
    val confidence: String? = null
)

object NotificationRepository {
    private const val TAG = "NotificationRepo"
    private const val PREFS_NAME = "geopulse_notifications"
    private const val KEY_READ_IDS = "read_notification_ids"

    private val liveNotifications = mutableListOf<GeoNotification>()

    fun addNotification(notification: GeoNotification) {
        synchronized(liveNotifications) {
            val idx = liveNotifications.indexOfFirst { it.id == notification.id }
            if (idx >= 0) {
                liveNotifications[idx] = notification
            } else {
                liveNotifications.add(0, notification)
            }
        }
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getReadIds(context: Context): Set<String> {
        return getPrefs(context).getStringSet(KEY_READ_IDS, emptySet()) ?: emptySet()
    }

    fun getNotifications(context: Context): List<GeoNotification> {
        val readIds = getReadIds(context)
        synchronized(liveNotifications) {
            return liveNotifications.map { notif ->
                notif.copy(isRead = readIds.contains(notif.id) || notif.isRead)
            }
        }
    }

    suspend fun syncWithBackend(context: Context): List<GeoNotification> = withContext(Dispatchers.IO) {
        val authToken = UserSession.getToken(context)
        if (authToken.isNullOrEmpty()) {
            return@withContext getNotifications(context)
        }

        try {
            val response = ApiClient.service.getNotifications(limit = 50)
            if (response.isSuccessful && response.body() != null) {
                val dtoList: List<NotificationDto> = response.body()!!
                val lang = UserSession.getLanguage(context)

                synchronized(liveNotifications) {
                    val readIds = getReadIds(context)
                    for (dto in dtoList) {
                        val displayTitle = if (lang == "ta" && !dto.titleTa.isNullOrBlank()) dto.titleTa else dto.title
                        val displayMsg = if (lang == "ta" && !dto.messageTa.isNullOrBlank()) dto.messageTa else dto.message
                        val timeStr = dto.createdAt.take(16).replace("T", " ")

                        val mapped = GeoNotification(
                            id = dto.id.toString(),
                            title = displayTitle,
                            message = displayMsg,
                            timestamp = timeStr,
                            isRead = dto.isRead || readIds.contains(dto.id.toString()),
                            priority = dto.priority,
                            category = dto.category.toString(),
                            fieldId = dto.fieldId,
                            fieldName = dto.fieldName,
                            actionNeeded = dto.actionNeeded,
                            recommendedDatetime = dto.recommendedDatetime,
                            inputQuantity = dto.inputQuantity,
                            reason = dto.reason,
                            confidence = dto.confidence
                        )
                        val existingIndex = liveNotifications.indexOfFirst { it.id == mapped.id }
                        if (existingIndex >= 0) {
                            liveNotifications[existingIndex] = mapped
                        } else {
                            liveNotifications.add(mapped)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Backend notification sync failed (offline or network error): ${e.message}")
        }

        return@withContext getNotifications(context)
    }

    fun markAsRead(context: Context, id: String) {
        val readIds = getReadIds(context).toMutableSet()
        readIds.add(id)
        getPrefs(context).edit().putStringSet(KEY_READ_IDS, readIds).apply()

        synchronized(liveNotifications) {
            val idx = liveNotifications.indexOfFirst { it.id == id }
            if (idx >= 0) {
                liveNotifications[idx] = liveNotifications[idx].copy(isRead = true)
            }
        }

        // Asynchronously sync with backend
        val notifIdInt = id.toIntOrNull()
        if (notifIdInt != null && !UserSession.getToken(context).isNullOrEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    ApiClient.service.markNotificationRead(notifIdInt)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to sync markRead with backend: ${e.message}")
                }
            }
        }
    }

    fun markAllAsRead(context: Context) {
        val allIds = synchronized(liveNotifications) { liveNotifications.map { it.id }.toSet() }
        getPrefs(context).edit().putStringSet(KEY_READ_IDS, allIds).apply()

        synchronized(liveNotifications) {
            for (i in liveNotifications.indices) {
                liveNotifications[i] = liveNotifications[i].copy(isRead = true)
            }
        }

        // Asynchronously sync with backend
        if (!UserSession.getToken(context).isNullOrEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    ApiClient.service.markAllNotificationsRead()
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to sync markAllRead with backend: ${e.message}")
                }
            }
        }
    }

    fun getUnreadCount(context: Context): Int {
        val readIds = getReadIds(context)
        synchronized(liveNotifications) {
            return liveNotifications.count { !readIds.contains(it.id) && !it.isRead }
        }
    }
}

@Composable
fun GeoNotificationDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var notificationList by remember { mutableStateOf(NotificationRepository.getNotifications(context)) }
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Irrigation", "Weather", "CropHealth", "Spraying", "Agronomy", "Inspection")

    LaunchedEffect(Unit) {
        val synced = NotificationRepository.syncWithBackend(context)
        notificationList = synced
    }

    fun refreshList() {
        notificationList = NotificationRepository.getNotifications(context)
    }

    val filteredList = if (selectedCategory == "All") {
        notificationList
    } else {
        notificationList.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }

    val unreadCount = notificationList.count { !it.isRead }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.94f)
            .padding(vertical = 16.dp),
        title = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Farm Advisories",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (unreadCount > 0) {
                            Surface(
                                shape = CircleShape,
                                color = RedAccent,
                                modifier = Modifier.size(20.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = unreadCount.toString(),
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    if (unreadCount > 0) {
                        TextButton(
                            onClick = {
                                NotificationRepository.markAllAsRead(context)
                                refreshList()
                            },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Mark all read",
                                fontSize = 12.sp,
                                color = LimeAccent,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Category Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(categories) { cat ->
                        val isSelected = selectedCategory == cat
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { selectedCategory = cat }
                        ) {
                            Text(
                                text = cat,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (filteredList.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsNone,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(44.dp)
                            )
                            Text(
                                text = "All Farm Conditions Normal",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "No active advisories for this category. Field-specific recommendations will appear here as sensor & satellite models update.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(filteredList, key = { it.id }) { notif ->
                        NotificationCardItem(
                            notification = notif,
                            onMarkRead = {
                                NotificationRepository.markAsRead(context, notif.id)
                                refreshList()
                            },
                            onNavigateField = { fId ->
                                val intent = Intent(context, Screen7Activity::class.java).apply {
                                    putExtra("FIELD_ID", fId)
                                }
                                context.startActivity(intent)
                                onDismiss()
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Close", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    )
}

@Composable
fun NotificationCardItem(
    notification: GeoNotification,
    onMarkRead: () -> Unit,
    onNavigateField: (Int) -> Unit = {}
) {
    val isUnread = !notification.isRead
    val cardBg = if (isUnread) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
    }
    val borderColor = if (isUnread) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = cardBg,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header Row: Dot + Title + Category & Priority Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    if (isUnread) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(LimeAccent)
                        )
                    }

                    Text(
                        text = notification.title,
                        fontWeight = if (isUnread) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.5.sp,
                        color = if (isUnread) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Priority Pill
                    val priorityColor = when (notification.priority.uppercase()) {
                        "CRITICAL" -> RedAccent
                        "HIGH" -> GoldAccent
                        "MEDIUM" -> CyanAccent
                        else -> LimeAccent
                    }
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = priorityColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = notification.priority.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = priorityColor,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }

                    // Category Pill
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = when (notification.category) {
                            "Weather", "Spraying" -> CyanAccent.copy(alpha = 0.15f)
                            "Irrigation" -> EmeraldPrimary.copy(alpha = 0.15f)
                            "CropHealth" -> LimeAccent.copy(alpha = 0.15f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ) {
                        Text(
                            text = notification.category,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = when (notification.category) {
                                "Weather", "Spraying" -> CyanAccent
                                "Irrigation" -> EmeraldPrimary
                                "CropHealth" -> LimeAccent
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Body Message
            Text(
                text = notification.message,
                fontSize = 12.sp,
                color = if (isUnread) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                lineHeight = 16.sp
            )

            // Recommended Action & Quantity Pill (if present)
            if (!notification.actionNeeded.isNullOrBlank() || !notification.inputQuantity.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = EmeraldPrimary.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = listOfNotNull(notification.actionNeeded, notification.inputQuantity).joinToString(" · "),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Footer Row: Field Link + Timestamp + Mark as Read Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (notification.fieldId != null) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            modifier = Modifier.clickable { onNavigateField(notification.fieldId) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(11.dp)
                                )
                                Text(
                                    text = notification.fieldName ?: "Field #${notification.fieldId}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Text(
                        text = notification.timestamp,
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                if (isUnread) {
                    TextButton(
                        onClick = onMarkRead,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text(
                            text = "Mark read",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = "Read",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Read",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}
