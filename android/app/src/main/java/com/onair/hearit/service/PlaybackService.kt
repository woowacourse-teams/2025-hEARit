package com.onair.hearit.service

import android.content.Intent
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

@OptIn(UnstableApi::class)
class PlaybackService : MediaSessionService() {
    private lateinit var player: ExoPlayer
    private lateinit var mediaSession: MediaSession

    override fun onCreate() {
        super.onCreate()

        player = ExoPlayer.Builder(this).build()

        mediaSession =
            MediaSession
                .Builder(this, player)
                .setId("hearit_session")
                .build()

        setMediaNotificationProvider(
            DefaultMediaNotificationProvider.Builder(this).build(),
        )
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        val audioUrl = intent?.getStringExtra("AUDIO_URL")
        val title = intent?.getStringExtra("TITLE") ?: "히어릿"

        if (!audioUrl.isNullOrEmpty()) {
            val mediaItem =
                MediaItem
                    .Builder()
                    .setUri(audioUrl.toUri())
                    .setMediaMetadata(
                        MediaMetadata
                            .Builder()
                            .setTitle(title)
                            .build(),
                    ).build()

            player.setMediaItem(mediaItem)
            player.prepare()
            player.playWhenReady = true
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onDestroy() {
        mediaSession.release()
        player.release()
        super.onDestroy()
    }
}
