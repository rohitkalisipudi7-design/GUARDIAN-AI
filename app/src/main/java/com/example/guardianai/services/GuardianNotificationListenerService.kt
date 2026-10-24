package com.example.guardianai.services

import android.app.Notification
import android.content.Context
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.guardianai.pipeline.ListenService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object ListenerStatus {
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    private val _isUserToggleOn = MutableStateFlow(true)
    val isUserToggleOn: StateFlow<Boolean> = _isUserToggleOn

    fun init(context: Context) {
        val prefs = context.getSharedPreferences("guardian_listener_prefs", Context.MODE_PRIVATE)
        _isUserToggleOn.value = prefs.getBoolean("listener_user_toggle_on", true)
    }

    fun setConnected(status: Boolean) {
        _isConnected.value = status
    }

    fun setUserToggleOn(context: Context, status: Boolean) {
        _isUserToggleOn.value = status
        val prefs = context.getSharedPreferences("guardian_listener_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("listener_user_toggle_on", status).apply()
    }
}

/**
 * Real-Time Threat Interceptor
 * Intercepts incoming messages and notifications from WhatsApp, Instagram, Telegram, SMS, etc.
 * Feeds intercepted metadata directly into the 5-Stage Automated Threat Response Pipeline.
 */
class GuardianNotificationListenerService : NotificationListenerService() {

    companion object {
        private const val TAG = "GuardianListener"
        
        private val MONITORED_PLATFORMS = mapOf(
            "com.whatsapp" to "WhatsApp",
            "com.whatsapp.w4b" to "WhatsApp Business",
            "com.instagram.android" to "Instagram DM",
            "org.telegram.messenger" to "Telegram",
            "com.google.android.apps.messaging" to "Google SMS",
            "com.samsung.android.messaging" to "Samsung SMS",
            "com.facebook.orca" to "Messenger",
            "org.thoughtcrime.securesms" to "Signal",
            "com.snapchat.android" to "Snapchat"
        )
    }

    override fun onCreate() {
        super.onCreate()
        ListenerStatus.init(applicationContext)
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.i(TAG, "Guardian AI Real-Time Notification Listener Connected!")
        ListenerStatus.init(applicationContext)
        ListenerStatus.setConnected(true)
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.i(TAG, "Guardian AI Real-Time Notification Listener Disconnected!")
        ListenerStatus.setConnected(false)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        // Check if user turned OFF the notification listener in app options
        if (!ListenerStatus.isUserToggleOn.value) {
            return
        }

        val packageName = sbn.packageName
        val platformName = MONITORED_PLATFORMS[packageName] ?: return // Only process social & messaging platforms

        val notification = sbn.notification ?: return
        val extras = notification.extras ?: return

        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim() ?: "Unknown Sender"
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.trim() ?: ""

        // Ignore empty texts or summary / background notifications
        if (text.isEmpty() || text == "New messages" || text.contains("messages from") || title.isEmpty()) {
            return
        }

        // Check if communication happened during late night hours (11 PM - 5 AM)
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        val isLateNight = hour >= 23 || hour < 5

        Log.d(TAG, "Intercepted message from [$title] on [$platformName]: $text")

        // Dispatch directly into Guardian AI 5-Stage Pipeline in milliseconds
        ListenService.processIncomingEvent(
            rawSender = title,
            rawContent = text,
            platform = platformName,
            isLateNight = isLateNight,
            rejectionClarity = 0,
            frequencyCount = 1
        )
    }
}
