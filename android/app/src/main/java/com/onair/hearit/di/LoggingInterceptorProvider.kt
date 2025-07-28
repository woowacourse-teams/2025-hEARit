package com.onair.hearit.di

import android.util.Log
import com.onair.hearit.BuildConfig
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import okhttp3.logging.HttpLoggingInterceptor

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
                            Log.i(
                                "PrettyLogger",
                                json.encodeToString(JsonElement.serializer(), parsed),
                            )
                        }.onFailure {
                            Log.i("PrettyLogger", message)
                        }
                        return
                    }
                    Log.i("PrettyLogger", message)
                }
            },
        ).apply {
            if (BuildConfig.DEBUG) level = HttpLoggingInterceptor.Level.BODY
        }
}
