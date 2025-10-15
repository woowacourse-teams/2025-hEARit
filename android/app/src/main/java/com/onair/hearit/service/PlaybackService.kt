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
import com.onair.hearit.presentation.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import javax.inject.Inject

@OptIn(UnstableApi::class)
@AndroidEntryPoint
class PlaybackService : MediaSessionService() {
    @Inject
    lateinit var player: ExoPlayer

    @Inject
    lateinit var playbackStateSaver: PlaybackStateSaver
    private lateinit var mediaSession: MediaSession

    @Inject
    lateinit var mediaItemManager: PlaybackMediaItemManager
    private lateinit var playbackPositionListener: PlaybackPositionListener

    private var notificationController: PlayerNotificationController? = null

    @Inject
    lateinit var libraryPlaybackHandler: LibraryPlaybackHandler

    @Inject
    lateinit var recentPlaybackHandler: RecentPlaybackHandler

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()

        initializePlayer()
        player.addListener(playbackStateSaver.listener)
        initializeMediaSession()

        // 2) 알림 + 포그라운드 제어는 컨트롤러에 위임
        notificationController =
            PlayerNotificationController(
                service = this,
                mediaSession = mediaSession,
                channelId = CHANNEL_ID,
                notificationId = NOTIFICATION_ID,
            ).also { it.attach(player) }

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
        }
        return START_STICKY
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
                .setCallback(
                    PlaybackSessionCallback(
                        serviceScope,
                        mediaItemManager,
                        libraryPlaybackHandler,
                        recentPlaybackHandler,
                        playbackPositionListener,
                        playbackStateSaver,
                    ),
                ).build()
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
        playbackStateSaver.release()
        mediaSession.release()
        player.removeListener(playbackStateSaver.listener)
        playbackPositionListener.detach()
        player.release()
    }

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val SESSION_ID = "hearit_session"
        private const val CHANNEL_ID = "hearit_channel"
        private const val ACTION_STOP_SERVICE = "action_stop_service"

        fun stopIntent(context: Context) =
            Intent(context, PlaybackService::class.java).apply {
                action = ACTION_STOP_SERVICE
            }
    }
}
