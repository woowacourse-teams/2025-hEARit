package com.onair.hearit.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class PlayingHistoryRequest(
    val hearitId: Long,
    val lastPlayTime: Long,
    val clientEventTime: Long,
)
