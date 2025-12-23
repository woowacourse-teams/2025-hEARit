package com.onair.hearit.data.datasource.remote

import com.onair.hearit.data.datasource.NetworkResult
import com.onair.hearit.data.dto.UserInfoResponse

interface UserRemoteDataSource {
    suspend fun getUserInfo(): NetworkResult<UserInfoResponse>
}
