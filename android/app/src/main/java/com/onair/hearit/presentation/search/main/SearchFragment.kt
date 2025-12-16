package com.onair.hearit.presentation.search.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.firebase.analytics.FirebaseAnalytics
import com.onair.hearit.R
import com.onair.hearit.analytics.AnalyticsParamKeys
import com.onair.hearit.di.AnalyticsProvider
import com.onair.hearit.presentation.IntentKeys
import com.onair.hearit.presentation.search.CategoryClickListener
import com.onair.hearit.presentation.search.SearchViewModel
import com.onair.hearit.presentation.search.SearchViewModelFactory
import com.onair.hearit.presentation.search.category.CategoryFragment
import com.onair.hearit.presentation.search.main.screen.SearchMainScreen
import com.onair.hearit.presentation.search.recent.SearchRecentFragment

class SearchFragment :
    Fragment(),
    CategoryClickListener {
    private val viewModel: SearchViewModel by viewModels {
        SearchViewModelFactory(null)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                SearchMainScreen(
                    viewModel,
                    onSearchBarClick = { navigateToRecent() },
                    onCategoryClick = ::onCategoryClick,
                )
            }
        }

    override fun onResume() {
        super.onResume()
        AnalyticsProvider.get().logEvent(
            FirebaseAnalytics.Event.SCREEN_VIEW,
            mapOf(
                FirebaseAnalytics.Param.SCREEN_NAME to AnalyticsParamKeys.SCREEN_NAME_SEARCH,
                FirebaseAnalytics.Param.SCREEN_CLASS to this::class.simpleName.orEmpty(),
            ),
        )
    }

    private fun navigateToRecent() {
        parentFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container_view, SearchRecentFragment())
            .addToBackStack(null)
            .commit()
    }

    override fun onCategoryClick(
        id: Long,
        name: String,
        colorCode: String,
    ) {
        val fragment =
            CategoryFragment().apply {
                arguments =
                    bundleOf(
                        IntentKeys.CATEGORY_ID_KEY to id,
                        IntentKeys.CATEGORY_NAME_KEY to name,
                        IntentKeys.CATEGORY_COLOR_KEY to colorCode,
                    )
            }

        parentFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container_view, fragment)
            .addToBackStack(null)
            .commit()
    }
}
