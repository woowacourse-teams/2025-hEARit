package com.onair.hearit.service

import android.app.Notification
import android.app.Service
import androidx.annotation.DrawableRes
import androidx.annotation.OptIn
import androidx.core.app.NotificationManagerCompat
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.ui.PlayerNotificationManager
import com.onair.hearit.R
import com.onair.hearit.notification.HearitNotificationChannels

@OptIn(UnstableApi::class)
class PlayerNotificationController(
    private val service: MediaSessionService,
    private val mediaSession: MediaSession,
    private val channelId: String,
    private val notificationId: Int,
    @DrawableRes private val smallIconResId: Int = R.drawable.ic_mini_notification,
) {
    private var manager: PlayerNotificationManager? = null

    fun attach(player: Player): PlayerNotificationManager {
        manager?.let { return it }

        ensureChannel()
        manager =
            createNotificationManager().apply {
                setPlayer(player)
            }

        return manager!!
    }

    fun detach() {
        manager?.setPlayer(null)
        manager = null
    }

    private fun createNotificationManager(): PlayerNotificationManager =
        PlayerNotificationManager
            .Builder(service, notificationId, channelId)
            .setMediaDescriptionAdapter(createDescriptionAdapter())
            .setSmallIconResourceId(smallIconResId)
            .setNotificationListener(createNotificationListener())
            .build()
            .apply {
                setMediaSessionToken(mediaSession.platformToken)
                setUseNextAction(true)
                setUsePreviousAction(true)
            }

    private fun createDescriptionAdapter() =
        object : PlayerNotificationManager.MediaDescriptionAdapter {
            override fun getCurrentContentTitle(player: Player): CharSequence =
                player.currentMediaItem?.mediaMetadata?.title
                    ?: service.getString(R.string.app_name)

            override fun getCurrentContentText(player: Player): CharSequence? = player.currentMediaItem?.mediaMetadata?.artist

            override fun createCurrentContentIntent(player: Player) = mediaSession.sessionActivity

            override fun getCurrentLargeIcon(
                player: Player,
                callback: PlayerNotificationManager.BitmapCallback,
            ) = null
        }

    private fun createNotificationListener() =
        object : PlayerNotificationManager.NotificationListener {
            override fun onNotificationPosted(
                notificationId: Int,
                notification: Notification,
                ongoing: Boolean,
            ) {
                if (ongoing) {
                    service.startForeground(notificationId, notification)
                } else {
                    service.stopForeground(Service.STOP_FOREGROUND_DETACH)
                    postNotificationSafely(notificationId, notification)
                }
            }

            override fun onNotificationCancelled(
                notificationId: Int,
                dismissedByUser: Boolean,
            ) {
                service.stopForeground(Service.STOP_FOREGROUND_REMOVE)
                service.stopSelf()
            }
        }

    private fun postNotificationSafely(
        notificationId: Int,
        notification: Notification,
    ) {
        try {
            NotificationManagerCompat.from(service).notify(notificationId, notification)
        } catch (_: SecurityException) {
        }
    }

    private fun ensureChannel() {
        HearitNotificationChannels.ensurePlaybackChannel(service)
    }
}
