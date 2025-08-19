package com.onair.hearit.service

import android.app.Service
import androidx.media3.common.Player
import androidx.media3.session.MediaSessionService

class ForegroundController(
    private val service: MediaSessionService,
    private val notificationManager: PlayerNotificationManager,
    private val notificationId: Int,
) : Player.Listener {
    private var isStarted = false

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        if (isPlaying && !isStarted) {
            val notification = notificationManager.buildForegroundNotification()
            service.startForeground(notificationId, notification)
            isStarted = true
        } else if (!isPlaying && isStarted) {
            service.stopForeground(Service.STOP_FOREGROUND_DETACH)
            isStarted = false
        }
    }

    override fun onPlaybackStateChanged(state: Int) {
        if (state == Player.STATE_ENDED) {
            if (isStarted) {
                service.stopForeground(Service.STOP_FOREGROUND_REMOVE)
                isStarted = false
            }
            service.stopSelf()
        }
    }
}
