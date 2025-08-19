package com.onair.hearit.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.onair.hearit.presentation.MainActivity
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

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        playerNotificationManager = PlayerNotificationManager(this)
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
    }

    // startForegroundService와 같은 메서드를 사용해서, 서비스가 명시적으로 시작되는 경우,
    // 외부 컴포넌트가 서비스를 시작하도록 요청할 때 호출됨
    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        if (intent?.action == ACTION_STOP_SERVICE) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            // 서비스가 종료되면 시스템이 서비스를 다시 시작하지 않도록 지시
            return START_NOT_STICKY
        }

        super.onStartCommand(intent, flags, startId)
        // 서비스가 예기치 않게 종료된 경우, 시스템이 서비스를 다시 시작하도록 지시
        return START_STICKY
    }

    private fun initializePlayer() {
        player = ExoPlayer.Builder(this).build().apply { playWhenReady = false }
    }

    private fun initializeMediaSession() {
        val pendingIntent =
            PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                },
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

    override fun onGetSession(info: MediaSession.ControllerInfo) = mediaSession

    override fun onDestroy() {
        serviceScope.cancel()
        stateSaver.release()
        mediaSession.release()
        player.removeListener(stateSaver.listener)
        player.removeListener(foregroundController)
        player.release()
        super.onDestroy()
    }

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val SESSION_ID = "hearit_session"

        const val ACTION_STOP_SERVICE = "hearit.ACTION_STOP_SERVICE"

        fun stopIntent(context: Context) =
            Intent(context, PlaybackService::class.java).apply {
                action = ACTION_STOP_SERVICE
            }
    }
}
