package com.onair.hearit.analytics

interface CrashlyticsLogger {
    fun recordException(throwable: Throwable)

    fun setUserId(userId: String)
}
