package com.onair.hearit.data.datasource.remote

import com.onair.hearit.data.api.AuthService
import com.onair.hearit.data.datasource.ApiErrorMessages.ERROR_RESPONSE_BODY_NULL_MESSAGE
import com.onair.hearit.data.datasource.ErrorResponseHandler
import com.onair.hearit.data.datasource.NetworkResult
import com.onair.hearit.data.datasource.handleApiCall
import com.onair.hearit.data.dto.KakaoLoginRequest
import com.onair.hearit.data.dto.KakaoLoginResponse
import com.onair.hearit.data.dto.TokenReissueRequest
import com.onair.hearit.data.dto.TokenReissueResponse

class AuthRemoteDataSourceImpl(
    private val authService: AuthService,
    private val errorResponseHandler: ErrorResponseHandler,
) : AuthRemoteDataSource {
    override suspend fun kakaoLogin(kakaoLoginRequest: KakaoLoginRequest): Result<NetworkResult<KakaoLoginResponse>> =
        handleApiCall(
            apiCall = { authService.postLogin(kakaoLoginRequest) },
            transform = { response ->
                response.body() ?: throw IllegalStateException(ERROR_RESPONSE_BODY_NULL_MESSAGE)
            },
            errorHandler = errorResponseHandler,
        )

    override suspend fun refreshAccessToken(reissueRequest: TokenReissueRequest): Result<NetworkResult<TokenReissueResponse>> =
        handleApiCall(
            apiCall = { authService.postRefreshToken(reissueRequest) },
            transform = { response ->
                response.body() ?: throw IllegalStateException(ERROR_RESPONSE_BODY_NULL_MESSAGE)
            },
            errorHandler = errorResponseHandler,
        )

    override suspend fun checkAccessToken(token: String): Result<NetworkResult<Unit>> =
        handleApiCall(
            apiCall = { authService.getAuthCheck(token) },
            transform = { },
            errorHandler = errorResponseHandler,
        )
}
