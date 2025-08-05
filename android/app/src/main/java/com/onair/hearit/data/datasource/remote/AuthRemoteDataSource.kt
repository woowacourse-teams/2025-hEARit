package com.onair.hearit.data.datasource.remote

import com.onair.hearit.data.datasource.NetworkResult
import com.onair.hearit.data.dto.KakaoLoginRequest
import com.onair.hearit.data.dto.KakaoLoginResponse

interface AuthRemoteDataSource {
    suspend fun kakaoLogin(kakaoLoginRequest: KakaoLoginRequest): Result<NetworkResult<KakaoLoginResponse>>
}
