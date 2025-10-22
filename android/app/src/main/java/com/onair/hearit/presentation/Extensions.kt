package com.onair.hearit.presentation

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.concurrent.futures.CallbackToFutureAdapter
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.google.common.util.concurrent.ListenableFuture
import com.onair.hearit.R
import com.onair.hearit.analytics.AnalyticsEventNames
import com.onair.hearit.analytics.AnalyticsParamKeys
import com.onair.hearit.di.AnalyticsProvider
import com.onair.hearit.domain.model.Keyword
import com.onair.hearit.presentation.IntentKeys.CATEGORY_COLOR_KEY
import com.onair.hearit.presentation.IntentKeys.CATEGORY_ID_KEY
import com.onair.hearit.presentation.IntentKeys.CATEGORY_KEY
import com.onair.hearit.presentation.IntentKeys.CATEGORY_NAME_KEY
import com.onair.hearit.presentation.IntentKeys.KEYWORD_KEY
import com.onair.hearit.presentation.IntentKeys.TYPE_KEY
import com.onair.hearit.presentation.main.MainActivity
import com.onair.hearit.presentation.search.category.CategoryComposeFragment
import com.onair.hearit.presentation.search.recent.SearchRecentFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume

fun Int.dpToPx(context: Context): Int = (this * context.resources.displayMetrics.density).toInt()

fun Int.pxToDp(context: Context): Int = (this / context.resources.displayMetrics.density).toInt()

fun Int.toTimeString(): String {
    val minutes = this / 60
    val seconds = this % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}

fun Keyword.toHashtagName(): String = "#${this.name}"

fun Intent?.toDetailResult(): DetailResult? {
    if (this == null) return null
    return when (getStringExtra(TYPE_KEY)) {
        CATEGORY_KEY -> {
            extras?.let { DetailResult.Category.fromBundle(it) }
        }

        KEYWORD_KEY -> {
            extras?.let { DetailResult.Keyword.fromBundle(it) }
        }

        else -> null
    }
}

fun DetailResult.navigate(mainActivity: MainActivity) {
    when (this) {
        is DetailResult.Category -> {
            val fragmentManager = mainActivity.supportFragmentManager
            val backStackTag = CategoryComposeFragment::class.java.simpleName

            // 기존 검색결과 Fragment가 있으면 popBackStack으로 지움
            fragmentManager.popBackStack(backStackTag, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            fragmentManager
                .beginTransaction()
                .replace(
                    R.id.fragment_container_view,
                    CategoryComposeFragment().apply {
                        arguments =
                            bundleOf(
                                CATEGORY_ID_KEY to categoryId,
                                CATEGORY_NAME_KEY to name,
                                CATEGORY_COLOR_KEY to colorCode,
                            )
                    },
                    backStackTag,
                ).addToBackStack(backStackTag)
                .commit()

            AnalyticsProvider.get().logEvent(
                AnalyticsEventNames.SEARCH_CATEGORY_SELECTED,
                mapOf(AnalyticsParamKeys.ITEM_NAME to name),
            )
        }

        is DetailResult.Keyword -> {
            mainActivity.selectTab(R.id.nav_search)
            val fragmentManager = mainActivity.supportFragmentManager
            val backStackTag = SearchRecentFragment::class.java.simpleName

            fragmentManager.popBackStack(backStackTag, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            fragmentManager
                .beginTransaction()
                .replace(
                    R.id.fragment_container_view,
                    SearchRecentFragment.newInstance(term),
                    backStackTag,
                ).addToBackStack(backStackTag)
                .commit()

            AnalyticsProvider.get().logEvent(
                AnalyticsEventNames.SEARCH_KEYWORD_ENTERED,
                mapOf(AnalyticsParamKeys.ITEM_NAME to term),
            )
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

fun <T> LiveData<T>.observeOnce(
    lifecycleOwner: LifecycleOwner,
    observer: Observer<T>,
) {
    observe(
        lifecycleOwner,
        object : Observer<T> {
            override fun onChanged(value: T) {
                observer.onChanged(value)
                removeObserver(this)
            }
        },
    )
}

fun <T> executeAsync(
    serviceScope: CoroutineScope,
    operationName: String,
    operation: suspend () -> T,
): ListenableFuture<T> =
    CallbackToFutureAdapter.getFuture { completer ->
        val job =
            serviceScope.launch {
                runCatching { operation() }
                    .onSuccess { completer.set(it) }
                    .onFailure { completer.setException(it) }
            }
        completer.addCancellationListener({ job.cancel() }, Runnable::run)
        operationName
    }

inline fun <reified T : Parcelable> Bundle.getParcelableCompat(key: String): T =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelable(key, T::class.java)
            ?: error("$key 데이터를 찾을 수 없습니다.")
    } else {
        @Suppress("DEPRECATION")
        getParcelable(key) as? T
            ?: error("$key 데이터를 찾을 수 없습니다.")
    }
