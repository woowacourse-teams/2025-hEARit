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
    private lateinit var playerNotificationManager: PlayerNotificationManager
    private lateinit var foregroundController: ForegroundController
    private lateinit var mediaItemManager: PlaybackMediaItemManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        playerNotificationManager = PlayerNotificationManager(this)
        mediaItemManager = PlaybackMediaItemManager()

        initializePlayer()
        initializeMediaSession()

        stateSaver = PlaybackStateSaver(player, serviceScope, this)
        player.addListener(stateSaver.listener)

        foregroundController =
            ForegroundController(
                service = this,
                notificationManager = playerNotificationManager,
                notificationId = NOTIFICATION_ID,
            )
        player.addListener(foregroundController)

        // ✅ 최소한의 수명/에러 관리
        player.addListener(
            object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    val nothingQueued = player.mediaItemCount == 0
                    if (state == Player.STATE_ENDED && nothingQueued) {
                        stopForeground(STOP_FOREGROUND_REMOVE)
                        stopSelf()
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    player.pause()
                    stopForeground(STOP_FOREGROUND_REMOVE)
                }
            },
        )
    }

    // startForegroundService와 같은 메서드를 사용해서, 서비스가 명시적으로 시작되는 경우,
    // 외부 컴포넌트가 서비스를 시작하도록 요청할 때 호출됨
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
                info,
                playbackMode,
                bookmarkId,
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
                    // 이어폰 뺐을 때 바로 정지되도록
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
                .setCallback(PlaybackSessionCallback(serviceScope))
                .setSessionActivity(pendingIntent)
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
        stateSaver.release()
        mediaSession.release()
        player.removeListener(stateSaver.listener)
        player.removeListener(foregroundController)
        player.release()
    }

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val SESSION_ID = "hearit_session"

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
