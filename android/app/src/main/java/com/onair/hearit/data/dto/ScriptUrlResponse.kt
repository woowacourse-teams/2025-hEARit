package com.onair.hearit.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ScriptUrlResponse(
    @SerialName("id")
    val id: Long,
    @SerialName("url")
    val url: String,
)
