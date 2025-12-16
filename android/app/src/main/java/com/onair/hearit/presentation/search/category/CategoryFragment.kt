package com.onair.hearit.presentation.search.category

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.onair.hearit.domain.model.SearchInput
import com.onair.hearit.presentation.IntentKeys.CATEGORY_COLOR_KEY
import com.onair.hearit.presentation.IntentKeys.CATEGORY_ID_KEY
import com.onair.hearit.presentation.IntentKeys.CATEGORY_NAME_KEY
import com.onair.hearit.presentation.detail.PlayerDetailActivity
import com.onair.hearit.presentation.main.MainActivity
import com.onair.hearit.presentation.main.MainViewModel
import com.onair.hearit.presentation.search.SearchViewModel
import com.onair.hearit.presentation.search.SearchViewModelFactory
import com.onair.hearit.presentation.search.category.component.CategorySearchRoute

class CategoryFragment : Fragment() {
    private val category by lazy {
        SearchInput.Category(
            arguments?.getLong(CATEGORY_ID_KEY) ?: -1L,
            arguments?.getString(CATEGORY_NAME_KEY) ?: "카테고리",
            arguments?.getString(CATEGORY_COLOR_KEY) ?: "#000000",
        )
    }

    private val mainViewModel: MainViewModel by activityViewModels()

    private val viewModel: SearchViewModel by viewModels {
        SearchViewModelFactory(category)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                CategorySearchRoute(
                    viewModel = viewModel,
                    mainViewModel = mainViewModel,
                    onBack = { parentFragmentManager.popBackStack() },
                    onHearitClick = { heartId -> onHearitClick(heartId) },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

    private fun onHearitClick(hearitId: Long) {
        val intent = PlayerDetailActivity.newIntent(requireActivity(), hearitId)
        (activity as? MainActivity)?.launchDetailActivity(intent)
    }
}
