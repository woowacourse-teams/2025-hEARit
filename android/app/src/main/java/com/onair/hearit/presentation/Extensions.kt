package com.onair.hearit.presentation

import android.content.Context
import android.content.Intent
import com.onair.hearit.R
import com.onair.hearit.domain.model.SearchInput
import com.onair.hearit.presentation.IntentKeys.BOOKMARK_ID_KEY
import com.onair.hearit.presentation.IntentKeys.CATEGORY_KEY
import com.onair.hearit.presentation.IntentKeys.EXPLORE_KEY
import com.onair.hearit.presentation.IntentKeys.HEARIT_ID_KEY
import com.onair.hearit.presentation.IntentKeys.KEYWORD_KEY
import com.onair.hearit.presentation.IntentKeys.TYPE_KEY
import com.onair.hearit.presentation.search.SearchFragment
import com.onair.hearit.presentation.search.category.SearchCategoryFragment

fun Int.dpToPx(context: Context): Int = (this * context.resources.displayMetrics.density).toInt()

fun Int.pxToDp(context: Context): Int = (this / context.resources.displayMetrics.density).toInt()

fun Intent?.toDetailResult(): DetailResult? {
    if (this == null) return null
    return when (getStringExtra(TYPE_KEY)) {
        EXPLORE_KEY -> {
            val hearitId = getLongExtra(HEARIT_ID_KEY, -1)
            if (hearitId == -1L) return null
            val bookmarkId = getLongExtra(BOOKMARK_ID_KEY, -1L).takeIf { it != -1L }
            DetailResult.Explore(hearitId, bookmarkId)
        }

        CATEGORY_KEY -> {
            extras?.let { DetailResult.Category(it) }
        }

        KEYWORD_KEY -> {
            extras?.let { DetailResult.Keyword(it) }
        }

        else -> null
    }
}

fun DetailResult.navigate(mainActivity: MainActivity) {
    when (this) {
        is DetailResult.Category -> {
            mainActivity.selectTab(R.id.nav_search)
            val searchCategoryFragment =
                SearchCategoryFragment.newInstance(SearchInput.Category(id, name, colorCode))

            mainActivity.supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container_view, searchCategoryFragment)
                .addToBackStack(null)
                .commit()
        }

        is DetailResult.Keyword -> {
            mainActivity.selectTab(R.id.nav_search)
            val searchFragment = SearchFragment().apply { arguments = this@navigate.bundle }
            mainActivity.supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container_view, searchFragment)
                .addToBackStack(null)
                .commit()
        }

        is DetailResult.Explore -> {
            Unit
        }
    }
}
