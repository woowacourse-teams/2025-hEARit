package com.onair.hearit.di

import com.onair.hearit.BuildConfig
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber

object LoggingInterceptorProvider {
    private val json =
        Json {
            prettyPrint = true
            coerceInputValues = true
        }

    fun provide(): HttpLoggingInterceptor =
        HttpLoggingInterceptor(
            object : HttpLoggingInterceptor.Logger {
                override fun log(message: String) {
                    if (message.startsWith("{") || message.startsWith("[")) {
                        runCatching {
                            val parsed = json.parseToJsonElement(message)
                            Timber.i(json.encodeToString(JsonElement.serializer(), parsed))
                        }.onFailure {
                            Timber.i(message)
                        }
                        return
                    }
                    Timber.i(message)
                }
            },
        ).apply {
            if (BuildConfig.DEBUG) level = HttpLoggingInterceptor.Level.BODY
        }
}
