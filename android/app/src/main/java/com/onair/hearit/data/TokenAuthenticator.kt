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
import timber.log.Timber

class TokenAuthenticator(
    private val preferenceProvider: () -> PreferencesLocalDataSource,
    private val authServiceProvider: () -> AuthService,
) : Authenticator {
    // 동시 갱신 방지
    @Volatile
    private var isRefreshing = false

    private val json =
        Json {
            prettyPrint = true
            coerceInputValues = true
        }

    override fun authenticate(
        route: Route?,
        response: Response,
    ): Request? {
        if (response.code == 401) {
            if (isRefreshing) {
                Thread.sleep(1000)
                val currentToken = TokenInterceptorProvider.getAccessToken()
                return if (currentToken != null) {
                    response.request
                        .newBuilder()
                        .header("Authorization", "Bearer $currentToken")
                        .build()
                } else {
                    null
                }
            }

            val errorBody = response.peekBody(Long.MAX_VALUE).string()
            val errorResponse = parseErrorResponse(errorBody)

            return when {
                // 토큰 만료 - 갱신 가능
                errorResponse?.properties?.code == "ACCESS_TOKEN_EXPIRED" && errorResponse.properties.reissuable -> {
                    refreshTokenAndRetry(response.request)
                }

                else -> null
            }
        }
        return null
    }

    private fun refreshTokenAndRetry(originalRequest: Request): Request? {
        if (isRefreshing) return null
        return try {
            val newToken = runBlocking { refreshToken() }
            if (newToken != null) {
                TokenInterceptorProvider.setAccessToken(newToken)
                originalRequest
                    .newBuilder()
                    .header("Authorization", "Bearer $newToken")
                    .build()
            } else {
                handleRefreshFailed()
                null
            }
        } catch (e: Exception) {
            handleRefreshFailed()
            null
        } finally {
            isRefreshing = false
        }
    }

    private fun handleRefreshFailed() {
        // 리프레시 실패시 로그아웃 처리
        TokenInterceptorProvider.setAccessToken(null)
        // 로그아웃 이벤트 발송 등...
    }

    private suspend fun refreshToken(): String? {
        val preferencesLocalDataSource = preferenceProvider()
        val authService = authServiceProvider()
        val refreshToken =
            preferencesLocalDataSource.getRefreshToken().getOrNull() ?: return null
        Timber.d(refreshToken)
        return try {
            val response = authService.postRefreshToken(TokenReissueRequest(refreshToken))
            val tokenResponse = response.body() ?: return null
            preferencesLocalDataSource.saveAccessToken(tokenResponse.accessToken)
            tokenResponse.accessToken
        } catch (e: Exception) {
            preferencesLocalDataSource.clearData()
            null
        }
    }

    private fun parseErrorResponse(errorBody: String?): ErrorResponse? =
        try {
            errorBody?.let { json.decodeFromString<ErrorResponse>(it) }
        } catch (e: Exception) {
            null
        }
}
