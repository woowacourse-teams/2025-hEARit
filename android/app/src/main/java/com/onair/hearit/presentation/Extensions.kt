package com.onair.hearit.presentation

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import com.onair.hearit.R
import com.onair.hearit.domain.model.SearchInput
import com.onair.hearit.presentation.IntentKeys.BOOKMARK_ID_KEY
import com.onair.hearit.presentation.IntentKeys.CATEGORY_KEY
import com.onair.hearit.presentation.IntentKeys.EXPLORE_KEY
import com.onair.hearit.presentation.IntentKeys.HEARIT_ID_KEY
import com.onair.hearit.presentation.IntentKeys.KEYWORD_KEY
import com.onair.hearit.presentation.IntentKeys.TYPE_KEY
import com.onair.hearit.presentation.main.MainActivity
import com.onair.hearit.presentation.search.category.SearchCategoryFragment
import com.onair.hearit.presentation.search.recent.SearchRecentFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

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
            val tag = SearchRecentFragment::class.java.simpleName

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

private suspend fun View.awaitAlpha(
    target: Float,
    duration: Long,
) = suspendCancellableCoroutine { cont ->
    // suspendCancellableCoroutine을 사용해 코루틴을 일시 중단하고, 취소될 때 작업을 처리
    animate()
        .alpha(target)
        .setDuration(duration.coerceAtLeast(0L))
        .withEndAction { if (cont.isActive) cont.resume(Unit) } // 애니메이션이 끝났을 때 코루틴을 재개
        .start()
    cont.invokeOnCancellation { animate().cancel() } // 코루틴이 취소되면 진행 중인 애니메이션을 취소
}

private fun View.show(initialAlpha: Float = 0f) {
    isVisible = true
    alpha = initialAlpha
}

private fun View.hideAndReset() {
    animate().cancel()
    isVisible = false
    alpha = 1f
}

// 페이드 인 페이드 아웃 애니메이션을 위한 확장 함수
@OptIn(ExperimentalCoroutinesApi::class)
fun View.flash(
    scope: CoroutineScope,
    ms: Long = 2000L, // 뷰가 완전히 보이는 상태로 유지될 시간
    fade: Long = 100L, // 페이드 인/아웃 애니메이션 시간
): Job {
    animate().cancel() // 이전에 실행 중이던 애니메이션이 있다면 취소

    return scope.launch {
        try {
            show(0f)
            awaitAlpha(1f, fade)
            delay(ms)
            awaitAlpha(0f, fade)
        } finally {
            hideAndReset()
        }
    }
}

fun View.hideFlashImmediately() = hideAndReset()
