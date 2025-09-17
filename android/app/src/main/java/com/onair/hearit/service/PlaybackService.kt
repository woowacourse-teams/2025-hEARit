package com.onair.hearit.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.onair.hearit.domain.model.PlaybackInfo
import com.onair.hearit.presentation.detail.PlayerDetailActivity.Companion.UNKNOWN_SCREEN_ID
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
    private lateinit var historyListener: PlaybackHistoryListener
    private lateinit var mediaItemManager: PlaybackMediaItemManager

    private var notificationController: PlayerNotificationController? = null

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()

        initializePlayer()
        initializeMediaSession()

        // 2) 알림 + 포그라운드 제어는 컨트롤러에 위임
        notificationController =
            PlayerNotificationController(
                service = this,
                mediaSession = mediaSession,
                channelId = CHANNEL_ID,
                notificationId = NOTIFICATION_ID,
            ).also { it.attach(player) }

        // 3) 상태 저장/에러 최소 핸들링
        mediaItemManager = PlaybackMediaItemManager()
        stateSaver = PlaybackStateSaver(player, serviceScope, this)
        historyListener = PlaybackHistoryListener(player, serviceScope).also { it.attach() }
        player.addListener(stateSaver.listener)
        player.addListener(
            object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    player.pause()
                }
            },
        )
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ACTION_STOP_SERVICE -> {
                runCatching {
                    player.pause()
                    player.clearMediaItems()
                }
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }

            ACTION_PLAY_SINGLE -> handlePlay(intent)
        }
        return START_STICKY
    }

    private fun handlePlay(intent: Intent) {
        val audioUrl = intent.getStringExtra(EXTRA_AUDIO_URL)
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "hEARit"
        val hearitId = intent.getLongExtra(EXTRA_HEARIT_ID, -1L)
        val startPosition = intent.getLongExtra(EXTRA_START_POSITION, 0L)
        val source = intent.getStringExtra(EXTRA_SOURCE) ?: "hEARit"
        val playbackMode = intent.getStringExtra(EXTRA_PLAYBACK_MODE) ?: UNKNOWN_SCREEN_ID
        val bookmarkId = intent.getLongExtra(EXTRA_BOOKMARK_ID, -1L).takeIf { it > 0 }

        if (audioUrl.isNullOrEmpty() || hearitId == -1L) {
            stopSelf()
            return
        }

        historyListener.recordIfSwitchingTo(hearitId)

        val info =
            PlaybackInfo(
                hearitId = hearitId,
                title = title,
                source = source,
                audioUrl = audioUrl,
                lastPosition = startPosition,
            )

        val item =
            mediaItemManager.buildMediaItem(
                info = info,
                playbackMode = playbackMode,
                bookmarkId = bookmarkId,
            )

        player.setMediaItems(listOf(item), 0, startPosition.coerceAtLeast(0L))
        player.prepare()
        player.play()
    }

    private fun initializePlayer() {
        val audioAttributes =
            AudioAttributes
                .Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .build()

        player =
            ExoPlayer
                .Builder(this)
                .setAudioAttributes(audioAttributes, true)
                .build()
                .apply {
                    playWhenReady = false
                    setHandleAudioBecomingNoisy(true)
                }
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
                .setSessionActivity(pendingIntent)
                .setCallback(PlaybackSessionCallback(serviceScope))
                .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession = mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        if (!player.isPlaying) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        notificationController?.detach()
        historyListener.detach()
        stateSaver.release()
        mediaSession.release()
        player.removeListener(stateSaver.listener)
        player.release()
    }

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val SESSION_ID = "hearit_session"
        private const val CHANNEL_ID = "hearit_channel"

        private const val EXTRA_AUDIO_URL = "AUDIO_URL"
        private const val EXTRA_TITLE = "TITLE"
        private const val EXTRA_HEARIT_ID = "HEARIT_ID"
        private const val EXTRA_START_POSITION = "START_POSITION"
        private const val EXTRA_SOURCE = "SOURCE"
        private const val EXTRA_PLAYBACK_MODE = "PLAYBACK_MODE"
        private const val EXTRA_BOOKMARK_ID = "BOOKMARK_ID"
        const val ACTION_STOP_SERVICE = "hearit.ACTION_STOP_SERVICE"
        const val ACTION_PLAY_SINGLE = "hearit.ACTION_PLAY_SINGLE"

        fun stopIntent(context: Context) =
            Intent(context, PlaybackService::class.java).apply {
                action = ACTION_STOP_SERVICE
            }
    }
}
