package com.onair.hearit.presentation.detail

import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSession.ControllerInfo
import androidx.media3.session.MediaSessionService
import com.onair.hearit.R

@androidx.annotation.OptIn(UnstableApi::class)
class PlaybackService : MediaSessionService() {
    private lateinit var player: ExoPlayer
    private lateinit var mediaSession: MediaSession

    override fun onCreate() {
        super.onCreate()

        player =
            ExoPlayer.Builder(this).build().apply {
                setMediaItem(
                    MediaItem
                        .Builder()
                        .setUri("android.resource://$packageName/${R.raw.test_audio2}".toUri())
                        .setMediaMetadata(
                            MediaMetadata
                                .Builder()
                                .setTitle("테스트 오디오")
                                .setArtist("HEARit")
                                .build(),
                        ).build(),
                )
                prepare()
                playWhenReady = false // MediaController 쪽에서 play() 요청할 때 실행
            }

        mediaSession =
            MediaSession
                .Builder(this, player)
                .setId("hearit_session")
                .build()

        setMediaNotificationProvider(
            DefaultMediaNotificationProvider.Builder(this).build(),
        )
    }

    override fun onGetSession(controllerInfo: ControllerInfo): MediaSession? = mediaSession

    override fun onDestroy() {
        mediaSession.release()
        player.release()
        super.onDestroy()
    }
}
