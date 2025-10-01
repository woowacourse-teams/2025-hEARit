package com.onair.hearit.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RecentUploadResponse(
    @SerialName("id")
    val id: Int,
    @SerialName("title")
    val title: String,
    @SerialName("playTime")
    val playTime: Int,
    @SerialName("lastPlayTime")
    val lastPlayTime: Long,
    @SerialName("createdAt")
    val createdAt: String,
    @SerialName("category")
    val category: CategoryResponse.Content,
)
