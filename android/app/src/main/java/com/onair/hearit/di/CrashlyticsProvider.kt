package com.onair.hearit.di

import com.google.firebase.crashlytics.FirebaseCrashlytics

object CrashlyticsProvider {
    private lateinit var crashlytics: FirebaseCrashlytics

    fun init() {
        crashlytics = FirebaseCrashlytics.getInstance()
    }

    fun get(): FirebaseCrashlytics = crashlytics
}
