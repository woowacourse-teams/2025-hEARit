package com.onair.hearit.presentation

import android.content.Context
import android.content.Intent
import androidx.fragment.app.FragmentManager
import com.onair.hearit.R
import com.onair.hearit.domain.model.SearchInput
import com.onair.hearit.presentation.IntentKeys.BOOKMARK_ID_KEY
import com.onair.hearit.presentation.IntentKeys.CATEGORY_KEY
import com.onair.hearit.presentation.IntentKeys.EXPLORE_KEY
import com.onair.hearit.presentation.IntentKeys.HEARIT_ID_KEY
import com.onair.hearit.presentation.IntentKeys.KEYWORD_KEY
import com.onair.hearit.presentation.IntentKeys.TYPE_KEY
import com.onair.hearit.presentation.search.category.SearchCategoryFragment
import com.onair.hearit.presentation.search.recent.SearchRecentFragment
import com.onair.hearit.presentation.search.recent.searchResult.SearchResultPageFragment

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
            val fm = mainActivity.supportFragmentManager
            val tag = SearchCategoryFragment::class.java.simpleName

            // 기존 검색결과 Fragment가 있으면 popBackStack으로 지움
            fm.popBackStack(tag, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            fm
                .beginTransaction()
                .replace(
                    R.id.fragment_container_view,
                    SearchCategoryFragment.newInstance(SearchInput.Category(id, name, colorCode)),
                    tag,
                ).addToBackStack(tag)
                .commit()
        }

        is DetailResult.Keyword -> {
            mainActivity.selectTab(R.id.nav_search)
            val fm = mainActivity.supportFragmentManager
            val tag = SearchResultPageFragment::class.java.simpleName

            fm.popBackStack(tag, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            fm
                .beginTransaction()
                .replace(
                    R.id.fragment_container_view,
                    SearchRecentFragment.newInstance(term),
                    tag,
                ).addToBackStack(tag)
                .commit()
        }

        is DetailResult.Explore -> {
            Unit
        }
    }
}
