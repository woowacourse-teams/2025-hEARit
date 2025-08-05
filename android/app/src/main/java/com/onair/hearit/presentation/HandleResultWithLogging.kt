package com.onair.hearit.presentation

import com.onair.hearit.BuildConfig
import com.onair.hearit.analytics.CrashlyticsLogger
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.IOException

fun CoroutineScope.launchWithLogging(
    logger: CrashlyticsLogger,
    block: suspend CoroutineScope.() -> Unit,
) {
    val handler =
        CoroutineExceptionHandler { _, throwable ->
            logger.recordException(throwable)
        }

    this.launch(handler, block = block)
}

inline fun <T> Result<T>.foldWithCrashlytics(
    logger: CrashlyticsLogger,
    onSuccess: (T) -> Unit,
    onFailure: (Throwable) -> Unit,
) = this.fold(
    onSuccess = { onSuccess(it) },
    onFailure = {
        if (BuildConfig.DEBUG) {
            it.printStackTrace()
        }
        if (it !is IOException) {
            logger.recordException(it)
        }
        onFailure(it)
    },
)
