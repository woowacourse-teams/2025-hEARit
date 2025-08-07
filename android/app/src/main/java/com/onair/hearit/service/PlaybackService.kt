package com.onair.hearit.service

import android.app.PendingIntent
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
import com.onair.hearit.presentation.splash.SplashActivity
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
        initializeAndStartForeground()
        stateSaver = PlaybackStateSaver(player, serviceScope)
        player.addListener(stateSaver.listener)
    }

    // startService나 startForegroundService와 같은 메서드를 사용해서, 서비스가 명시적으로 시작되는 경우,
    // 외부 컴포넌트가 서비스를 시작하도록 요청할 때 호출됨
    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
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
        val splashActivityIntent =
            Intent(this, SplashActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

        val pendingIntent =
            PendingIntent.getActivity(
                this,
                0,
                splashActivityIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )

        mediaSession =
            MediaSession
                .Builder(this, player)
                .setId(SESSION_ID)
                .setCallback(PlaybackSessionCallback(player, serviceScope))
                .setSessionActivity(pendingIntent)
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
