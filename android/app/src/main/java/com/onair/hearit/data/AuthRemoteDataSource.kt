package com.onair.hearit.data

interface AuthRemoteDataSource {
    suspend fun kakaoLogin(kakaoLoginRequest: KakaoLoginRequest): Result<KakaoLoginResponse>
}
