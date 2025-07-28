package com.onair.hearit.di

import android.app.Application
import com.google.firebase.analytics.FirebaseAnalytics

object AnalyticsProvider {
    private lateinit var analytics: FirebaseAnalytics

    fun init(application: Application) {
        analytics = FirebaseAnalytics.getInstance(application)
    }

    fun get(): FirebaseAnalytics = analytics
}
