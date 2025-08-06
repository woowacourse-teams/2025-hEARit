package com.onair.hearit.service

import android.content.Context
import android.content.Intent
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

@OptIn(UnstableApi::class)
class PlaybackService : MediaSessionService() {
    private lateinit var player: ExoPlayer
    private lateinit var mediaSession: MediaSession
    private lateinit var stateSaver: PlaybackStateSaver
    private lateinit var notificationManager: NotificationManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var isServiceStarted = false

    override fun onCreate() {
        super.onCreate()
        notificationManager = NotificationManager(this)
        initializePlayer()
        initializeMediaSession()
        stateSaver = PlaybackStateSaver(player, serviceScope)
        player.addListener(stateSaver.listener)
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        initializeAndStartForeground()
        super.onStartCommand(intent, flags, startId)
        val audioUrl = intent?.getStringExtra(EXTRA_AUDIO_URL)
        val title = intent?.getStringExtra(EXTRA_TITLE) ?: "hearit"
        val hearitId = intent?.getLongExtra(EXTRA_HEARIT_ID, -1L) ?: -1L
        val startPosition = intent?.getLongExtra(EXTRA_START_POSITION, 0L) ?: 0L

        if (!audioUrl.isNullOrEmpty() && hearitId != -1L) {
            player.setMediaItem(createMediaItem(audioUrl, title, hearitId))
            player.prepare()
            player.seekTo(startPosition.coerceAtLeast(0L))
            player.play()
        }
        return START_STICKY
    }

    private fun initializePlayer() {
        player = ExoPlayer.Builder(this).build().apply { playWhenReady = false }
    }

    private fun initializeMediaSession() {
        mediaSession =
            MediaSession
                .Builder(this, player)
                .setId(SESSION_ID)
                .setCallback(PlaybackSessionCallback(player, serviceScope))
                .build()
    }

    private fun createMediaItem(
        url: String,
        title: String,
        id: Long,
    ): MediaItem =
        MediaItem
            .Builder()
            .setUri(url.toUri())
            .setMediaId(id.toString())
            .setMediaMetadata(
                MediaMetadata
                    .Builder()
                    .setTitle(title)
                    .build(),
            ).build()

    private fun initializeAndStartForeground() {
        if (isServiceStarted) return
        val notification = notificationManager.buildForegroundNotification()
        startForeground(NOTIFICATION_ID, notification)
        isServiceStarted = true
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession = mediaSession

    override fun onDestroy() {
        serviceScope.cancel()
        stateSaver.release()
        mediaSession.release()
        player.release()
        super.onDestroy()
    }

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val SESSION_ID = "hearit_session"

        private const val EXTRA_AUDIO_URL = "AUDIO_URL"
        private const val EXTRA_TITLE = "TITLE"
        private const val EXTRA_HEARIT_ID = "HEARIT_ID"
        private const val EXTRA_START_POSITION = "START_POSITION"

        fun newIntent(
            context: Context,
            audioUrl: String,
            title: String,
            hearitId: Long,
            startPosition: Long,
        ) = Intent(context, PlaybackService::class.java).apply {
            putExtra(EXTRA_AUDIO_URL, audioUrl)
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_HEARIT_ID, hearitId)
            putExtra(EXTRA_START_POSITION, startPosition)
        }
    }
}
