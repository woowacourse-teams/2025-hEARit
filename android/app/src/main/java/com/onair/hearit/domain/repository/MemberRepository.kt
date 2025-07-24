package com.onair.hearit.domain.repository

import com.onair.hearit.domain.model.UserInfo

interface MemberRepository {
    suspend fun getUserInfo(): Result<UserInfo>
}
