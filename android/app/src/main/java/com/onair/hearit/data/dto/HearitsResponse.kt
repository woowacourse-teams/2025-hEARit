package com.onair.hearit.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HearitsResponse(
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
        @SerialName("id")
        val id: Long,
        @SerialName("title")
        val title: String,
        @SerialName("playTime")
        val playTime: Int,
        @SerialName("lastPlayTime")
        val lastPlayTime: Long? = null,
        @SerialName("createdAt")
        val createdAt: String,
        @SerialName("keywords")
        val keywords: List<KeywordResponse>,
        @SerialName("category")
        val category: CategoryResponse.Content,
    )
}
