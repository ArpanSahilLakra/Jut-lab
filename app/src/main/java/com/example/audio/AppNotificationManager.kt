package com.example.audio

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.AppPreferences
import kotlinx.coroutines.flow.first

class AppNotificationManager(private val context: Context, private val preferences: AppPreferences) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val generalChannel = NotificationChannel(
                CHANNEL_GENERAL,
                "General Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Standard lab updates and messages"
            }

            val learningChannel = NotificationChannel(
                CHANNEL_LEARNING,
                "Learning Reminders",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Study reminders and progress notifications"
            }

            notificationManager.createNotificationChannel(generalChannel)
            notificationManager.createNotificationChannel(learningChannel)
        }
    }

    suspend fun sendStudyReminder(title: String, message: String) {
        if (!preferences.notificationsEnabled.first()) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_LEARNING)
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(NOTIFICATION_ID_LEARNING, builder.build())
    }

    companion object {
        const val CHANNEL_GENERAL = "channel_general"
        const val CHANNEL_LEARNING = "channel_learning"
        const val NOTIFICATION_ID_LEARNING = 1001
    }
}
