package com.onair.hearit.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ShortAudioUrlResponse(
    @SerialName("id")
    val id: Long,
    @SerialName("url")
    val url: String,
)
