package com.onair.hearit.presentation.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import com.onair.hearit.analytics.AnalyticsLogger
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

val LocalAnalyticsLogger =
    staticCompositionLocalOf<AnalyticsLogger> {
        error("AnalyticsLogger not provided")
    }

@AndroidEntryPoint
class SearchFragment : Fragment() {
    @Inject
    lateinit var analyticsLogger: AnalyticsLogger

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                CompositionLocalProvider(
                    LocalAnalyticsLogger provides analyticsLogger,
                ) {
                    SearchNavHost(
                        startArgs = arguments.toSearchStartArgs(),
                        onExitSearch = { parentFragmentManager.popBackStack() },
                        onHearitClick = { hearitId ->
                            // Hearit 클릭 처리 (기존 방식 유지 또는 Navigation으로 전환)
                            // 예: 기존 Fragment로 이동하거나 Compose 화면으로 이동
                        },
                    )
                }
            }
        }

    private fun Bundle?.toSearchStartArgs(): SearchStartArgs {
        if (this == null) return SearchStartArgs()

        val categoryId = getLong(ARG_CATEGORY_ID, -1L)
        if (categoryId == -1L) return SearchStartArgs()

        return SearchStartArgs(
            initialCategory =
                SearchRoute.Category(
                    id = categoryId,
                    name = getString(ARG_CATEGORY_NAME) ?: "",
                    colorCode = getString(ARG_CATEGORY_COLOR) ?: "",
                ),
            isDirectEntry = getBoolean(ARG_IS_DIRECT_ENTRY, false),
        )
    }

    companion object {
        private const val ARG_CATEGORY_ID = "category_id"
        private const val ARG_CATEGORY_NAME = "category_name"
        private const val ARG_CATEGORY_COLOR = "category_color"
        private const val ARG_IS_DIRECT_ENTRY = "is_direct_entry"

        fun newInstance() = SearchFragment()

        fun newInstanceWithCategory(
            categoryId: Long,
            categoryName: String,
            categoryColor: String,
        ) = SearchFragment().apply {
            arguments =
                Bundle().apply {
                    putLong(ARG_CATEGORY_ID, categoryId)
                    putString(ARG_CATEGORY_NAME, categoryName)
                    putString(ARG_CATEGORY_COLOR, categoryColor)
                    putBoolean(ARG_IS_DIRECT_ENTRY, true)
                }
        }
    }
}
