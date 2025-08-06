package com.onair.hearit.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class TokenReissueRequest(
    val refreshToken: String,
)
