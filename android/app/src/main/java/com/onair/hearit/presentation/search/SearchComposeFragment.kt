package com.onair.hearit.presentation.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.firebase.analytics.FirebaseAnalytics
import com.onair.hearit.R
import com.onair.hearit.analytics.AnalyticsEventNames
import com.onair.hearit.analytics.AnalyticsParamKeys
import com.onair.hearit.analytics.AnalyticsParamKeys.SCREEN_NAME_SEARCH
import com.onair.hearit.di.AnalyticsProvider
import com.onair.hearit.presentation.IntentKeys.CATEGORY_COLOR_KEY
import com.onair.hearit.presentation.IntentKeys.CATEGORY_ID_KEY
import com.onair.hearit.presentation.IntentKeys.CATEGORY_NAME_KEY
import com.onair.hearit.presentation.search.category.CategoryComposeFragment
import com.onair.hearit.presentation.search.recent.SearchRecentFragment

class SearchComposeFragment :
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
            setContent {
                SearchMainScreen(
                    viewModel,
                    onSearchBarClick = { navigateToRecent() },
                    onCategoryClick = { id, name, color ->
                        onCategoryClick(id, name, color)
                    },
                )
            }
        }

    override fun onResume() {
        super.onResume()
        AnalyticsProvider.get().logEvent(
            FirebaseAnalytics.Event.SCREEN_VIEW,
            mapOf(
                FirebaseAnalytics.Param.SCREEN_NAME to SCREEN_NAME_SEARCH,
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
        AnalyticsProvider.get().logEvent(
            AnalyticsEventNames.SEARCH_CATEGORY_SELECTED,
            mapOf(AnalyticsParamKeys.CATEGORY_NAME to name),
        )

        val fragment =
            CategoryComposeFragment().apply {
                arguments =
                    bundleOf(
                        CATEGORY_ID_KEY to id,
                        CATEGORY_NAME_KEY to name,
                        CATEGORY_COLOR_KEY to colorCode,
                    )
            }

        parentFragmentManager.beginTransaction().apply {
            val currentFragment =
                parentFragmentManager.findFragmentById(R.id.fragment_container_view)
            if (currentFragment != null) hide(currentFragment)

            add(R.id.fragment_container_view, fragment)
            addToBackStack(null)
            commit()
        }
    }
}
