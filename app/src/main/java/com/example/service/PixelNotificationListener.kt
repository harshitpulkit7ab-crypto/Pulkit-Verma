package com.example.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.example.model.AppNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PixelNotificationListener : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        instance = this
        fetchActiveNotifications()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        instance = null
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        fetchActiveNotifications()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        fetchActiveNotifications()
    }

    private fun fetchActiveNotifications() {
        try {
            val active = activeNotifications ?: return
            val list = active.mapNotNull { sbn ->
                val extras = sbn.notification.extras
                val title = extras?.getCharSequence(android.app.Notification.EXTRA_TITLE)?.toString() ?: ""
                val text = extras?.getCharSequence(android.app.Notification.EXTRA_TEXT)?.toString() ?: ""
                if (title.isBlank() && text.isBlank()) return@mapNotNull null

                val pm = packageManager
                val appName = try {
                    val appInfo = pm.getApplicationInfo(sbn.packageName, 0)
                    pm.getApplicationLabel(appInfo).toString()
                } catch (e: Exception) {
                    sbn.packageName
                }

                AppNotification(
                    id = sbn.key,
                    packageName = sbn.packageName,
                    appName = appName,
                    title = title,
                    content = text,
                    postTime = sbn.postTime
                )
            }.take(15)

            _notificationsFlow.value = list
        } catch (e: Exception) {}
    }

    companion object {
        var instance: PixelNotificationListener? = null
            private set

        private val _notificationsFlow = MutableStateFlow<List<AppNotification>>(emptyList())
        val notificationsFlow: StateFlow<List<AppNotification>> = _notificationsFlow.asStateFlow()

        fun cancelNotification(key: String) {
            instance?.cancelNotification(key)
        }

        fun cancelAll() {
            instance?.cancelAllNotifications()
        }
    }
}
