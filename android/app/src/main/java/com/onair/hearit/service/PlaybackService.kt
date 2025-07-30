package com.onair.hearit.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.onair.hearit.R

@OptIn(UnstableApi::class)
class PlaybackService : MediaSessionService() {
    private lateinit var player: ExoPlayer
    private lateinit var mediaSession: MediaSession

    private var playerListener: Player.Listener? = null

    private var isServiceStarted = false

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()

        player = ExoPlayer.Builder(this).build()
        playerListener =
            object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    if (isPlaying) {
                        startServiceForeground()
                    }
                }
            }
        player.addListener(playerListener!!)

        mediaSession =
            MediaSession
                .Builder(this, player)
                .setId(SESSION_ID)
                .build()

        setMediaNotificationProvider(
            DefaultMediaNotificationProvider.Builder(this).build(),
        )
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        super.onStartCommand(intent, flags, startId)

        val audioUrl = intent?.getStringExtra(EXTRA_AUDIO_URL)
        val title = intent?.getStringExtra(EXTRA_TITLE) ?: "hearit"
        val hearitId = intent?.getLongExtra(EXTRA_HEARIT_ID, -1L) ?: -1L

        if (!audioUrl.isNullOrEmpty() && hearitId != -1L) {
            val mediaItem =
                MediaItem
                    .Builder()
                    .setUri(audioUrl.toUri())
                    .setMediaId(hearitId.toString())
                    .setMediaMetadata(MediaMetadata.Builder().setTitle(title).build())
                    .build()

            player.setMediaItem(mediaItem)
            player.playWhenReady = false
            player.prepare()
        }

        return START_STICKY
    }

    private fun createNotificationChannel() {
        val channel =
            NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW,
            )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification(
        title: String,
        content: String,
    ) = NotificationCompat
        .Builder(this, CHANNEL_ID)
        .setContentTitle(title)
        .setContentText(content)
        .setSmallIcon(R.drawable.ic_mini_notification)
        .setOngoing(true)
        .build()

    private fun startServiceForeground() {
        if (!isServiceStarted) {
            val notification =
                buildNotification(
                    getString(R.string.notification_title_playing),
                    getString(R.string.notification_text_playing),
                )
            startForeground(NOTIFICATION_ID, notification)
            isServiceStarted = true
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo) = mediaSession

    override fun onDestroy() {
        mediaSession.release()
        playerListener?.let { player.removeListener(it) }
        player.release()
        super.onDestroy()
    }

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "hearit_channel"
        private const val SESSION_ID = "hearit_session"
        private const val EXTRA_AUDIO_URL = "AUDIO_URL"
        private const val EXTRA_TITLE = "TITLE"
        private const val EXTRA_HEARIT_ID = "HEARIT_ID"

        fun newIntent(
            context: Context,
            audioUrl: String,
            title: String,
            hearitId: Long,
        ): Intent =
            Intent(context, PlaybackService::class.java).apply {
                putExtra(EXTRA_AUDIO_URL, audioUrl)
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_HEARIT_ID, hearitId)
            }
    }
}
