package com.onair.hearit.data

class AuthRemoteDataSourceImpl(
    private val authService: AuthService,
) : AuthRemoteDataSource {
    override suspend fun kakaoLogin(kakaoLoginRequest: KakaoLoginRequest): Result<KakaoLoginResponse> =
        handleApiCall(
            errorMessage = "카카오 로그인 실패",
            apiCall = { authService.postLogin(kakaoLoginRequest) },
            transform = { response ->
                response.body() ?: throw IllegalStateException("응답 바디가 null입니다.")
            },
        )
}
