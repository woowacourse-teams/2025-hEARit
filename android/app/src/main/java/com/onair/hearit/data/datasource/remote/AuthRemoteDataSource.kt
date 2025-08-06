package com.onair.hearit.data.datasource.remote

import com.onair.hearit.data.datasource.NetworkResult
import com.onair.hearit.data.dto.KakaoLoginRequest
import com.onair.hearit.data.dto.KakaoLoginResponse
import com.onair.hearit.data.dto.TokenReissueRequest
import com.onair.hearit.data.dto.TokenReissueResponse

interface AuthRemoteDataSource {
    suspend fun kakaoLogin(kakaoLoginRequest: KakaoLoginRequest): Result<NetworkResult<KakaoLoginResponse>>

    suspend fun refreshAccessToken(reissueRequest: TokenReissueRequest): Result<NetworkResult<TokenReissueResponse>>

    suspend fun checkAccessToken(token: String): Result<NetworkResult<Unit>>
}
