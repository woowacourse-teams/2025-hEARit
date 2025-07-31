package com.onair.hearit.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HearitResponse(
    @SerialName("id")
    val id: Long,
    @SerialName("title")
    val title: String,
    @SerialName("summary")
    val summary: String,
    @SerialName("source")
    val source: String,
    @SerialName("playTime")
    val playTime: Int,
    @SerialName("createdAt")
    val createdAt: String,
    @SerialName("isBookmarked")
    val isBookmarked: Boolean,
    @SerialName("bookmarkId")
    val bookmarkId: Long?,
    @SerialName("keywords")
    val keywords: List<KeywordResponse>,
)
