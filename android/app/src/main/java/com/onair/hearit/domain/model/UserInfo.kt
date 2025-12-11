package com.onair.hearit.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class UserInfo(
    val id: Long,
    val nickname: String,
    val profileImage: String?,
) {
    companion object {
        fun default() =
            UserInfo(
                id = -1,
                nickname = "hEARit",
                profileImage = null,
            )
    }
}

fun UserInfo.isLoggedIn(): Boolean = this.id > 0
