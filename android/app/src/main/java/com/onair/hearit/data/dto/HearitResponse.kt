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
    @SerialName("sources")
    val sources: List<SourceResponse>,
    @SerialName("playTime")
    val playTime: Int,
    @SerialName("lastPlayTime")
    val lastPlayTime: Long? = null,
    @SerialName("createdAt")
    val createdAt: String,
    @SerialName("isBookmarked")
    val isBookmarked: Boolean,
    @SerialName("bookmarkId")
    val bookmarkId: Long?,
    @SerialName("category")
    val category: CategoryResponse.Content,
    @SerialName("keywords")
    val keywords: List<KeywordResponse>,
    @SerialName("like")
    val like: LikeResponse,
    @SerialName("viewCount")
    val viewCount: Int,
)
