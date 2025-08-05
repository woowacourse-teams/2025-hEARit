package com.onair.hearit.domain.model

data class LoginToken(
    val accessToken: String,
    val refreshToken: String,
)
