package com.onair.hearit.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CategoryHearitResponse(
    @SerialName("createdAt")
    val createdAt: String,
    @SerialName("hearitId")
    val hearitId: Long,
    @SerialName("title")
    val title: String,
)
