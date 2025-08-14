package com.onair.hearit.presentation.explore

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
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
import com.onair.hearit.presentation.LoginRequiredDialogFragment
import com.onair.hearit.presentation.PlayerControllerView
import com.onair.hearit.presentation.detail.PlayerDetailActivity
import com.onair.hearit.presentation.detail.PlayerDetailActivity.Companion.LOGIN_REQUIRED_DIALOG_ID
import com.onair.hearit.presentation.login.LoginActivity
import com.onair.hearit.service.PlaybackService

class ExploreFragment :
    Fragment(),
    ShortsClickListener {
    @Suppress("ktlint:standard:backing-property-naming")
    private var _binding: FragmentExploreBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ExploreViewModel by viewModels { ExploreViewModelFactory() }

    private val player by lazy { ExoPlayer.Builder(requireContext()).build() }
    private val adapter by lazy { ShortsAdapter(player, this) }
    private val snapHelper = PagerSnapHelper()
    private var isFirstLoad = true

    private var animator: ObjectAnimator? = null
    private var playbackListener: Player.Listener? = null

    private val playerDetailLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                (activity as? PlayerControllerView)?.apply {
                    pause()
                    hidePlayerControlView()

                    val hearitId = result.data?.getLongExtra(HEARIT_ID, -1) ?: -1
                    val bookmarkId =
                        result.data?.getLongExtra(BOOKMARK_ID, -1L).takeIf { it != -1L }

                    if (hearitId != -1L) {
                        updateBookmarkState(hearitId, bookmarkId)
                    }
                }
            }
        }

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
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        setupWindowInsets()
        setupRecyclerView()
        observeViewModel()

        (activity as? PlayerControllerView)?.pause()

        playbackListener =
            object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_ENDED) scrollToNextItem()
                }
            }.also { player.addListener(it) }
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
                        val snapView = snapHelper.findSnapView(layoutManager) ?: return
                        val position = layoutManager.getPosition(snapView)
                        val item = adapter.currentList.getOrNull(position) ?: return

                        player.setMediaItem(MediaItem.fromUri(item.audioUrl))
                        player.prepare()
                        player.play()
                        checkAndLoadNextPage(position)

                        AnalyticsProvider.get().logEvent(AnalyticsEventNames.EXPLORE_SWIPE)
                    }
                }
            },
        )
    }

    private fun observeViewModel() {
        viewModel.shortsHearits.observe(viewLifecycleOwner) { shortsHearits ->
            adapter.submitList(shortsHearits)

            if (isFirstLoad && shortsHearits.isNotEmpty()) {
                viewModel.shouldPlayAnimation.observe(viewLifecycleOwner) { isEnabled ->
                    if (isEnabled) {
                        startSwipeAnimation()
                        isFirstLoad = false
                    }
                }
            }
        }

        viewModel.toastMessage.observe(viewLifecycleOwner) { resId ->
            showToast(getString(resId))
        }

        viewModel.showLoginDialog.observe(viewLifecycleOwner) {
            showLoginRequiredDialog()
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.frExploreSkeleton.apply {
                if (isLoading) startShimmer() else stopShimmer()
            }
        }
    }

    private fun startSwipeAnimation() {
        binding.lavExploreSwipeUp.visibility = View.VISIBLE
        animator =
            ObjectAnimator.ofFloat(binding.rvExplore, "translationY", 0f, -100f, 0f).apply {
                duration = 1300
                repeatCount = 1
                repeatMode = ObjectAnimator.RESTART
                addListener(
                    object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            binding.lavExploreSwipeUp.visibility = View.INVISIBLE
                        }
                    },
                )
                start()
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
        val intent =
            PlayerDetailActivity.newIntent(requireActivity(), hearitId, lastPosition).apply {
                putExtra(AnalyticsParamKeys.SOURCE, PlayerDetailActivity.EXPLORE_SCREEN_ID)
            }
        playerDetailLauncher.launch(intent)
    }

    private fun showLoginRequiredDialog() {
        LoginRequiredDialogFragment {
            navigateToLogin()
        }.show(parentFragmentManager, LOGIN_REQUIRED_DIALOG_ID)
    }

    private fun navigateToLogin() {
        val intent = LoginActivity.newIntent(requireContext())
        startActivity(intent)

        requireContext().stopService(PlaybackService.stopIntent(requireContext()))

        parentFragmentManager
            .beginTransaction()
            .remove(this)
            .commit()
    }

    private fun updateBookmarkState(
        hearitId: Long,
        bookmarkId: Long?,
    ) {
        val updatedList =
            adapter.currentList.map { item ->
                if (item.id == hearitId) {
                    item.copy(
                        bookmarkId = bookmarkId,
                        isBookmarked = bookmarkId != null,
                    )
                } else {
                    item
                }
            }
        adapter.submitList(updatedList)
    }

    override fun onClickHearitInfo(hearitId: Long) {
        val lastPosition = player.currentPosition
        AnalyticsProvider.get().logEvent(
            AnalyticsEventNames.EXPLORE_TO_DETAIL,
            mapOf(AnalyticsParamKeys.ITEM_ID to hearitId.toString()),
        )

        navigateToDetail(hearitId, lastPosition)
    }

    override fun onClickBookmark(
        hearitId: Long,
        callback: (bookmarkId: Long?) -> Unit,
    ) {
        viewModel.toggleBookmark(
            hearitId = hearitId,
            onFinished = { bookmarkId ->
                callback(bookmarkId)
            },
        )
    }

    override fun onPause() {
        super.onPause()
        player.pause()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        playbackListener?.let { player.removeListener(it) }
        playbackListener = null

        animator?.cancel()
        animator?.removeAllListeners()
        animator?.setTarget(null)

        binding.rvExplore.clearOnScrollListeners()
        snapHelper.attachToRecyclerView(null)
        binding.rvExplore.adapter = null

        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }

    companion object {
        const val HEARIT_ID = "hearit_id"
        const val BOOKMARK_ID = "bookmark_id"
    }
}
