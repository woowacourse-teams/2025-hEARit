package com.onair.hearit.presentation.explore

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@ViewModelScoped
class ExplorePlayerManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : ExplorePlayerManager {
    private val _currentPosition = MutableStateFlow(0L)
    override val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    override val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _isPlaybackEnded = MutableStateFlow(false)
    override val isPlaybackEnded: StateFlow<Boolean> = _isPlaybackEnded.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _speed = MutableStateFlow(1.0f)
    override val speed: StateFlow<Float> = _speed.asStateFlow()

    private val playerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    val player: ExoPlayer by lazy {
        ExoPlayer.Builder(context).build().apply {
            addListener(playbackListener)
        }
    }

    private var scriptSyncJob: Job? = null

    private val playbackListener =
        object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_ENDED -> {
                        _isPlaybackEnded.value = true
                        _isPlaying.value = false // 재생 종료 시 false
                        scriptSyncJob?.cancel() // 재생 끝나면 동기화 중단
                    }

                    Player.STATE_IDLE -> {
                        _isPlaying.value = false
                        scriptSyncJob?.cancel()
                    }

                    Player.STATE_READY -> {
                        _isPlaybackEnded.value = false
                        _duration.value = player.duration.coerceAtLeast(0L)
                    }
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying

                if (isPlaying) {
                    _isPlaybackEnded.value = false
                    _duration.value = player.duration.coerceAtLeast(0L)
                    startScriptSync()
                } else {
                    scriptSyncJob?.cancel()
                }
            }
        }

    override fun play(
        audioUrl: String,
        startPosition: Long,
    ) {
        val mediaItem = MediaItem.fromUri(audioUrl)
        player.setMediaItem(mediaItem)
        player.prepare()
        if (startPosition > 0) player.seekTo(startPosition)
        player.play()
        _isPlaybackEnded.value = false
    }

    override fun seekTo(position: Long) {
        player.seekTo(position)
        player.play()
    }

    override fun stop() {
        player.stop()
        scriptSyncJob?.cancel()
    }

    override fun resume() {
        if (!player.isPlaying) {
            player.play()
        }
    }

    override fun pause() {
        player.pause()
        player.playWhenReady = false
        scriptSyncJob?.cancel()
    }

    override fun setPlaybackSpeed(speed: Float) {
        val params = player.playbackParameters.withSpeed(speed)
        player.playbackParameters = params

        _speed.value = speed
    }

    private fun startScriptSync() {
        scriptSyncJob?.cancel()
        scriptSyncJob =
            playerScope.launch {
                while (isActive) {
                    if (player.isPlaying && player.playbackState == Player.STATE_READY) {
                        val position = player.currentPosition
                        _currentPosition.value = position
                    }
                    delay(200L)
                }
            }
    }
}
