package com.onair.hearit.data.api

import com.onair.hearit.data.dto.KakaoLoginRequest
import com.onair.hearit.data.dto.KakaoLoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("auth/kakao-login")
    suspend fun postLogin(
        @Body kakaoLoginRequest: KakaoLoginRequest,
    ): Response<KakaoLoginResponse>
}
