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
import com.onair.hearit.presentation.detail.PlayerDetailActivity
import com.onair.hearit.presentation.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CategoryFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                CategorySearchRoute(
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
