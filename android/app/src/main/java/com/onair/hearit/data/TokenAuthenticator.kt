package com.onair.hearit.data

import com.onair.hearit.data.api.AuthService
import com.onair.hearit.data.datasource.local.PreferencesLocalDataSource
import com.onair.hearit.data.dto.TokenReissueRequest
import com.onair.hearit.di.TokenInterceptorProvider
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(
    private val preferenceProvider: () -> PreferencesLocalDataSource,
    private val authServiceProvider: () -> AuthService,
) : Authenticator {
    private val json =
        Json {
            prettyPrint = true
            coerceInputValues = true
        }

    override fun authenticate(
        route: Route?,
        response: Response,
    ): Request? {
        if (response.request.header("X-Retry-Attempt") == "1") return null
        if (response.request.header("No-Auth") == "true") return null

        if (response.code == 401) {
            val errorBody = response.peekBody(Long.MAX_VALUE).string()
            val errorResponse = parseErrorResponse(errorBody)

            return when {
                // 토큰 만료 - 갱신 가능
                errorResponse?.reissuable == true -> {
                    refreshTokenAndRetry(response.request)
                }

                else -> null
            }
        }
        return null
    }

    private fun refreshTokenAndRetry(originalRequest: Request): Request? =
        try {
            val newToken = runBlocking { refreshToken() }
            if (newToken != null) {
                TokenInterceptorProvider.setAccessToken(newToken)
                originalRequest
                    .newBuilder()
                    .header("Authorization", "Bearer $newToken")
                    .header("X-Retry-Attempt", "1")
                    .build()
            } else {
                handleRefreshFailed()
                null
            }
        } catch (_: Exception) {
            handleRefreshFailed()
            null
        }

    private fun handleRefreshFailed() {
        // 리프레시 실패시 로그아웃 처리
        TokenInterceptorProvider.setAccessToken(null)
        AuthEventManager.sendLogoutEvent()
    }

    private suspend fun refreshToken(): String? {
        val preferencesLocalDataSource = preferenceProvider()
        val authService = authServiceProvider()
        val refreshToken =
            preferencesLocalDataSource.getRefreshToken().getOrNull() ?: return null
        return try {
            val response = authService.postRefreshToken(TokenReissueRequest(refreshToken))
            val tokenResponse = response.body() ?: return null
            preferencesLocalDataSource.saveAccessToken(tokenResponse.accessToken)
            tokenResponse.accessToken
        } catch (_: Exception) {
            preferencesLocalDataSource.clearData()
            null
        }
    }

    private fun parseErrorResponse(errorBody: String?): ErrorResponse? =
        try {
            errorBody?.let { json.decodeFromString<ErrorResponse>(it) }
        } catch (_: Exception) {
            null
        }
}
