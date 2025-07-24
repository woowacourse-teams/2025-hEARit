package com.onair.hearit.presentation

interface PlayerControllerView {
    fun showPlayerControlView()

    fun hidePlayerControlView()

    fun play(hearitId: Long)

    fun pause()
}
