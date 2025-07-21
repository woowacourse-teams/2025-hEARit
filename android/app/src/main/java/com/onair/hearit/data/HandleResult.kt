package com.onair.hearit.data

import com.onair.hearit.BuildConfig

inline fun <T> handleResult(action: () -> T): Result<T> =
    runCatching(action).onFailure { throwable ->
        if (BuildConfig.DEBUG) throwable.printStackTrace()
    }
