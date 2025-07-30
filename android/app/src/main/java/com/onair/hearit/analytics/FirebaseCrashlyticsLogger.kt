package com.onair.hearit.analytics

import com.google.firebase.crashlytics.FirebaseCrashlytics

class FirebaseCrashlyticsLogger(
    private val crashlytics: FirebaseCrashlytics,
) : CrashlyticsLogger {
    override fun recordException(throwable: Throwable) {
        crashlytics.recordException(throwable)
    }

    override fun setUserId(userId: String) {
        crashlytics.setUserId(userId)
    }
}
