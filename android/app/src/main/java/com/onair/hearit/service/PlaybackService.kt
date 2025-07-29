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

    private var isServiceStarted = false

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()

        player = ExoPlayer.Builder(this).build()
        player.addListener(
            object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    if (isPlaying) {
                        startServiceForeground()
                    }
                }
            },
        )

        mediaSession =
            MediaSession
                .Builder(this, player)
                .setId("hearit_session")
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
        createNotificationChannel()

        val notification = buildNotification("히어릿 재생 준비 중", "재생을 준비하고 있어요.")
        startForeground(1001, notification)

        val audioUrl = intent?.getStringExtra(AUDIO_URL)
        val title = intent?.getStringExtra(TITLE) ?: "hearit"
        val hearitId = intent?.getLongExtra(HEARIT_ID, -1) ?: -1

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
                "hearit_channel",
                "Hearit Playback",
                NotificationManager.IMPORTANCE_LOW,
            )
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    private fun buildNotification(
        title: String,
        content: String,
    ) = NotificationCompat
        .Builder(this, "hearit_channel")
        .setContentTitle(title)
        .setContentText(content)
        .setSmallIcon(R.drawable.ic_mini_notification)
        .setOngoing(true)
        .build()

    private fun startServiceForeground() {
        if (!isServiceStarted) {
            val notification =
                buildNotification("히어릿 재생 중", "음악이 재생되고 있어요.")
            startForeground(1001, notification)
            isServiceStarted = true
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo) = mediaSession

    override fun onDestroy() {
        mediaSession.release()
        player.release()
        super.onDestroy()
    }

    companion object {
        private const val AUDIO_URL = "AUDIO_URL"
        private const val TITLE = "TITLE"
        private const val HEARIT_ID = "HEARIT_ID"

        fun newIntent(
            context: Context,
            audioUrl: String,
            title: String,
            hearitId: Long,
        ): Intent =
            Intent(context, PlaybackService::class.java).apply {
                putExtra(AUDIO_URL, audioUrl)
                putExtra(TITLE, title)
                putExtra(HEARIT_ID, hearitId)
            }
    }
}
