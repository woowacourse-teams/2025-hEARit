package com.onair.hearit.analytics

interface CrashlyticsLogger {
    fun recordException(throwable: Throwable)

    fun log(message: String)

    fun setUserId(userId: String)
}
