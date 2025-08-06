package com.onair.hearit.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TokenReissueResponse(
    @SerialName("accessToken")
    val accessToken: String,
)
