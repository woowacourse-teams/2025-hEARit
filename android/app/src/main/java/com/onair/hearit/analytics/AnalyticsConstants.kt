package com.onair.hearit.analytics

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import com.onair.hearit.analytics.AnalyticsConstants.EVENT_SCREEN_VIEW
import com.onair.hearit.analytics.AnalyticsConstants.PARAM_PREVIOUS_SCREEN
import com.onair.hearit.analytics.AnalyticsConstants.PARAM_SCREEN_CLASS
import com.onair.hearit.analytics.AnalyticsConstants.PARAM_SCREEN_NAME

object AnalyticsConstants {
    const val EVENT_SCREEN_VIEW = FirebaseAnalytics.Event.SCREEN_VIEW
    const val EVENT_SEARCH_KEYWORD_SELECTED = "keyword_selected"
    const val EVENT_SEARCH_CATEGORY_SELECTED = "search_category_selected"
    const val EVENT_EXPLORE_TO_DETAIL = "explore_to_detail"
    const val EVENT_EXPLORE_SWIPE = "explore_swipe"

    const val PARAM_SCREEN_NAME = FirebaseAnalytics.Param.SCREEN_NAME
    const val PARAM_SCREEN_CLASS = FirebaseAnalytics.Param.SCREEN_CLASS
    const val PARAM_PREVIOUS_SCREEN = "previous_screen"
    const val PARAM_KEYWORD_NAME = "keyword_name"
    const val PARAM_CATEGORY_NAME = "category_name"
    const val PARAM_SOURCE = "source"
    const val PARAM_ITEM_ID = "item_id"
    const val PARAM_SWIPE_POSITION = "swipe_position"
    const val PARAM_SWIPE_COUNT = "swipe_count"

    const val SCREEN_NAME_HOME = "홈 화면"
    const val SCREEN_CLASS_HOME = "HomeFragment"
    const val SCREEN_NAME_SEARCH = "검색 화면"
    const val SCREEN_CLASS_SEARCH = "SearchFragment"
    const val SCREEN_NAME_EXPLORE = "탐색 화면"
    const val SCREEN_CLASS_EXPLORE = "ExploreFragment"
    const val SCREEN_NAME_LIBRARY = "라이브러리 화면"
    const val SCREEN_CLASS_LIBRARY = "LibraryFragment"
    const val SCREEN_NAME_DETAIL = "상세 재생 화면"
    const val SCREEN_CLASS_DETAIL = "DetailActivity"
}

fun FirebaseAnalytics.logScreenView(
    screenName: String,
    screenClass: String,
    previousScreen: String? = null,
) {
    logEvent(EVENT_SCREEN_VIEW) {
        param(PARAM_SCREEN_NAME, screenName)
        param(PARAM_SCREEN_CLASS, screenClass)
        previousScreen?.let {
            param(PARAM_PREVIOUS_SCREEN, it)
        }
    }
}
