package com.onair.hearit.presentation.explore

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import com.onair.hearit.analytics.AnalyticsLogger
import com.onair.hearit.presentation.DetailResult
import com.onair.hearit.presentation.IntentKeys.PREVIOUS_SCREEN_KEY
import com.onair.hearit.presentation.IntentValues.EXPLORE_VALUE
import com.onair.hearit.presentation.PlayerControllerView
import com.onair.hearit.presentation.detail.PlayerDetailActivity
import com.onair.hearit.presentation.logNavigationEvent
import com.onair.hearit.presentation.toDetailResult
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class ExploreFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                ExploreRoute(
                    onBackClick = {
                        parentFragmentManager.popBackStack()
                    },
                    onNavigateToDetail = { id, position ->
                        navigateToDetail(id, position)
                    },
                )
            }
        }

    @Inject
    lateinit var analyticsLogger: AnalyticsLogger

    // 상세 페이지에서 돌아왔을 때의 결과를 처리하기 위한 런처
    private val playerDetailLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult

            (activity as? PlayerControllerView)?.apply {
                pause()
                hidePlayerControlView()

                when (val detailResult = result.data.toDetailResult()) {
                    is DetailResult.Category, is DetailResult.Keyword -> {
                        detailResult.logNavigationEvent(analyticsLogger)
                    }

                    null -> {
                        Timber.w("Invalid detail result")
                    }
                }
            }
        }

    private fun navigateToDetail(
        hearitId: Long,
        lastPosition: Long? = null,
    ) {
        val intent =
            PlayerDetailActivity.newIntent(requireActivity(), hearitId, lastPosition).apply {
                putExtra(PREVIOUS_SCREEN_KEY, EXPLORE_VALUE)
            }
        playerDetailLauncher.launch(intent)
    }
}
