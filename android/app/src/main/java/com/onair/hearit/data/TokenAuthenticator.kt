package com.onair.hearit.data

import com.onair.hearit.data.api.AuthService
import com.onair.hearit.data.datasource.local.AuthLocalDataSource
import com.onair.hearit.data.dto.TokenReissueRequest
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Named

class TokenAuthenticator @Inject constructor(
    private val authLocalDataSource: AuthLocalDataSource,
    @param:Named("noAuth") private val authService: AuthService,
    private val authHeaderProvider: AuthHeaderProvider,
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

                else -> {
                    null
                }
            }
        }
        return null
    }

    private fun refreshTokenAndRetry(originalRequest: Request): Request? =
        try {
            val newToken = runBlocking { refreshToken() }
            if (newToken != null) {
                authHeaderProvider.updateAccessToken(newToken)
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
        authHeaderProvider.updateAccessToken(null)
        AuthEventManager.sendLogoutEvent()
    }

    private suspend fun refreshToken(): String? {
        val refreshToken =
            authLocalDataSource.getRefreshToken().getOrNull() ?: return null
        return try {
            val response = authService.postRefreshToken(TokenReissueRequest(refreshToken))
            val tokenResponse =
                response.body() ?: run {
                    authLocalDataSource.clearAuthData()
                    return null
                }
            authLocalDataSource.saveAccessToken(tokenResponse.accessToken)
            tokenResponse.accessToken
        } catch (_: Exception) {
            authLocalDataSource.clearAuthData()
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
