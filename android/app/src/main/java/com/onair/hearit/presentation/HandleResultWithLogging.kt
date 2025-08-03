package com.onair.hearit.presentation

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
): Result<T> =
    this.fold(
        onSuccess = {
            onSuccess(it)
            Result.success(it)
        },
        onFailure = {
            if (it !is IOException) {
                logger.recordException(it)
            }
            onFailure(it)
            Result.failure(it)
        },
    )
