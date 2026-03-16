package com.onair.hearit.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AdvertisementResponse(
    @SerialName("id")
    val id: Long,
    @SerialName("imageUrl")
    val imageUrl: String,
    @SerialName("linkUrl")
    val linkUrl: String,
    @SerialName("title")
    val title: String,
)
