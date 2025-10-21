package com.onair.hearit.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.onair.hearit.presentation.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import timber.log.Timber
import javax.inject.Inject

@OptIn(UnstableApi::class)
@AndroidEntryPoint
class PlaybackService : MediaSessionService() {
    @Inject
    lateinit var player: ExoPlayer

    @Inject
    lateinit var libraryPlaybackHandler: LibraryPlaybackHandler

    @Inject
    lateinit var playbackStateSaver: PlaybackStateSaver

    @Inject
    lateinit var mediaItemManager: PlaybackMediaItemManager

    @Inject
    lateinit var recentPlaybackHandler: RecentPlaybackHandler

    @Inject
    lateinit var serviceScope: CoroutineScope

    private lateinit var mediaSession: MediaSession

    private lateinit var playbackPositionListener: PlaybackPositionListener

    private var notificationController: PlayerNotificationController? = null

    override fun onCreate() {
        super.onCreate()

        player.addListener(playbackStateSaver.listener)
        playbackPositionListener = PlaybackPositionListener(player)
        playbackPositionListener.attach()

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

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        if (intent?.action == ACTION_STOP_SERVICE) {
            runCatching {
                player.pause()
                player.clearMediaItems()
            }.onFailure { e ->
                Timber.e(e, "플레이어를 정상적으로 중지하지 못했습니다.")
            }

            serviceScope.cancel()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

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
