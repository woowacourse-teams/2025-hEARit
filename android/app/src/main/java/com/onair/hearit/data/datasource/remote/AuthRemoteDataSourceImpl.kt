package com.onair.hearit.data.datasource.remote

import com.onair.hearit.data.api.AuthService
import com.onair.hearit.data.datasource.ErrorResponseHandler
import com.onair.hearit.data.datasource.NetworkResult
import com.onair.hearit.data.datasource.handleApiCall
import com.onair.hearit.data.datasource.handleApiCallUnit
import com.onair.hearit.data.dto.KakaoLoginRequest
import com.onair.hearit.data.dto.KakaoLoginResponse
import com.onair.hearit.data.dto.TokenReissueRequest
import com.onair.hearit.data.dto.TokenReissueResponse
import javax.inject.Inject

class AuthRemoteDataSourceImpl @Inject constructor(
    private val authService: AuthService,
    private val errorResponseHandler: ErrorResponseHandler,
) : AuthRemoteDataSource {
    override suspend fun checkAccessToken(accessToken: String): NetworkResult<Unit> =
        handleApiCallUnit(
            apiCall = { authService.getAuthCheck("Bearer $accessToken") },
            errorHandler = errorResponseHandler,
        )

    override suspend fun kakaoLogin(kakaoLoginRequest: KakaoLoginRequest): NetworkResult<KakaoLoginResponse> =
        handleApiCall(
            apiCall = { authService.postLogin(kakaoLoginRequest) },
            errorHandler = errorResponseHandler,
        )

    override suspend fun refreshAccessToken(reissueRequest: TokenReissueRequest): NetworkResult<TokenReissueResponse> =
        handleApiCall(
            apiCall = { authService.postRefreshToken(reissueRequest) },
            errorHandler = errorResponseHandler,
        )

    override suspend fun withdraw(): NetworkResult<Unit> =
        handleApiCallUnit(
            apiCall = { authService.deleteAccount() },
            errorHandler = errorResponseHandler,
        )
}
