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
import com.onair.hearit.presentation.main.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

@OptIn(UnstableApi::class)
class PlaybackService : MediaSessionService() {
    private lateinit var player: ExoPlayer
    private lateinit var mediaSession: MediaSession
    private lateinit var stateSaver: PlaybackStateSaver
    private lateinit var playerNotificationManager: PlayerNotificationManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var isServiceStarted = false

    override fun onCreate() {
        super.onCreate()
        playerNotificationManager = PlayerNotificationManager(this)
        initializePlayer()
        initializeMediaSession()
        stateSaver = PlaybackStateSaver(player, serviceScope, this)
        player.addListener(stateSaver.listener)
    }

    // startForegroundService와 같은 메서드를 사용해서, 서비스가 명시적으로 시작되는 경우,
    // 외부 컴포넌트가 서비스를 시작하도록 요청할 때 호출됨
    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        if (intent?.action == ACTION_STOP_SERVICE) {
            runCatching {
                player.pause()
                player.clearMediaItems()
            }
            stopSelf()
            return START_NOT_STICKY
        }

        super.onStartCommand(intent, flags, startId)
        initializeAndStartForeground()

        val audioUrl = intent?.getStringExtra(EXTRA_AUDIO_URL)
        val title = intent?.getStringExtra(EXTRA_TITLE) ?: "hEARit"
        val hearitId = intent?.getLongExtra(EXTRA_HEARIT_ID, -1L) ?: -1L
        val startPosition = intent?.getLongExtra(EXTRA_START_POSITION, 0L) ?: 0L
        val source = intent?.getStringExtra(EXTRA_SOURCE) ?: "hEARit"

        if (audioUrl.isNullOrEmpty() || hearitId == -1L) {
            stopSelf()
            return START_NOT_STICKY
        }

        val item = createMediaItem(audioUrl, title, hearitId, source)
        player.setMediaItems(listOf(item), 0, startPosition.coerceAtLeast(0L))
        player.prepare()
        player.play()

        return START_STICKY
    }

    private fun initializePlayer() {
        player = ExoPlayer.Builder(this).build().apply { playWhenReady = false }
    }

    private fun initializeMediaSession() {
        val mainActivityIntent =
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

        val pendingIntent =
            PendingIntent.getActivity(
                this,
                0,
                mainActivityIntent,
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
        source: String,
    ): MediaItem =
        MediaItem
            .Builder()
            .setUri(url.toUri())
            .setMediaId(id.toString())
            .setMediaMetadata(
                MediaMetadata
                    .Builder()
                    .setTitle(title)
                    .setArtist(source)
                    .build(),
            ).build()

    private fun initializeAndStartForeground() {
        if (!isServiceStarted) {
            val notification = playerNotificationManager.buildForegroundNotification()
            startForeground(NOTIFICATION_ID, notification)
            isServiceStarted = true
        }
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
        private const val EXTRA_SOURCE = "SOURCE"

        const val ACTION_STOP_SERVICE = "hearit.ACTION_STOP_SERVICE"

        fun newIntent(
            context: Context,
            audioUrl: String,
            title: String,
            hearitId: Long,
            startPosition: Long,
            source: String,
        ) = Intent(context, PlaybackService::class.java).apply {
            putExtra(EXTRA_AUDIO_URL, audioUrl)
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_HEARIT_ID, hearitId)
            putExtra(EXTRA_START_POSITION, startPosition)
            putExtra(EXTRA_SOURCE, source)
        }

        fun stopIntent(context: Context) =
            Intent(context, PlaybackService::class.java).apply {
                action = ACTION_STOP_SERVICE
            }
    }
}
