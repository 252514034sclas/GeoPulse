package com.geopulse.app.notifications

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.geopulse.app.ui.theme.EmeraldPrimary
import com.geopulse.app.ui.theme.LimeAccent
import com.geopulse.app.ui.theme.RedAccent
import com.geopulse.app.ui.theme.GoldAccent
import com.geopulse.app.ui.theme.CyanAccent

data class GeoNotification(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: String,
    val isRead: Boolean = false,
    val priority: String = "MEDIUM",
    val category: String = "Advisory"
)

object NotificationRepository {
    private const val PREFS_NAME = "geopulse_notifications"
    private const val KEY_READ_IDS = "read_notification_ids"

    private val liveNotifications = mutableListOf<GeoNotification>()

    fun addNotification(notification: GeoNotification) {
        if (liveNotifications.none { it.id == notification.id }) {
            liveNotifications.add(0, notification)
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
        return liveNotifications.map { notif ->
            notif.copy(isRead = readIds.contains(notif.id))
        }
    }

    fun markAsRead(context: Context, id: String) {
        val readIds = getReadIds(context).toMutableSet()
        readIds.add(id)
        getPrefs(context).edit().putStringSet(KEY_READ_IDS, readIds).apply()
    }

    fun markAllAsRead(context: Context) {
        val allIds = liveNotifications.map { it.id }.toSet()
        getPrefs(context).edit().putStringSet(KEY_READ_IDS, allIds).apply()
    }

    fun getUnreadCount(context: Context): Int {
        val readIds = getReadIds(context)
        return liveNotifications.count { !readIds.contains(it.id) }
    }
}

@Composable
fun GeoNotificationDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var notificationList by remember { mutableStateOf(NotificationRepository.getNotifications(context)) }
    val unreadCount = notificationList.count { !it.isRead }

    fun refreshList() {
        notificationList = NotificationRepository.getNotifications(context)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .padding(vertical = 24.dp),
        title = {
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
                        text = "Notifications",
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
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (notificationList.isEmpty()) {
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
                                text = "No Notifications",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "You are all caught up! Real-time alerts will appear here when weather or crop events occur.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(notificationList, key = { it.id }) { notif ->
                        NotificationCardItem(
                            notification = notif,
                            onMarkRead = {
                                NotificationRepository.markAsRead(context, notif.id)
                                refreshList()
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
    onMarkRead: () -> Unit
) {
    val isUnread = !notification.isRead
    val cardBg = if (isUnread) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
    }
    val borderColor = if (isUnread) {
        LimeAccent.copy(alpha = 0.6f)
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = cardBg,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
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

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when (notification.category) {
                        "Weather" -> CyanAccent.copy(alpha = 0.15f)
                        "Agronomy" -> EmeraldPrimary.copy(alpha = 0.15f)
                        "Market" -> GoldAccent.copy(alpha = 0.15f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Text(
                        text = notification.category,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = when (notification.category) {
                            "Weather" -> CyanAccent
                            "Agronomy" -> LimeAccent
                            "Market" -> GoldAccent
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = notification.message,
                fontSize = 12.sp,
                color = if (isUnread) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = notification.timestamp,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )

                if (isUnread) {
                    TextButton(
                        onClick = onMarkRead,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text(
                            text = "Mark as read",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = LimeAccent
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
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}
