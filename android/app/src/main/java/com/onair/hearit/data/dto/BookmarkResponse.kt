package com.onair.hearit.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BookmarkResponse(
    @SerialName("content")
    val content: List<Content>,
    @SerialName("page")
    val page: Int,
    @SerialName("size")
    val size: Int,
    @SerialName("totalPages")
    val totalPages: Int,
    @SerialName("totalElements")
    val totalElements: Int,
    @SerialName("isFirst")
    val isFirst: Boolean,
    @SerialName("isLast")
    val isLast: Boolean,
) {
    @Serializable
    data class Content(
        @SerialName("hearitId")
        val hearitId: Long,
        @SerialName("bookmarkId")
        val bookmarkId: Long,
        @SerialName("title")
        val title: String,
        @SerialName("summary")
        val summary: String,
        @SerialName("playTime")
        val playTime: Int,
    )
}
