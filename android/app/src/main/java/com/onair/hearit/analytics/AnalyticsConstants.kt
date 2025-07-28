package com.onair.hearit.analytics

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent

object AnalyticsConstants {
    const val EVENT_SCREEN_VIEW = FirebaseAnalytics.Event.SCREEN_VIEW
    const val PARAM_SCREEN_NAME = FirebaseAnalytics.Param.SCREEN_NAME
    const val PARAM_SCREEN_CLASS = FirebaseAnalytics.Param.SCREEN_CLASS

    const val SCREEN_NAME_HOME = "홈 화면"
    const val SCREEN_CLASS_HOME = "HomeFragment"
    const val SCREEN_NAME_SEARCH = "검색 화면"
    const val SCREEN_CLASS_SEARCH = "SearchFragment"
    const val SCREEN_NAME_EXPLORE = "탐색 화면"
    const val SCREEN_CLASS_EXPLORE = "ExploreFragment"
    const val SCREEN_NAME_LIBRARY = "라이브러리 화면"
    const val SCREEN_CLASS_LIBRARY = "LibraryFragment"
}

fun FirebaseAnalytics.logScreenView(
    screenName: String,
    screenClass: String,
) {
    logEvent(AnalyticsConstants.EVENT_SCREEN_VIEW) {
        param(AnalyticsConstants.PARAM_SCREEN_NAME, screenName)
        param(AnalyticsConstants.PARAM_SCREEN_CLASS, screenClass)
    }
}
