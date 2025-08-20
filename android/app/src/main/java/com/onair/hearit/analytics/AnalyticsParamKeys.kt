package com.onair.hearit.analytics

import com.google.firebase.analytics.FirebaseAnalytics

object AnalyticsParamKeys {
    const val SCREEN_NAME = FirebaseAnalytics.Param.SCREEN_NAME
    const val SCREEN_CLASS = FirebaseAnalytics.Param.SCREEN_CLASS
    const val PREVIOUS_SCREEN = "previous_screen"
    const val CATEGORY_NAME = "category_name"
    const val KEYWORD_NAME = "keyword_name"
    const val ITEM_ID = "item_id"
}
