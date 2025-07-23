package com.onair.hearit.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class UserInfo(
    val id: Long,
    val nickname: String,
    val profileImage: String,
)
