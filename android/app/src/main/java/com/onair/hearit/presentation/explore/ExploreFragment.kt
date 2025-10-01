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
import androidx.core.view.isEmpty
import androidx.core.view.isNotEmpty
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
import com.onair.hearit.domain.model.ExploreHearit
import com.onair.hearit.presentation.DetailResult
import com.onair.hearit.presentation.IntentKeys.PREVIOUS_SCREEN_KEY
import com.onair.hearit.presentation.IntentValues.EXPLORE_VALUE
import com.onair.hearit.presentation.LoginRequiredDialogFragment
import com.onair.hearit.presentation.PlayerControllerView
import com.onair.hearit.presentation.detail.PlayerDetailActivity
import com.onair.hearit.presentation.detail.PlayerDetailActivity.Companion.LOGIN_REQUIRED_DIALOG_TAG
import com.onair.hearit.presentation.login.LoginActivity
import com.onair.hearit.presentation.main.MainActivity
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

    private val isViewValid: Boolean
        get() = _binding != null

    private val viewModel: ExploreViewModel by activityViewModels { ExploreViewModelFactory() }

    private val playerManager by lazy {
        ExplorePlayerManager(
            context = requireContext().applicationContext,
            lifecycleScope = viewLifecycleOwner.lifecycleScope,
            onPlaybackEnded = {
                if (isViewValid) scrollToNextItem()
            },
            onPositionUpdated = { position ->
                if (isViewValid) highlightScript(position)
            },
        )
    }
    private val player get() = playerManager.player

    private val adapter by lazy { ShortsAdapter(player, this) }
    private val snapHelper = PagerSnapHelper()

    private var animator: ObjectAnimator? = null
    private var lastPlayingIndex: Int = RecyclerView.NO_POSITION

    private val playerDetailLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult

            (activity as? PlayerControllerView)?.apply {
                pause()
                hidePlayerControlView()

                when (val detailResult = result.data.toDetailResult()) {
                    is DetailResult.Category, is DetailResult.Keyword ->
                        detailResult.navigate(requireActivity() as MainActivity)

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

        setupRecyclerView()
        observeViewModel()

        (activity as? PlayerControllerView)?.pause()

        viewModel.resumeIfScheduled()
    }

    override fun onResume() {
        super.onResume()
        if (lastPlayingIndex != RecyclerView.NO_POSITION) {
            player.playWhenReady = true
        }
    }

    override fun onPause() {
        super.onPause()

        if (lastPlayingIndex != RecyclerView.NO_POSITION) {
            player.playWhenReady = false

            if (isViewValid) {
                val index = currentIndex()
                viewModel.scheduleResume(
                    resumeIndex = index,
                    playerPositionMs = playerManager.getCurrentPosition(),
                )
            }
        }
    }

    override fun onDestroyView() {
        animator?.removeAllListeners()
        animator?.cancel()
        animator?.setTarget(null)
        animator = null

        binding.rvExplore.clearOnScrollListeners()
        snapHelper.attachToRecyclerView(null)
        binding.rvExplore.adapter = null

        playerManager.stop()
        lastPlayingIndex = RecyclerView.NO_POSITION

        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        playerManager.release()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
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
                    if (!isViewValid) return

                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        val index = currentIndex()
                        if (index != RecyclerView.NO_POSITION && index != lastPlayingIndex) {
                            switchTo(index)
                            player.play()
                        }

                        viewModel.maybeLoadMore(index, adapter.itemCount)
                        AnalyticsProvider.get().logEvent(AnalyticsEventNames.EXPLORE_SWIPE)
                    }
                }
            },
        )
    }

    private fun observeViewModel() {
        viewModel.shortsHearits.observe(viewLifecycleOwner) { shortsHearits ->
            handleShortsHearitsUpdate(shortsHearits)
        }

        viewModel.shouldPlayAnimation.observe(viewLifecycleOwner) { isEnabled ->
            if (!isViewValid) return@observe

            if (isEnabled && binding.rvExplore.isNotEmpty()) {
                startSwipeAnimation()
            }
        }

        viewModel.toastMessage.observe(viewLifecycleOwner) { resId ->
            showToast(getString(resId))
        }

        viewModel.showLoginDialog.observe(viewLifecycleOwner) {
            showLoginRequiredDialog()
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (!isViewValid) return@observe
            binding.frExploreSkeleton.apply {
                if (isLoading) startShimmer() else stopShimmer()
            }
        }
    }

    // 피드 목록이 갱신 되었을 때
    private fun handleShortsHearitsUpdate(shortsHearits: List<ExploreHearit>) {
        if (!isViewValid) return
        adapter.submitList(shortsHearits) {
            if (!isViewValid) return@submitList
            if (shortsHearits.isEmpty()) return@submitList

            // 레이아웃이 아직 안붙은 경우에 한 번 더 post로 지연
            binding.rvExplore.post {
                if (!isViewValid) return@post

                if (binding.rvExplore.isEmpty()) {
                    waitForRecyclerViewLayout()
                } else {
                    startPlaybackAndAnimation()
                }
            }
        }
    }

    // 만약에 뷰가 없으면 기다렸다가 post 재시도
    private fun waitForRecyclerViewLayout() {
        binding.rvExplore.post {
            if (isViewValid) {
                startPlaybackAndAnimation()
            }
        }
    }

    // 현재 인덱스의 아이템을 재생하고, 추가적으로 애니메이션 트리거
    private fun startPlaybackAndAnimation() {
        val index = currentIndex().takeIf { it != RecyclerView.NO_POSITION } ?: 0
        switchTo(index)
        viewModel.maybeLoadMore(index, adapter.itemCount)
        viewModel.loadAnimation()
    }

    private fun currentIndex(): Int {
        if (!isViewValid) return RecyclerView.NO_POSITION

        val layoutManager =
            binding.rvExplore.layoutManager as? LinearLayoutManager
                ?: return RecyclerView.NO_POSITION
        val snapView = snapHelper.findSnapView(layoutManager) ?: return RecyclerView.NO_POSITION
        return layoutManager.getPosition(snapView)
    }

    private fun highlightScript(positionMs: Long) {
        if (!isViewValid) return

        val index = currentIndex()
        if (index == RecyclerView.NO_POSITION) return
        val holder =
            binding.rvExplore.findViewHolderForAdapterPosition(index) as? ShortsViewHolder
        holder?.highlightScriptLine(positionMs)
    }

    private fun playAudioAtIndex(
        index: Int,
        startPosition: Long = 0L,
    ) {
        val list = adapter.currentList
        if (index !in list.indices) return

        val item = list[index]
        val url =
            item.audioUrl?.takeIf { it.isNotBlank() } ?: run {
                return
            }

        playerManager.playAudio(url, startPosition)
    }

    private fun switchTo(newPosition: Int) {
        if (newPosition == RecyclerView.NO_POSITION || newPosition == lastPlayingIndex) return
        val startPos = viewModel.consumeResumePositionMs()
        playAudioAtIndex(newPosition, startPos)
        lastPlayingIndex = newPosition
        player.playWhenReady = true
    }

    private fun scrollToNextItem() {
        if (!isViewValid) return

        val currentPosition = currentIndex()
        val nextPosition = currentPosition + 1
        if (nextPosition < adapter.itemCount) {
            binding.rvExplore.smoothScrollToPosition(nextPosition)
        }
    }

    private fun startSwipeAnimation() {
        if (!isViewValid) return

        binding.lavExploreSwipeUp.visibility = View.VISIBLE

        animator?.removeAllListeners()
        animator?.cancel()
        animator?.setTarget(null)

        animator = createSwipeAnimator()
    }

    private fun createSwipeAnimator() =
        ObjectAnimator.ofFloat(binding.rvExplore, "translationY", 0f, -100f, 0f).apply {
            duration = 1300
            repeatCount = 1
            repeatMode = ObjectAnimator.RESTART
            addListener(
                object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        if (!isViewValid) return
                        _binding?.lavExploreSwipeUp?.visibility = View.INVISIBLE
                    }
                },
            )
            start()
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

    private fun showLoginRequiredDialog() {
        LoginRequiredDialogFragment { navigateToLogin() }
            .show(parentFragmentManager, LOGIN_REQUIRED_DIALOG_TAG)
    }

    private fun navigateToLogin() {
        AnalyticsProvider.get().logEvent(
            AnalyticsEventNames.LOGIN_EVENT,
            mapOf(AnalyticsParamKeys.SOURCE_NAME to "explore_login"),
        )

        val intent = LoginActivity.newIntent(requireContext())
        startActivity(intent)

        requireContext().stopService(PlaybackService.stopIntent(requireContext()))

        parentFragmentManager.beginTransaction().remove(this).commit()
    }

    override fun onClickHearitInfo(
        hearitId: Long,
        title: String,
    ) {
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

    private fun showToast(message: String?) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
