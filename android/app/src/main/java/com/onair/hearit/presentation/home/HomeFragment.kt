package com.onair.hearit.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.google.firebase.analytics.FirebaseAnalytics
import com.onair.hearit.R
import com.onair.hearit.analytics.AnalyticsEventNames
import com.onair.hearit.analytics.AnalyticsParamKeys.CATEGORY_NAME
import com.onair.hearit.analytics.AnalyticsParamKeys.ITEM_ID
import com.onair.hearit.analytics.AnalyticsParamKeys.SCREEN_NAME_HOME
import com.onair.hearit.analytics.HearitSource
import com.onair.hearit.databinding.FragmentHomeBinding
import com.onair.hearit.di.AnalyticsProvider
import com.onair.hearit.domain.model.Bookmark
import com.onair.hearit.domain.model.PlayingHistoryHearit
import com.onair.hearit.domain.model.RecentUploadHearit
import com.onair.hearit.domain.model.RecommendHearit
import com.onair.hearit.domain.model.RecommendationCategories
import com.onair.hearit.domain.model.UserInfo
import com.onair.hearit.presentation.HearitClickListener
import com.onair.hearit.presentation.IntentKeys.CATEGORY_COLOR_KEY
import com.onair.hearit.presentation.IntentKeys.CATEGORY_ID_KEY
import com.onair.hearit.presentation.IntentKeys.CATEGORY_NAME_KEY
import com.onair.hearit.presentation.detail.PlayerDetailActivity
import com.onair.hearit.presentation.dpToPx
import com.onair.hearit.presentation.home.adapter.PlayingBookmarkHearitAdapter
import com.onair.hearit.presentation.home.adapter.PlayingHistoryHearitAdapter
import com.onair.hearit.presentation.home.adapter.RecentUploadHearitAdapter
import com.onair.hearit.presentation.home.adapter.RecommendHearitAdapter
import com.onair.hearit.presentation.home.adapter.RecommendationCategoryAdapter
import com.onair.hearit.presentation.main.MainActivity
import com.onair.hearit.presentation.main.MainViewModel
import com.onair.hearit.presentation.search.category.CategoryComposeFragment
import kotlinx.coroutines.launch

