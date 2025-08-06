package com.onair.hearit.data.api

import com.onair.hearit.data.dto.KakaoLoginRequest
import com.onair.hearit.data.dto.KakaoLoginResponse
import com.onair.hearit.data.dto.TokenReissueRequest
import com.onair.hearit.data.dto.TokenReissueResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthService {
    @POST("auth/kakao-login")
    suspend fun postLogin(
        @Body kakaoLoginRequest: KakaoLoginRequest,
    ): Response<KakaoLoginResponse>

    @POST("auth/token/refresh")
    suspend fun postRefreshToken(
        @Body tokenReissueRequest: TokenReissueRequest,
    ): Response<TokenReissueResponse>

    @GET("auth/check")
    suspend fun getAuthCheck(
        @Header("Authorization") token: String,
    ): Response<Unit>
}
