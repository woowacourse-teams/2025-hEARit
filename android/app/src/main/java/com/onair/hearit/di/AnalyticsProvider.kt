package com.onair.hearit.di

import android.app.Application
import com.google.firebase.analytics.FirebaseAnalytics
import com.onair.hearit.analytics.AnalyticsLogger
import com.onair.hearit.analytics.FirebaseAnalyticsLogger

object AnalyticsProvider {
    private lateinit var instance: AnalyticsLogger

    fun init(application: Application) {
        val firebaseAnalytics = FirebaseAnalytics.getInstance(application)
        instance = FirebaseAnalyticsLogger(firebaseAnalytics)
    }

    fun get(): AnalyticsLogger = instance
}
