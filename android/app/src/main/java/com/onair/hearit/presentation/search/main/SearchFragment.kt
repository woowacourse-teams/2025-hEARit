package com.onair.hearit.presentation.search.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.compose.rememberNavController
import com.onair.hearit.analytics.AnalyticsLogger
import com.onair.hearit.presentation.main.MainViewModel
import com.onair.hearit.presentation.search.SearchNavHost
import com.onair.hearit.presentation.search.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SearchFragment : Fragment() {
    private val searchViewModel: SearchViewModel by activityViewModels()
    private val mainViewModel: MainViewModel by activityViewModels()

    @Inject
    lateinit var analyticsLogger: AnalyticsLogger

    // 카테고리로 직접 들어왔는지 플래그
    private val isDirectCategoryEntry: Boolean
        get() = arguments?.getBoolean(IS_DIRECT_CATEGORY_ENTRY, false) ?: false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val navController = rememberNavController()

                SearchNavHost(
                    navController = navController,
                    analyticsLogger = analyticsLogger,
                    searchViewModel = searchViewModel,
                    mainViewModel = mainViewModel,
                    onHearitClick = { hearitId ->
                        // Hearit 클릭 처리 (기존 방식 유지 또는 Navigation으로 전환)
                        // 예: 기존 Fragment로 이동하거나 Compose 화면으로 이동
                    },
                    onCategoryBack = {
                        if (isDirectCategoryEntry) {
                            // 홈으로 돌아가기 (Fragment 종료)
                            parentFragmentManager.popBackStack()
                        } else {
                            // 검색 메인으로 돌아가기
                            navController.navigateUp()
                        }
                    },
                )

                // HomeFragment에서 카테고리 정보가 넘어왔다면 자동 이동
                LaunchedEffect(Unit) {
                    arguments?.let { args ->
                        val categoryId = args.getLong(CATEGORY_ID_KEY, -1L)
                        if (categoryId != -1L) {
                            val categoryName = args.getString(CATEGORY_NAME_KEY, "")
                            val categoryColor = args.getString(CATEGORY_COLOR_KEY, "")
                            val encodedColor = categoryColor.removePrefix("#")

                            navController.navigate("category/$categoryId/$categoryName/$encodedColor")
                        }
                    }
                }
            }
        }

    companion object {
        private const val CATEGORY_ID_KEY = "categoryId"
        private const val CATEGORY_NAME_KEY = "categoryName"
        private const val CATEGORY_COLOR_KEY = "categoryColor"
        private const val IS_DIRECT_CATEGORY_ENTRY = "isDirectCategoryEntry"

        fun newInstance() = SearchFragment()

        fun newInstanceWithCategory(
            categoryId: Long,
            categoryName: String,
            categoryColor: String,
        ) = SearchFragment().apply {
            arguments =
                Bundle().apply {
                    putLong(CATEGORY_ID_KEY, categoryId)
                    putString(CATEGORY_NAME_KEY, categoryName)
                    putString(CATEGORY_COLOR_KEY, categoryColor)
                    putBoolean(IS_DIRECT_CATEGORY_ENTRY, true)
                }
        }
    }
}
