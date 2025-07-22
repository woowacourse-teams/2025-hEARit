package com.onair.hearit.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class KakaoLoginResponse(
    @SerialName("accessToken")
    val accessToken: String,
)