class HomeFragment :
    Fragment(),
    HearitClickListener {
    @Suppress("ktlint:standard:backing-property-naming")
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels { HomeViewModelFactory() }
    private val mainViewModel: MainViewModel by activityViewModels()

    private val playingHistoryAdapter: PlayingHistoryHearitAdapter by lazy {
        PlayingHistoryHearitAdapter(this)
    }

    private val recommendAdapter: RecommendHearitAdapter by lazy {
        RecommendHearitAdapter(this)
    }

    private val recentUploadAdapter: RecentUploadHearitAdapter by lazy {
        RecentUploadHearitAdapter(this)
    }

    private val playingBookmarkAdapter: PlayingBookmarkHearitAdapter by lazy {
        PlayingBookmarkHearitAdapter(this)
    }

    private val recommendationCategoryAdapter: RecommendationCategoryAdapter by lazy {
        RecommendationCategoryAdapter(
            this,
            navigateClickListener = ::navigateToSearch,
        )
    }
    private val snapHelper = PagerSnapHelper()
    private var centerScrollListener: CenterScrollListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        setupWindowInsets()
        setupListeners()
        setupRecyclerView()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        AnalyticsProvider.get().logEvent(
            FirebaseAnalytics.Event.SCREEN_VIEW,
            mapOf(
                FirebaseAnalytics.Param.SCREEN_NAME to SCREEN_NAME_HOME,
                FirebaseAnalytics.Param.SCREEN_CLASS to this::class.simpleName.orEmpty(),
            ),
        )
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }
    }

    private fun setupListeners() {
        binding.tvHomePlayingBookmarkTitle.setOnClickListener {
            AnalyticsProvider.get().logEvent(AnalyticsEventNames.HOME_BOOKMARK_SELECTED)
            (activity as MainActivity).selectTab(R.id.nav_library)
        }

        binding.tvHomeShortcast.setOnClickListener {
            AnalyticsProvider.get().logEvent(AnalyticsEventNames.HOME_EXPLORE_SELECTED)
            (activity as MainActivity).selectTab(R.id.nav_explore)
        }

        binding.tvHomeWootaeco.setOnClickListener {
            AnalyticsProvider.get().logEvent(AnalyticsEventNames.HOME_WOOTAECO_SELECTED)
            navigateToSearch(id = 13, name = "우아한테크코스", colorCode = "#12C6B0")
        }
    }

    private fun setupRecyclerView() {
        centerScrollListener =
            CenterScrollListener(snapHelper) { position ->
                updateIndicator(position)
            }

        binding.rvHomeRecommend.apply {
            doOnPreDraw { scrollToMiddlePosition() }
            adapter = recommendAdapter
            snapHelper.attachToRecyclerView(this)
            centerScrollListener?.let { addOnScrollListener(it) }
        }

        binding.rvHomePlayingHistoryHearit.apply {
            adapter = playingHistoryAdapter
            addItemDecoration(HorizontalMarginItemDecoration(SIDE_MARGIN.dpToPx(requireContext())))
        }

        binding.rvHomeRecentUpload.apply {
            adapter = recentUploadAdapter
            addItemDecoration(HorizontalMarginItemDecoration(SIDE_MARGIN.dpToPx(requireContext())))
        }

        binding.rvHomePlayingBookmark.apply {
            adapter = playingBookmarkAdapter
            addItemDecoration(HorizontalMarginItemDecoration(SIDE_MARGIN.dpToPx(requireContext())))
        }

        binding.rvHomeRecommendationCategories.adapter = recommendationCategoryAdapter
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    updateLoadingState(state.isLoading)
                    updateAdSections(state.isLoading)

                    updatePlayingHistorySection(
                        state.playingHistoryHearits,
                        !state.isLoading && state.showPlayingHistory,
                    )
                    updateRecentUploadSection(
                        state.recentUploadHearits,
                        !state.isLoading && state.showRecentUpload,
                    )
                    updateBookmarkSection(
                        state.playingBookmarkHearits,
                        !state.isLoading && state.showBookmark,
                    )

                    if (!state.isLoading) {
                        updateUserInfo(state.userInfo, state.isLoggedIn)
                        updateRecommendSection(state.recommendHearits)
                        updateCategoriesSection(state.recommendationCategories)
                    }
                }
            }
        }

        viewModel.toastMessage.observe(viewLifecycleOwner) { resId -> showToast(resId) }
    }

    private fun updateLoadingState(isLoading: Boolean) {
        binding.frHomeSkeleton.apply {
            isVisible = isLoading
            if (isLoading) startShimmer() else stopShimmer()
        }
    }

    private fun updateUserInfo(
        userInfo: UserInfo,
        isLoggedIn: Boolean,
    ) {
        mainViewModel.updateLoginState(isLoggedIn)
        binding.userInfo = userInfo
    }

    private fun updateRecommendSection(recommendHearits: List<RecommendHearit>) {
        recommendAdapter.submitList(recommendHearits) {
            if (view != null && viewLifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                scrollToMiddlePosition()
                setupIndicator()
            }
        }
    }

    private fun updatePlayingHistorySection(
        playingHistoryHearits: List<PlayingHistoryHearit>,
        shouldShow: Boolean,
    ) {
        binding.tvHomePlayingHistoryHearitTitle.isVisible = shouldShow
        binding.rvHomePlayingHistoryHearit.isVisible = shouldShow
        playingHistoryAdapter.submitList(playingHistoryHearits)
    }

    private fun updateRecentUploadSection(
        recentUploadHearits: List<RecentUploadHearit>,
        shouldShow: Boolean,
    ) {
        binding.tvHomeRecentUploadTitle.isVisible = shouldShow
        recentUploadAdapter.submitList(recentUploadHearits)
    }

    private fun updateBookmarkSection(
        playingBookmarkHearits: List<Bookmark>,
        shouldShow: Boolean,
    ) {
        binding.tvHomePlayingBookmarkTitle.isVisible = shouldShow
        binding.rvHomePlayingBookmark.isVisible = shouldShow
        playingBookmarkAdapter.submitList(playingBookmarkHearits)
    }

    private fun updateCategoriesSection(recommendationCategories: List<RecommendationCategories>) {
        recommendationCategoryAdapter.submitList(recommendationCategories)
    }

    private fun updateAdSections(isLoading: Boolean) {
        binding.tvHomeShortcast.isVisible = !isLoading
        binding.tvHomeWootaeco.isVisible = !isLoading
    }

    private fun setupIndicator(size: Int = 5) {
        val container = binding.indicatorContainer
        container.removeAllViews()
        val density = resources.displayMetrics.density

        repeat(size) {
            val dot =
                View(requireContext()).apply {
                    val sizeInPx = (INDICATOR_SIZE_DP * density).toInt()
                    val marginPx = (INDICATOR_MARGIN_DP * density).toInt()
                    layoutParams =
                        LinearLayout.LayoutParams(sizeInPx, sizeInPx).apply {
                            marginStart = marginPx
                            marginEnd = marginPx
                        }
                }
            container.addView(dot)
        }
        setCurrentIndicator(INITIAL_INDICATOR_POSITION)
    }

    private fun updateIndicator(position: Int) {
        val container = binding.indicatorContainer
        val count = container.childCount
        if (count == 0) return

        val indicatorIndex = position
        if (indicatorIndex in 0 until count) {
            setCurrentIndicator(indicatorIndex)
        }
    }

    private fun setCurrentIndicator(selectedIndex: Int) {
        val container = binding.indicatorContainer
        for (i in 0 until container.childCount) {
            val drawableRes =
                if (i == selectedIndex) {
                    R.drawable.indicator_selected
                } else {
                    R.drawable.indicator_unselected
                }
            container.getChildAt(i).setBackgroundResource(drawableRes)
        }
    }

    private fun scrollToMiddlePosition() {
        if (viewLifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            val middlePosition = recommendAdapter.currentList.size / 2
            val layoutManager = binding.rvHomeRecommend.layoutManager as LinearLayoutManager
            val recyclerViewCenter = binding.rvHomeRecommend.width / 2
            val itemWidth = (ITEM_WIDTH_DP * resources.displayMetrics.density).toInt()
            layoutManager.scrollToPositionWithOffset(
                middlePosition,
                recyclerViewCenter - itemWidth / 2,
            )
        }
    }

    private fun showToast(messageResId: Int) {
        Toast.makeText(requireContext(), getString(messageResId), Toast.LENGTH_SHORT).show()
    }

    private fun navigateToSearch(
        id: Long,
        name: String,
        colorCode: String,
    ) {
        AnalyticsProvider.get().logEvent(
            AnalyticsEventNames.HOME_RECOMMENDATION_CATEGORY_SELECTED,
            mapOf(ITEM_ID to id.toString(), CATEGORY_NAME to name),
        )

        parentFragmentManager
            .beginTransaction()
            .replace(
                R.id.fragment_container_view,
                CategoryComposeFragment().apply {
                    arguments =
                        bundleOf(
                            CATEGORY_ID_KEY to id,
                            CATEGORY_NAME_KEY to name,
                            CATEGORY_COLOR_KEY to colorCode,
                        )
                },
            ).addToBackStack(null)
            .commit()
    }

    private fun navigateToPlayerDetail(hearitId: Long) {
        val intent = PlayerDetailActivity.newIntent(requireActivity(), hearitId)
        (activity as? MainActivity)?.launchDetailActivity(intent)
    }

    private fun logHomeHearitClick(
        source: HearitSource,
        hearitId: Long,
    ) {
        val event =
            when (source) {
                HearitSource.PLAYING_HISTORY -> AnalyticsEventNames.HOME_PLAYING_HISTORY_SELECTED
                HearitSource.RECOMMEND -> AnalyticsEventNames.HOME_RECOMMEND_SELECTED
                HearitSource.RECENT_UPLOAD -> AnalyticsEventNames.HOME_RECENT_UPLOAD_SELECTED
                HearitSource.PLAYING_BOOKMARK -> AnalyticsEventNames.HOME_PLAYING_BOOKMARK_SELECTED
                HearitSource.RECOMMENDATION_CATEGORY -> AnalyticsEventNames.HOME_RECOMMENDATION_CATEGORY_HEARIT_SELECTED
                HearitSource.SEARCH_KEYWORD -> AnalyticsEventNames.SEARCH_KEYWORD_SELECTED
            }
        AnalyticsProvider.get().logEvent(event, mapOf(ITEM_ID to hearitId.toString()))
    }

    override fun onClick(
        hearitId: Long,
        source: HearitSource,
    ) {
        logHomeHearitClick(source, hearitId)
        navigateToPlayerDetail(hearitId)
    }

    override fun onDestroyView() {
        centerScrollListener?.let {
            binding.rvHomeRecommend.removeOnScrollListener(it)
        }
        centerScrollListener = null
        snapHelper.attachToRecyclerView(null)
        binding.rvHomeRecommend.adapter = null
        binding.rvHomeRecommendationCategories.adapter = null
        _binding = null
        super.onDestroyView()
    }

    private companion object {
        private const val ITEM_WIDTH_DP = 260
        private const val INDICATOR_SIZE_DP = 8
        private const val INDICATOR_MARGIN_DP = 4
        private const val INITIAL_INDICATOR_POSITION = 2
        private const val SIDE_MARGIN = 16
    }
}
