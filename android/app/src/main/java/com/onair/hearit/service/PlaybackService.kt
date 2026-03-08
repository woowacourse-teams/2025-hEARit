package com.onair.hearit.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.onair.hearit.di.ServiceCoroutineScope
import com.onair.hearit.domain.usecase.AddPlayingHistoryUseCase
import com.onair.hearit.domain.usecase.UpdateRecentPositionUseCase
import com.onair.hearit.presentation.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

@AndroidEntryPoint
@OptIn(UnstableApi::class)
class PlaybackService : MediaSessionService() {
    @Inject
    lateinit var mediaItemManager: PlaybackMediaItemManager

    @Inject
    lateinit var libraryPlaybackHandler: LibraryPlaybackHandler

    @Inject
    lateinit var recentPlaybackHandler: RecentPlaybackHandler

    @Inject
    lateinit var addPlayingHistoryUseCase: AddPlayingHistoryUseCase

    @Inject
    lateinit var updateRecentPositionUseCase: UpdateRecentPositionUseCase

    @Inject
    @ServiceCoroutineScope
    lateinit var serviceScope: CoroutineScope

    @Inject
    lateinit var player: Player

    @Inject
    lateinit var stateSaver: PlaybackStateSaver

    @Inject
    lateinit var sessionCallback: PlaybackSessionCallback

    @Inject
    lateinit var durationTracker: PlaybackDurationTracker

    private lateinit var playbackPositionListener: PlaybackPositionListener

    private lateinit var mediaSession: MediaSession

    private var notificationController: PlayerNotificationController? = null

    private val errorListener =
        object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                player.pause()
            }
        }

    override fun onCreate() {
        super.onCreate()

        playbackPositionListener = PlaybackPositionListener(player)

        stateSaver.service = this
        player.addListener(stateSaver.listener)

        playbackPositionListener.attach()
        sessionCallback.setPlaybackPositionListener(playbackPositionListener)

        durationTracker.attach(player)

        initializeMediaSession()

        notificationController =
            PlayerNotificationController(
                service = this,
                mediaSession = mediaSession,
                channelId = CHANNEL_ID,
                notificationId = NOTIFICATION_ID,
            ).also { it.attach(player) }

        player.addListener(errorListener)
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
                    sessionCallback,
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
        player.removeListener(errorListener)
        player.removeListener(stateSaver.listener)
        notificationController?.detach()
        playbackPositionListener.detach()
        durationTracker.detach()
        stateSaver.release()
        mediaSession.release()
        player.release()
    }

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val SESSION_ID = "hearit_session"
        private const val CHANNEL_ID = "hearit_playback_channel"

        const val ACTION_STOP_SERVICE = "hearit.ACTION_STOP_SERVICE"

        fun stopIntent(context: Context) =
            Intent(context, PlaybackService::class.java).apply {
                action = ACTION_STOP_SERVICE
            }
    }
}
