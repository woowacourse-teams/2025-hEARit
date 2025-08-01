package com.onair.hearit.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RecommendHearitResponse(
    @SerialName("id")
    val id: Long,
    @SerialName("title")
    val title: String,
    @SerialName("playTime")
    val playTime: Int,
    @SerialName("createdAt")
    val createdAt: String,
    @SerialName("categoryName")
    val categoryName: String,
    @SerialName("categoryColor")
    val categoryColor: String,
)
