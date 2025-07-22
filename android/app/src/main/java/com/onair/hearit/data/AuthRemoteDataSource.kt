package com.onair.hearit.data

interface AuthRemoteDataSource {
    suspend fun login(loginRequest: LoginRequest): Result<LoginResponse>
}
