package com.onair.hearit.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RandomHearitResponse(
    @SerialName("createdAt")
    val createdAt: String,
    @SerialName("id")
    val id: Long,
    @SerialName("playTime")
    val playTime: Int,
    @SerialName("source")
    val source: String,
    @SerialName("summary")
    val summary: String,
    @SerialName("title")
    val title: String,
)
