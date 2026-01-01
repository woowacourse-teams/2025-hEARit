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
import com.onair.hearit.presentation.util.getParcelableCompat
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

    private fun Bundle?.toSearchStartArgs(): SearchStartArgs =
        this?.getParcelableCompat<SearchStartArgs>(ARG_START_ARGS)
            ?: SearchStartArgs()

    companion object {
        private const val ARG_START_ARGS = "startArgs"

        fun newInstance() = SearchFragment()

        fun newInstanceWithCategory(
            categoryId: Long,
            categoryName: String,
            categoryColor: String,
        ) = SearchFragment().apply {
            arguments =
                Bundle().apply {
                    putParcelable(
                        ARG_START_ARGS,
                        SearchStartArgs(
                            initialCategory =
                                CategoryNavModel(
                                    id = categoryId,
                                    name = categoryName,
                                    colorCode = categoryColor,
                                ),
                            isDirectEntry = true,
                        ),
                    )
                }
        }
    }
}
