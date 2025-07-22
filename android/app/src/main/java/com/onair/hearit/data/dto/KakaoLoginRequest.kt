package com.onair.hearit.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class KakaoLoginRequest(
    val accessToken: String,
)
