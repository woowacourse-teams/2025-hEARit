package com.onair.hearit.di

import okhttp3.Interceptor

object TokenInterceptorProvider {
    const val NO_AUTH_KEY = "No-Auth"
    const val AUTH_HEADER_NAME = "Authorization"

    @Volatile
    private var accessToken: String? = null

    fun provide(): Interceptor =
        Interceptor { chain ->
            val originalRequest = chain.request()

            if (originalRequest.header(NO_AUTH_KEY) != null) {
                val newRequest =
                    originalRequest
                        .newBuilder()
                        .removeHeader(NO_AUTH_KEY)
                        .build()
                return@Interceptor chain.proceed(newRequest)
            }

            accessToken?.let { token ->
                val newRequest =
                    originalRequest
                        .newBuilder()
                        .addHeader(AUTH_HEADER_NAME, "Bearer $token")
                        .build()
                chain.proceed(newRequest)
            } ?: chain.proceed(originalRequest)
        }

    fun setAccessToken(token: String?) {
        accessToken = token
    }
}
