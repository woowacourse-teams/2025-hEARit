package com.onair.hearit.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.onair.hearit.R

@OptIn(UnstableApi::class)
class PlaybackService : MediaSessionService() {
    private lateinit var player: ExoPlayer
    private lateinit var mediaSession: MediaSession

    private var isServiceStarted = false

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()
        initializePlayer()
        initializeMediaSession()
    }

    // 밖에서 intent를 보내서 서비스를 실행하려고 할때 호출되는 부분임
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

        if (!audioUrl.isNullOrEmpty() && hearitId != -1L) {
            player.setMediaItem(createMediaItem(audioUrl, title, hearitId))
            player.prepare()
        }

        return START_STICKY
    }

    // 처음 서비스가 생성되고 나서 자동으로 play되는 것을 막기 위함. 사용자의 상호작용을 통해 실행될 수 있도록
    private fun initializePlayer() {
        player =
            ExoPlayer.Builder(this).build().apply {
                playWhenReady = false
            }
    }

    // mediaSession은 시스템과 앱의 미디어를 연결해주는 역할로, 시스템 UI와 연결하기 위해 필요함
    // exoPlayer와 Android를 연결해주는 장치임
    private fun initializeMediaSession() {
        mediaSession =
            MediaSession
                .Builder(this, player)
                .setId(SESSION_ID)
                .build()

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
                MediaMetadata.Builder().setTitle(title).build(),
            ).build()

    // 서비스가 아직 시작하지 않았다면, startForeground로 서비스 실행하도록 함
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

    // foreground를 위해 기본으로 사용할 Notification을 생성
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

    // Notification을 위한 히어릿 채널을 생성함
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

    override fun onDestroy() {
        mediaSession.release()
        player.release()
        super.onDestroy()
    }

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "hearit_channel"
        private const val SESSION_ID = "hearit_session"

        private const val EXTRA_AUDIO_URL = "AUDIO_URL"
        private const val EXTRA_TITLE = "TITLE"
        private const val EXTRA_HEARIT_ID = "HEARIT_ID"

        fun newIntent(
            context: Context,
            audioUrl: String,
            title: String,
            hearitId: Long,
        ) = Intent(context, PlaybackService::class.java).apply {
            putExtra(EXTRA_AUDIO_URL, audioUrl)
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_HEARIT_ID, hearitId)
        }
    }
}
