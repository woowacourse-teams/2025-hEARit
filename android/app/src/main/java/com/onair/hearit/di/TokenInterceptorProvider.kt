package com.onair.hearit.di

import okhttp3.Interceptor

object TokenInterceptorProvider {
    const val NO_AUTH_KEY = "No-Auth"
    const val AUTH_HEADER_NAME = "Authorization"

    private var accessToken: String? = null

    fun provide(): Interceptor =
        Interceptor { chain ->
            val originalRequest = chain.request()

            // No-Auth 헤더가 있으면 인증 헤더 없이 요청 진행
            if (originalRequest.header(NO_AUTH_KEY) != null) {
                val newRequest =
                    originalRequest
                        .newBuilder()
                        .removeHeader(NO_AUTH_KEY)
                        .build()
                return@Interceptor chain.proceed(newRequest)
            }

            // 토큰이 있으면 Authorization 헤더 추가, 없으면 원본 요청 그대로 진행
            accessToken?.let { token ->
                val newRequest =
                    originalRequest
                        .newBuilder()
                        .addHeader(AUTH_HEADER_NAME, "Bearer $token")
                        .build()
                chain.proceed(newRequest)
            } ?: chain.proceed(originalRequest)
        }

    // 앱에서 토큰 업데이트 시 호출 (예: 로그인/토큰 갱신 후)
    fun setAccessToken(token: String?) {
        accessToken = token
    }
}
