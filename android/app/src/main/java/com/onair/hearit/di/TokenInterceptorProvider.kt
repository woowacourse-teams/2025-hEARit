package com.onair.hearit.di

import okhttp3.Interceptor

object TokenInterceptorProvider {
    const val NO_AUTH_KEY = "No-Auth"
    const val AUTH_HEADER_NAME = "Authorization"

    private var accessToken: String? = null

    fun provide() =
        Interceptor { chain ->
            val originalRequest = chain.request()

            if (originalRequest.header(NO_AUTH_KEY) != null) {
                val newRequest =
                    originalRequest
                        .newBuilder()
                        .removeHeader(NO_AUTH_KEY)
                        .build()
                chain.proceed(newRequest)
            } else {
                val token = accessToken
                val newRequest =
                    if (token != null) {
                        originalRequest
                            .newBuilder()
                            .addHeader(AUTH_HEADER_NAME, token)
                            .build()
                    } else {
                        originalRequest
                    }
                chain.proceed(newRequest)
            }
        }

    // 앱에서 토큰 업데이트 시 호출 (예: 로그인/토큰 갱신 후)
    fun setAccessToken(token: String?) {
        accessToken = token
    }
}
