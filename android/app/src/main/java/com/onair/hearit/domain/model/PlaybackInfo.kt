package com.onair.hearit.domain.model

data class PlaybackInfo(
    val hearitId: Long,
    val audioUrl: String,
    val title: String,
    val lastPosition: Long = 0L,
)
