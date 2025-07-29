package com.onair.hearit.di

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.onair.hearit.analytics.CrashlyticsLogger
import com.onair.hearit.analytics.FirebaseCrashlyticsLogger
import kotlin.math.log

object CrashlyticsProvider {
    private var logger: CrashlyticsLogger? = null

    fun init() {
        logger = FirebaseCrashlyticsLogger(FirebaseCrashlytics.getInstance())
    }

    fun get(): CrashlyticsLogger = logger ?: throw IllegalStateException("CrashlyticsProvider not initialized")
}
