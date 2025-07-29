package com.onair.hearit.analytics

import com.google.firebase.analytics.FirebaseAnalytics

object AnalyticsEventNames {
    const val SCREEN_VIEW = FirebaseAnalytics.Event.SCREEN_VIEW
    const val SEARCH_KEYWORD_SELECTED = "keyword_selected"
    const val SEARCH_CATEGORY_SELECTED = "search_category_selected"
    const val EXPLORE_TO_DETAIL = "explore_to_detail"
    const val EXPLORE_SWIPE = "explore_swipe"
}
