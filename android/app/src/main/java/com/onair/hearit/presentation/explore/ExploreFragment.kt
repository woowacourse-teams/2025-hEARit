package com.onair.hearit.presentation.explore

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.analytics.AnalyticsEventNames
import com.onair.hearit.analytics.AnalyticsParamKeys
import com.onair.hearit.analytics.AnalyticsScreenInfo
import com.onair.hearit.databinding.FragmentExploreBinding
import com.onair.hearit.di.AnalyticsProvider
import com.onair.hearit.di.CrashlyticsProvider
import com.onair.hearit.presentation.PlayerControllerView
import com.onair.hearit.presentation.detail.PlayerDetailActivity

class ExploreFragment :
    Fragment(),
    ShortsClickListener {
    @Suppress("ktlint:standard:backing-property-naming")
    private var _binding: FragmentExploreBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ExploreViewModel by viewModels {
        ExploreViewModelFactory(
            CrashlyticsProvider.get(),
        )
    }

    private val player by lazy { ExoPlayer.Builder(requireContext()).build() }
    private val adapter by lazy { ShortsAdapter(player, this) }
    private val snapHelper = PagerSnapHelper()

    private val playerDetailLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                (activity as? PlayerControllerView)?.apply {
                    pause()
                    hidePlayerControlView()
                }
            }
        }

    private var currentPosition = 0
    private var swipeCount = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentExploreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this

        setupWindowInsets()
        setupRecyclerView()
        observeViewModel()

        (activity as? PlayerControllerView)?.pause()

        player.addListener(
            object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_ENDED) {
                        scrollToNextItem()
                    }
                }
            },
        )
    }

    override fun onResume() {
        super.onResume()
        AnalyticsProvider.get().logScreenView(
            screenName = AnalyticsScreenInfo.Explore.NAME,
            screenClass = AnalyticsScreenInfo.Explore.CLASS,
        )
        if (!player.isPlaying && player.playbackState == Player.STATE_READY) {
            player.play()
        }
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }
    }

    private fun scrollToNextItem() {
        val layoutManager = binding.rvExplore.layoutManager ?: return
        val currentSnapView = snapHelper.findSnapView(layoutManager) ?: return
        val currentPosition = layoutManager.getPosition(currentSnapView)

        val nextPosition = currentPosition + 1
        if (nextPosition < adapter.itemCount) {
            binding.rvExplore.smoothScrollToPosition(nextPosition)
        }
    }

    private fun setupRecyclerView() {
        binding.rvExplore.adapter = adapter
        snapHelper.attachToRecyclerView(binding.rvExplore)

        binding.rvExplore.addOnScrollListener(
            object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(
                    recyclerView: RecyclerView,
                    newState: Int,
                ) {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        val layoutManager =
                            recyclerView.layoutManager as? LinearLayoutManager ?: return
                        val newPosition = layoutManager.findFirstVisibleItemPosition()
                        val snapView = snapHelper.findSnapView(layoutManager) ?: return
                        val position = layoutManager.getPosition(snapView)
                        val item = adapter.currentList.getOrNull(position) ?: return

                        player.setMediaItem(MediaItem.fromUri(item.audioUrl))
                        player.prepare()
                        player.play()

                        swipeCount++
                        currentPosition = newPosition
                        AnalyticsProvider.get().logEvent(
                            AnalyticsEventNames.EXPLORE_SWIPE,
                            mapOf(
                                AnalyticsParamKeys.SWIPE_POSITION to currentPosition.toString(),
                                AnalyticsParamKeys.SWIPE_COUNT to swipeCount.toString(),
                                AnalyticsParamKeys.SCREEN_NAME to AnalyticsScreenInfo.Explore.NAME,
                            ),
                        )

                        checkAndLoadNextPage(position)
                    }
                }
            },
        )
    }

    private fun observeViewModel() {
        viewModel.shortsHearits.observe(viewLifecycleOwner) { shortsHearits ->
            adapter.submitList(shortsHearits)
        }

        viewModel.toastMessage.observe(viewLifecycleOwner) { resId ->
            showToast(getString(resId))
        }
    }

    private fun checkAndLoadNextPage(position: Int) {
        if (position >= adapter.itemCount - 2) {
            viewModel.fetchNextPage()
        }
    }

    private fun showToast(message: String?) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToDetail(
        hearitId: Long,
        lastPosition: Long = 0L,
    ) {
        val intent = PlayerDetailActivity.newIntent(requireActivity(), hearitId, lastPosition)
        playerDetailLauncher.launch(intent)
    }

    override fun onClickHearitInfo(hearitId: Long) {
        val lastPosition = player.currentPosition
        AnalyticsProvider.get().logEvent(
            AnalyticsEventNames.EXPLORE_TO_DETAIL,
            mapOf(
                AnalyticsParamKeys.SOURCE to "explore",
                AnalyticsParamKeys.ITEM_ID to hearitId.toString(),
            ),
        )

        navigateToDetail(hearitId, lastPosition)
    }

    override fun onClickBookmark(hearitId: Long) {
        viewModel.toggleBookmark(hearitId)
    }

    override fun onPause() {
        super.onPause()
        player.pause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }
}
