package com.onair.hearit.data.datasource.remote

import com.onair.hearit.data.datasource.NetworkResult
import com.onair.hearit.data.dto.KakaoLoginRequest
import com.onair.hearit.data.dto.KakaoLoginResponse
import com.onair.hearit.data.dto.TokenReissueRequest
import com.onair.hearit.data.dto.TokenReissueResponse

interface AuthRemoteDataSource {
    suspend fun checkAccessToken(accessToken: String): NetworkResult<Unit>

    suspend fun kakaoLogin(kakaoLoginRequest: KakaoLoginRequest): NetworkResult<KakaoLoginResponse>

    suspend fun refreshAccessToken(reissueRequest: TokenReissueRequest): NetworkResult<TokenReissueResponse>

    suspend fun withdraw(): NetworkResult<Unit>
}
