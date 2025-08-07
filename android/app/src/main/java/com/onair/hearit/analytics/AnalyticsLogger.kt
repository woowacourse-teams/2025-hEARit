package com.onair.hearit.analytics

interface AnalyticsLogger {
    fun logEvent(
        name: String,
        params: Map<String, String> = emptyMap(),
    )

    fun logScreenView(
        screenName: String,
        screenClass: String,
        previousScreen: String? = null,
    )

    fun setUserId(userId: String)
}
