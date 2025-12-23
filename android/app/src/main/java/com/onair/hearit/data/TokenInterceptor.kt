package com.onair.hearit.data

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class TokenInterceptor @Inject constructor(
    private val authHeaderProvider: AuthHeaderProvider,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val noAuth: Boolean = original.header(NO_AUTH_KEY) == "true"

        val builder =
            original
                .newBuilder()
                .removeHeader(NO_AUTH_KEY)

        if (!noAuth) {
            authHeaderProvider.getAccessToken()?.let { token ->
                builder.header(AUTH_HEADER_NAME, "$BEARER_PREFIX $token")
            }
            authHeaderProvider.getDeviceUuid()?.let { deviceUuid ->
                builder.header(DEVICE_UUID_HEADER, deviceUuid)
            }
        }
        authHeaderProvider.getAppVersion()?.let { version ->
            builder.header(APP_VERSION_HEADER, version)
        }
        return chain.proceed(builder.build())
    }

    companion object {
        private const val NO_AUTH_KEY: String = "No-Auth"
        private const val AUTH_HEADER_NAME: String = "Authorization"
        private const val BEARER_PREFIX: String = "Bearer"
        private const val DEVICE_UUID_HEADER: String = "Device-Uuid"
        private const val APP_VERSION_HEADER: String = "App-Version"
    }
}
