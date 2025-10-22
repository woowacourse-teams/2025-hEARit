package com.onair.hearit.data.api

import com.onair.hearit.data.dto.KakaoLoginRequest
import com.onair.hearit.data.dto.KakaoLoginResponse
import com.onair.hearit.data.dto.TokenReissueRequest
import com.onair.hearit.data.dto.TokenReissueResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthService {
    @GET("api/v1/auth/check")
    suspend fun getAuthCheck(
        @Header("Authorization") accessToken: String,
    ): Response<Unit>

    @POST("api/v1/auth/kakao-login")
    suspend fun postLogin(
        @Body kakaoLoginRequest: KakaoLoginRequest,
    ): Response<KakaoLoginResponse>

    @POST("api/v1/auth/token/refresh")
    suspend fun postRefreshToken(
        @Body tokenReissueRequest: TokenReissueRequest,
    ): Response<TokenReissueResponse>

    @DELETE("api/v1/auth/withdraw")
    suspend fun deleteAccount(): Response<Unit>
}
