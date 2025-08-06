package com.onair.hearit.data.datasource.remote

import com.onair.hearit.data.datasource.NetworkResult
import com.onair.hearit.data.dto.UserInfoResponse

interface MemberRemoteDataSource {
    suspend fun getUserInfo(token: String?): Result<NetworkResult<UserInfoResponse>>
}
