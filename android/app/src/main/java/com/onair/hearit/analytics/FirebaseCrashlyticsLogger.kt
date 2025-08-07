package com.onair.hearit.analytics

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.onair.hearit.BuildConfig

class FirebaseCrashlyticsLogger(
    private val crashlytics: FirebaseCrashlytics,
) : CrashlyticsLogger {
    override fun recordException(throwable: Throwable) {
        if (BuildConfig.DEBUG) return
        crashlytics.recordException(throwable)
    }

    override fun log(message: String) {
        if (BuildConfig.DEBUG) return
        crashlytics.log(message)
    }

    override fun setUserId(userId: String) {
        if (BuildConfig.DEBUG) return
        crashlytics.setUserId(userId)
    }
}
