package com.onair.hearit.presentation.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
                // 기존 테마 적용
//                SearchNavHost(
//                    onBackClick = {
//                        parentFragmentManager.popBackStack()
//                    },
                SearchMainScreen(
                    viewModel,
                    {},
                    {},
                    onCategoryClick = { id, name, color ->
                        onCategoryClick(id, name, color)
                    },
                )
            }
        }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        setupWindowInsets(view)
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

    private fun setupWindowInsets(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }
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
