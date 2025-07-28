package com.onair.hearit.data.datasource

import com.onair.hearit.data.dto.KakaoLoginRequest
import com.onair.hearit.data.dto.KakaoLoginResponse

interface AuthRemoteDataSource {
    suspend fun kakaoLogin(kakaoLoginRequest: KakaoLoginRequest): Result<KakaoLoginResponse>
}
