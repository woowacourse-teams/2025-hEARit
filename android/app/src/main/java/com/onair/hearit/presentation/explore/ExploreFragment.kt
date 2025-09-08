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
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.analytics.AnalyticsEventNames
import com.onair.hearit.analytics.AnalyticsParamKeys
import com.onair.hearit.databinding.FragmentExploreBinding
import com.onair.hearit.di.AnalyticsProvider
import com.onair.hearit.presentation.DetailResult
import com.onair.hearit.presentation.IntentKeys.PREVIOUS_SCREEN_KEY
import com.onair.hearit.presentation.IntentValues.EXPLORE_VALUE
import com.onair.hearit.presentation.LoginRequiredDialogFragment
import com.onair.hearit.presentation.MainActivity
import com.onair.hearit.presentation.PlayerControllerView
import com.onair.hearit.presentation.detail.PlayerDetailActivity
import com.onair.hearit.presentation.detail.PlayerDetailActivity.Companion.LOGIN_REQUIRED_DIALOG_TAG
import com.onair.hearit.presentation.login.LoginActivity
import com.onair.hearit.presentation.navigate
import com.onair.hearit.presentation.toDetailResult
import com.onair.hearit.service.PlaybackService
import timber.log.Timber

class ExploreFragment :
    Fragment(),
    ShortsClickListener {
    @Suppress("ktlint:standard:backing-property-naming")
    private var _binding: FragmentExploreBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ExploreViewModel by activityViewModels { ExploreViewModelFactory() }

    private lateinit var playerManager: ExplorePlayerManager
    private val player get() = playerManager.player

    private val adapter by lazy { ShortsAdapter(player, this) }
    private val snapHelper = PagerSnapHelper()

    private var animator: ObjectAnimator? = null

    private val playerDetailLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult

            (activity as? PlayerControllerView)?.apply {
                pause()
                hidePlayerControlView()

                when (val detailResult = result.data.toDetailResult()) {
                    is DetailResult.Explore -> {
                        updateBookmarkState(detailResult.hearitId, detailResult.bookmarkId)
                    }

                    is DetailResult.Category,
                    is DetailResult.Keyword,
                    -> {
                        detailResult.navigate(requireActivity() as MainActivity)
                    }

                    null -> Timber.w("Invalid detail result")
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

        playerManager =
            ExplorePlayerManager(
                context = requireContext().applicationContext,
                lifecycleScope = viewLifecycleOwner.lifecycleScope,
                onPlaybackEnded = { scrollToNextItem() },
                onPositionUpdated = { position -> highlightScript(position) },
            )

        setupRecyclerView()
        observeViewModel()

        (activity as? PlayerControllerView)?.pause()
    }

    override fun onResume() {
        super.onResume()
        val player = playerManager.player
        player.playWhenReady = true
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }
    }

    private fun currentIndex(): Int {
        val layoutManager =
            binding.rvExplore.layoutManager as? LinearLayoutManager
                ?: return RecyclerView.NO_POSITION
        val snapView = snapHelper.findSnapView(layoutManager) ?: return RecyclerView.NO_POSITION
        return layoutManager.getPosition(snapView)
    }

    private fun highlightScript(positionMs: Long) {
        val index = currentIndex()
        if (index == RecyclerView.NO_POSITION) return
        val holder = binding.rvExplore.findViewHolderForAdapterPosition(index) as? ShortsViewHolder
        holder?.highlightScriptLine(positionMs)
    }

    private fun playAudioAtIndex(
        index: Int,
        startPosition: Long = 0L,
    ) {
        val item = adapter.currentList.getOrNull(index) ?: return
        playerManager.playAudio(item.audioUrl, startPosition)
    }

    private fun switchTo(newPosition: Int) {
        if (newPosition == RecyclerView.NO_POSITION) return

        val lastPosition = viewModel.getLastPlayerPosition()
        viewModel.onPageSnapped(newPosition)

        playAudioAtIndex(newPosition, lastPosition)
        checkAndLoadNextPage(newPosition)
    }

    private fun scrollToNextItem() {
        val currentPosition = currentIndex()
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
                        switchTo(currentIndex())
                        AnalyticsProvider.get().logEvent(AnalyticsEventNames.EXPLORE_SWIPE)
                    }
                }
            },
        )
    }

    private fun observeViewModel() {
        viewModel.shortsHearits.observe(viewLifecycleOwner) { shortsHearits ->
            adapter.submitList(shortsHearits) {
                _binding?.let { binding ->
                    if (shortsHearits.isNotEmpty()) {
                        val target = viewModel.currentIndex.value ?: 0
                        val validTarget = target.coerceIn(0, shortsHearits.lastIndex)

                        (binding.rvExplore.layoutManager as? LinearLayoutManager)
                            ?.scrollToPositionWithOffset(validTarget, 0)

                        switchTo(validTarget)
                        viewModel.loadAnimation()
                    }
                }
            }
        }

        viewModel.shouldPlayAnimation.observe(viewLifecycleOwner) { isEnabled ->
            if (isEnabled) startSwipeAnimation()
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
                            _binding?.lavExploreSwipeUp?.visibility = View.INVISIBLE
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
                putExtra(PREVIOUS_SCREEN_KEY, EXPLORE_VALUE)
            }
        playerDetailLauncher.launch(intent)
    }

    private fun showLoginRequiredDialog() {
        LoginRequiredDialogFragment {
            navigateToLogin()
        }.show(parentFragmentManager, LOGIN_REQUIRED_DIALOG_TAG)
    }

    private fun navigateToLogin() {
        AnalyticsProvider.get().logEvent(
            AnalyticsEventNames.LOGIN_EVENT,
            mapOf(AnalyticsParamKeys.SOURCE_NAME to "explore_login"),
        )

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
        viewModel.updateBookmarkState(hearitId, bookmarkId)
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

    override fun onClickHearitInfo(
        hearitId: Long,
        title: String,
    ) {
        player.playWhenReady = false

        val lastPosition = playerManager.getCurrentPosition()
        AnalyticsProvider.get().logEvent(
            AnalyticsEventNames.EXPLORE_TO_DETAIL,
            mapOf(
                AnalyticsParamKeys.ITEM_NAME to title,
                AnalyticsParamKeys.ITEM_INDEX to currentIndex().toString(),
            ),
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
        player.playWhenReady = false
        val position = currentIndex()
        viewModel.saveCurrentState(position, playerManager.getCurrentPosition(), adapter.itemCount)
    }

    override fun onStop() {
        super.onStop()
        playerManager.stop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
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
        playerManager.release()
    }
}
