package com.onair.hearit.data

class AuthRemoteDataSourceImpl(
    private val authService: AuthService,
) : AuthRemoteDataSource {
    override suspend fun login(loginRequest: LoginRequest): Result<LoginResponse> =
        handleApiCall(
            errorMessage = "카카오 로그인 실패",
            apiCall = { authService.postLogin(loginRequest) },
            transform = { response ->
                response.body() ?: throw IllegalStateException("응답 바디가 null입니다.")
            },
        )
}
