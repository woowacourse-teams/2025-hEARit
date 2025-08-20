package com.onair.hearit.analytics

interface AnalyticsLogger {
    fun logEvent(
        name: String,
        params: Map<String, String> = emptyMap(),
    )

    fun setUserId(userId: String)
}
