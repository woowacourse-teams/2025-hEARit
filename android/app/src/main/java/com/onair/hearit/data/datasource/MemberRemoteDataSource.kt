package com.onair.hearit.data.datasource

import com.onair.hearit.data.dto.UserInfoResponse

interface MemberRemoteDataSource {
    suspend fun getUserInfo(): Result<UserInfoResponse>
}
