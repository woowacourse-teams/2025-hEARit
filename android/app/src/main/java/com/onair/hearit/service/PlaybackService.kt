package com.onair.hearit.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.concurrent.futures.CallbackToFutureAdapter
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
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionError
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.ListenableFuture
import com.onair.hearit.R
import com.onair.hearit.di.RepositoryProvider
import com.onair.hearit.domain.model.PlaybackInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(UnstableApi::class)
class PlaybackService : MediaSessionService() {
    private lateinit var player: ExoPlayer
    private lateinit var mediaSession: MediaSession

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var isServiceStarted = false

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        initializePlayer()
        initializeMediaSession()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        super.onStartCommand(intent, flags, startId)
        initializeAndStartForeground()

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
        player =
            ExoPlayer.Builder(this).build().apply {
                playWhenReady = false
            }
    }

    private fun initializeMediaSession() {
        mediaSession =
            MediaSession
                .Builder(this, player)
                .setId(SESSION_ID)
                .setCallback(
                    object : MediaSession.Callback {
                        override fun onConnect(
                            session: MediaSession,
                            controller: MediaSession.ControllerInfo,
                        ): MediaSession.ConnectionResult {
                            val base = super.onConnect(session, controller)
                            val sessionCommands =
                                base.availableSessionCommands
                                    .buildUpon()
                                    .add(PRELOAD_RECENT_COMMAND)
                                    .build()
                            val playerCommands = base.availablePlayerCommands

                            return MediaSession.ConnectionResult
                                .AcceptedResultBuilder(session)
                                .setAvailableSessionCommands(sessionCommands)
                                .setAvailablePlayerCommands(playerCommands)
                                .build()
                        }

                        override fun onCustomCommand(
                            session: MediaSession,
                            controller: MediaSession.ControllerInfo,
                            command: SessionCommand,
                            args: Bundle,
                        ): ListenableFuture<SessionResult> {
                            if (command.customAction != CMD_PRELOAD_RECENT) {
                                return super.onCustomCommand(session, controller, command, args)
                            }
                            return CallbackToFutureAdapter.getFuture { completer ->
                                val job =
                                    serviceScope.launch {
                                        try {
                                            loadRecentInfo()?.let { info ->
                                                prepareIfNeeded(info)
                                            }
                                            completer.set(SessionResult(SessionResult.RESULT_SUCCESS))
                                        } catch (_: Exception) {
                                            completer.set(SessionResult(SessionError.ERROR_UNKNOWN))
                                        }
                                    }
                                completer.addCancellationListener({ job.cancel() }, Runnable::run)
                                "preload_recent_command"
                            }
                        }

                        override fun onPlaybackResumption(
                            session: MediaSession,
                            controller: MediaSession.ControllerInfo,
                        ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> =
                            CallbackToFutureAdapter.getFuture { completer ->
                                val job =
                                    serviceScope.launch {
                                        try {
                                            val info = loadRecentInfo()
                                            val result =
                                                info?.let { toItemsWithStart(it) }
                                                    ?: MediaSession.MediaItemsWithStartPosition(
                                                        emptyList(),
                                                        0,
                                                        0L,
                                                    )
                                            completer.set(result)
                                        } catch (_: Exception) {
                                            completer.set(
                                                MediaSession.MediaItemsWithStartPosition(
                                                    emptyList(),
                                                    0,
                                                    0L,
                                                ),
                                            )
                                        }
                                    }
                                completer.addCancellationListener({ job.cancel() }, Runnable::run)
                                "onPlaybackResumption"
                            }
                    },
                ).build()

        setMediaNotificationProvider(
            DefaultMediaNotificationProvider.Builder(this).build(),
        )
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
        val notification =
            buildNotification(
                getString(R.string.notification_title_preparing),
                getString(R.string.notification_text_preparing),
            )
        startForeground(NOTIFICATION_ID, notification)
        isServiceStarted = true
    }

    // 임시 알림 생성
    private fun buildNotification(
        title: String,
        content: String,
    ) = NotificationCompat
        .Builder(this, CHANNEL_ID)
        .setContentTitle(title)
        .setContentText(content)
        .setSmallIcon(R.drawable.ic_mini_notification)
        .setOngoing(true)
        .build()

    // 임시 알림을 위한 채널 생성
    private fun createNotificationChannel() {
        val manager = getSystemService(NotificationManager::class.java)
        val channel =
            NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW,
            )
        manager.createNotificationChannel(channel)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession = mediaSession

    // 최근 들은 히어릿을 서비스 내에서 불러오기 위한 코드
    private suspend fun loadRecentInfo(): PlaybackInfo? =
        withContext(Dispatchers.IO) {
            RepositoryProvider.recentHearitRepository
                .getRecentHearit()
                .getOrNull()
                ?.let { recent ->
                    RepositoryProvider.getPlaybackInfoUseCase(recent.id).getOrNull()
                }
        }

    // 미디어 아이템 구현
    private fun buildMediaItem(info: PlaybackInfo): MediaItem =
        createMediaItem(
            url = info.audioUrl,
            title = info.title,
            id = info.hearitId,
        )

    private fun prepareIfNeeded(info: PlaybackInfo) {
        val item = buildMediaItem(info)
        val sameItem = player.currentMediaItem?.mediaId == item.mediaId
        val preparedOrBuffering =
            player.playbackState == Player.STATE_READY || player.playbackState == Player.STATE_BUFFERING
        if (!(sameItem && preparedOrBuffering)) {
            player.setMediaItems(listOf(item), 0, info.lastPosition)
            player.prepare()
        }
    }

    private fun toItemsWithStart(info: PlaybackInfo): MediaSession.MediaItemsWithStartPosition =
        MediaSession.MediaItemsWithStartPosition(
            listOf(buildMediaItem(info)),
            0,
            info.lastPosition,
        )

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
        mediaSession.release()
        player.release()
    }

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "hearit_channel"
        private const val SESSION_ID = "hearit_session"

        private const val EXTRA_AUDIO_URL = "AUDIO_URL"
        private const val EXTRA_TITLE = "TITLE"
        private const val EXTRA_HEARIT_ID = "HEARIT_ID"
        private const val EXTRA_START_POSITION = "START_POSITION"

        const val CMD_PRELOAD_RECENT = "hearit.PRELOAD_RECENT"
        val PRELOAD_RECENT_COMMAND: SessionCommand =
            SessionCommand(CMD_PRELOAD_RECENT, Bundle.EMPTY)

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
