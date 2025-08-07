package com.onair.hearit.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.onair.hearit.BuildConfig

class FirebaseAnalyticsLogger(
    private val firebaseAnalytics: FirebaseAnalytics,
) : AnalyticsLogger {
    override fun logEvent(
        name: String,
        params: Map<String, String>,
    ) {
        if (BuildConfig.DEBUG) return
        val bundle =
            Bundle().apply {
                params.forEach { (key, value) -> putString(key, value) }
            }
        firebaseAnalytics.logEvent(name, bundle)
    }

    override fun logScreenView(
        screenName: String,
        screenClass: String,
        previousScreen: String?,
    ) {
        if (BuildConfig.DEBUG) return
        val bundle =
            Bundle().apply {
                putString(AnalyticsParamKeys.SCREEN_NAME, screenName)
                putString(AnalyticsParamKeys.SCREEN_CLASS, screenClass)
                previousScreen?.let {
                    putString(AnalyticsParamKeys.PREVIOUS_SCREEN, it)
                }
            }
        firebaseAnalytics.logEvent(AnalyticsEventNames.SCREEN_VIEW, bundle)
    }

    override fun setUserId(userId: String) {
        firebaseAnalytics.setUserId(userId)
    }
}
