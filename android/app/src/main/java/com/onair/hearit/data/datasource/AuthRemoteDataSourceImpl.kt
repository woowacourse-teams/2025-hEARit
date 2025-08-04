package com.onair.hearit.data.datasource

import com.onair.hearit.data.api.AuthService
import com.onair.hearit.data.dto.KakaoLoginRequest
import com.onair.hearit.data.dto.KakaoLoginResponse

class AuthRemoteDataSourceImpl(
    private val authService: AuthService,
    private val errorResponseHandler: ErrorResponseHandler,
) : AuthRemoteDataSource {
    override suspend fun kakaoLogin(kakaoLoginRequest: KakaoLoginRequest): Result<NetworkResult<KakaoLoginResponse>> =
        handleApiCall(
            apiCall = { authService.postLogin(kakaoLoginRequest) },
            transform = { response ->
                response.body() ?: throw IllegalStateException("응답 바디가 null입니다.")
            },
            errorHandler = errorResponseHandler,
        )
}
