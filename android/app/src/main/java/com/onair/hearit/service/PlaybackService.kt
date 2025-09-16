package com.onair.hearit.service

import android.app.NotificationChannel
import android.app.NotificationManager
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
import androidx.media3.ui.PlayerNotificationManager
import com.onair.hearit.R
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
    private lateinit var mediaItemManager: PlaybackMediaItemManager

    private lateinit var notificationManager: PlayerNotificationManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()

        // 1) 알림 채널 확보
        ensureChannel()

        // 2) 플레이어/세션 초기화
        initializePlayer()
        initializeMediaSession()

        // 3) Media3 PlayerNotificationManager 설정 (세션→ 알림→ 플레이어 순서)
        notificationManager =
            PlayerNotificationManager
                .Builder(this, NOTIFICATION_ID, CHANNEL_ID)
                .setMediaDescriptionAdapter(
                    object : PlayerNotificationManager.MediaDescriptionAdapter {
                        override fun getCurrentContentTitle(player: Player): CharSequence =
                            player.currentMediaItem?.mediaMetadata?.title
                                ?: getString(R.string.app_name)

                        override fun getCurrentContentText(player: Player): CharSequence? = player.currentMediaItem?.mediaMetadata?.artist

                        override fun createCurrentContentIntent(player: Player): PendingIntent? = mediaSession.sessionActivity

                        override fun getCurrentLargeIcon(
                            player: Player,
                            callback: PlayerNotificationManager.BitmapCallback,
                        ) = null
                    },
                ).setSmallIconResourceId(R.drawable.ic_mini_notification)
                .build()
                .apply {
                    setMediaSessionToken(mediaSession.platformToken)
                    setUseNextAction(true)
                    setUsePreviousAction(true)
                    setListener(
                        object : PlayerNotificationManager.NotificationListener, Listener {
                            override fun onNotificationPosted(
                                notificationId: Int,
                                notification: android.app.Notification,
                                ongoing: Boolean,
                            ) {
                                if (ongoing) {
                                    // 재생 중: 포그라운드 유지
                                    startForeground(notificationId, notification)
                                } else {
                                    // 일시정지 등: 포그라운드 분리, 내용만 갱신
                                    stopForeground(STOP_FOREGROUND_DETACH)
                                    getSystemService(NotificationManager::class.java)
                                        .notify(notificationId, notification)
                                }
                            }

                            override fun onNotificationCancelled(
                                notificationId: Int,
                                dismissedByUser: Boolean,
                            ) {
                                // 알림이 내려가면 서비스도 종료
                                stopForeground(STOP_FOREGROUND_REMOVE)
                                stopSelf()
                            }
                        },
                    )
                    setPlayer(player)
                }

        // 4) 상태 저장/에러 최소 핸들링
        mediaItemManager = PlaybackMediaItemManager()
        stateSaver = PlaybackStateSaver(player, serviceScope, this)
        player.addListener(stateSaver.listener)
        player.addListener(
            object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    player.pause()
                }
            },
        )
    }

    // 명시적 시작 요청 (startForegroundService 등)
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
        val title = intent.getStringExtra(EXTRA_TITLE) ?: getString(R.string.app_name)
        val hearitId = intent.getLongExtra(EXTRA_HEARIT_ID, -1L)
        val startPosition = intent.getLongExtra(EXTRA_START_POSITION, 0L)
        val source = intent.getStringExtra(EXTRA_SOURCE) ?: getString(R.string.app_name)
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
                info = info,
                playbackMode = playbackMode,
                bookmarkId = bookmarkId,
            )

        // 여기서는 큐를 단일 아이템으로 명확히 대체
        player.setMediaItems(
            listOf(item),
            0,
            startPosition.coerceAtLeast(0L),
        )
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

    private fun ensureChannel() {
        val notificationManager = getSystemService(NotificationManager::class.java)
        if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
            val ch =
                NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_LOW,
                )
            notificationManager.createNotificationChannel(ch)
        }
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
        notificationManager.setPlayer(null)
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
