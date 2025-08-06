package com.onair.hearit.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.onair.hearit.R

class NotificationManager(
    private val context: Context,
) {
    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel =
            NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW,
            )
        notificationManager.createNotificationChannel(channel)
    }

    fun buildForegroundNotification() =
        NotificationCompat
            .Builder(context, CHANNEL_ID)
            .setContentTitle(context.getString(R.string.notification_title_preparing))
            .setContentText(context.getString(R.string.notification_text_preparing))
            .setSmallIcon(R.drawable.ic_mini_notification)
            .setOngoing(true)
            .build()

    companion object {
        private const val CHANNEL_ID = "hearit_channel"
    }
}
