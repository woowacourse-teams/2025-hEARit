package com.onair.hearit.di

import okhttp3.Interceptor

object TokenInterceptorProvider {
    private const val NO_AUTH_KEY = "No-Auth"
    private const val AUTH_HEADER_NAME = "Authorization"
    private const val BEARER_PREFIX = "Bearer"
    private const val DEVICE_UUID_HEADER = "Device-Uuid"

    @Volatile
    private var accessToken: String? = null

    @Volatile
    private var deviceUuid: String? = null

    fun provide(): Interceptor =
        Interceptor { chain ->
            val original = chain.request()
            val noAuth = original.header(NO_AUTH_KEY) == "true"

            val builder =
                original
                    .newBuilder()
                    .removeHeader(NO_AUTH_KEY)

            if (!noAuth) {
                accessToken?.let { token ->
                    builder.header(AUTH_HEADER_NAME, "$BEARER_PREFIX $token")
                }
                deviceUuid?.let { builder.header(DEVICE_UUID_HEADER, it) }
            }

            chain.proceed(builder.build())
        }

    fun setAccessToken(token: String?) {
        accessToken = token
    }

    fun setDeviceUuid(uuid: String?) {
        deviceUuid = uuid
    }
}
