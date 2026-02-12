package com.onair.hearit.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LikeResponse(
    @SerialName("count")
    val count: Int,
    @SerialName("isLiked")
    val isLiked: Boolean,
)
