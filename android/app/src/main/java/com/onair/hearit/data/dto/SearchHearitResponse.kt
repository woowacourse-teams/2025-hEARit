package com.onair.hearit.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchHearitResponse(
    @SerialName("id")
    val id: Long,
    @SerialName("playTime")
    val playTime: Int,
    @SerialName("summary")
    val summary: String,
    @SerialName("title")
    val title: String,
)
