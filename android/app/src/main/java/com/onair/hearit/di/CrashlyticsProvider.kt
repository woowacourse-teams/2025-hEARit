package com.onair.hearit.di

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.onair.hearit.analytics.CrashlyticsLogger
import com.onair.hearit.analytics.FirebaseCrashlyticsLogger

object CrashlyticsProvider {
    private val logger: CrashlyticsLogger by lazy {
        FirebaseCrashlyticsLogger(FirebaseCrashlytics.getInstance())
    }

    fun get(): CrashlyticsLogger = logger
}
