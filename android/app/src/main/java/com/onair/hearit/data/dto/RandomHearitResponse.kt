package com.onair.hearit.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RandomHearitResponse(
    @SerialName("content")
    val content: List<Content>,
    @SerialName("isEmpty")
    val isEmpty: Boolean,
    @SerialName("cursorId")
    val cursorId: Long,
) {
    @Serializable
    data class Content(
        @SerialName("id")
        val id: Long,
        @SerialName("title")
        val title: String,
        @SerialName("categoryColorCode")
        val categoryColorCode: String,
        @SerialName("isBookmarked")
        val isBookmarked: Boolean,
        @SerialName("bookmarkId")
        val bookmarkId: Long?,
        @SerialName("keywords")
        val keywords: List<KeywordResponse>,
    )
}
