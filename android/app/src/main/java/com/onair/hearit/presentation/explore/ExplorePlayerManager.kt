package com.onair.hearit.presentation.explore

import kotlinx.coroutines.flow.StateFlow

interface ExplorePlayerManager {
    val currentPosition: StateFlow<Long>

    val isPlaying: StateFlow<Boolean>
    val speed: StateFlow<Float>
    val duration: StateFlow<Long>
    val isPlaybackEnded: StateFlow<Boolean>

    fun play(
        audioUrl: String,
        startPosition: Long = 0L,
    )

    fun seekTo(position: Long)

    fun stop()

    fun pause()

    fun resume()

    fun setPlaybackSpeed(speed: Float)
}
