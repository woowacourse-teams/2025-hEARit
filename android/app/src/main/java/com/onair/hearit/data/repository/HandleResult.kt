package com.onair.hearit.data.repository

import com.onair.hearit.BuildConfig
import com.onair.hearit.di.CrashlyticsProvider

inline fun <T> handleResult(action: () -> T): Result<T> =
    runCatching(action).onFailure { throwable ->
        if (BuildConfig.DEBUG) {
            throwable.printStackTrace()
        } else {
            CrashlyticsProvider.get().recordException(throwable)
        }
    }
