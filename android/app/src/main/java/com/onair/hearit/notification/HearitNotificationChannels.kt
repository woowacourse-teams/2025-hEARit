package com.onair.hearit.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.onair.hearit.R

object HearitNotificationChannels {
    const val PLAYBACK_CHANNEL_ID: String = "hearit_playback_channel"
    const val COMMUTE_PUSH_CHANNEL_ID: String = "hearit_commute_channel"

    fun ensurePlaybackChannel(context: Context) {
        ensureChannel(
            context = context,
            channelId = PLAYBACK_CHANNEL_ID,
            channelName = context.getString(R.string.app_name),
            channelDescription = "Playback controls",
            importance = NotificationManager.IMPORTANCE_LOW,
            showBadge = false,
        )
    }

    fun ensureCommutePushChannel(context: Context) {
        ensureChannel(
            context = context,
            channelId = COMMUTE_PUSH_CHANNEL_ID,
            channelName = context.getString(R.string.app_name),
            channelDescription = "Commute push notifications",
            importance = NotificationManager.IMPORTANCE_DEFAULT,
            showBadge = true,
        )
    }

    private fun ensureChannel(
        context: Context,
        channelId: String,
        channelName: String,
        channelDescription: String,
        importance: Int,
        showBadge: Boolean,
    ) {
        val notificationManager: NotificationManager =
            context.getSystemService(NotificationManager::class.java)

        if (notificationManager.getNotificationChannel(channelId) != null) return

        val channel =
            NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
                setShowBadge(showBadge)
            }

        notificationManager.createNotificationChannel(channel)
    }
}
