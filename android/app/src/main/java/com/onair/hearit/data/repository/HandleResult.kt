package com.onair.hearit.data.repository

import com.onair.hearit.BuildConfig
import com.onair.hearit.analytics.CrashlyticsLogger

inline fun <T> handleResult(
    crashlyticsLogger: CrashlyticsLogger,
    action: () -> T,
): Result<T> =
    runCatching(action).onFailure { throwable ->
        if (BuildConfig.DEBUG) {
            throwable.printStackTrace()
        } else {
            crashlyticsLogger.recordException(throwable)
        }
    }
