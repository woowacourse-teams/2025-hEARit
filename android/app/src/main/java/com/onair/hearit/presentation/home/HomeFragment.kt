package com.onair.hearit.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.firebase.analytics.FirebaseAnalytics
import com.onair.hearit.R
import com.onair.hearit.analytics.AnalyticsEventNames
import com.onair.hearit.analytics.AnalyticsLogger
import com.onair.hearit.analytics.AnalyticsParamKeys.CATEGORY_NAME
import com.onair.hearit.analytics.AnalyticsParamKeys.ITEM_ID
import com.onair.hearit.analytics.AnalyticsParamKeys.SCREEN_NAME_HOME
import com.onair.hearit.analytics.HearitSource
import com.onair.hearit.databinding.FragmentHomeBinding
import com.onair.hearit.domain.model.Bookmark
import com.onair.hearit.domain.model.PlayingHistoryHearit
import com.onair.hearit.domain.model.RecentUploadHearit
import com.onair.hearit.domain.model.RecommendationCategories
import com.onair.hearit.domain.model.UserInfo
import com.onair.hearit.presentation.HearitClickListener
import com.onair.hearit.presentation.detail.PlayerDetailActivity
import com.onair.hearit.presentation.dpToPx
import com.onair.hearit.presentation.home.adapter.PlayingBookmarkHearitAdapter
import com.onair.hearit.presentation.home.adapter.PlayingHistoryHearitAdapter
import com.onair.hearit.presentation.home.adapter.RecentUploadHearitAdapter
import com.onair.hearit.presentation.home.adapter.RecommendationCategoryAdapter
import com.onair.hearit.presentation.home.component.CarouselSection
import com.onair.hearit.presentation.main.MainActivity
import com.onair.hearit.presentation.main.MainViewModel
import com.onair.hearit.presentation.search.SearchFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment :
    Fragment(),
    HearitClickListener {
    @Suppress("ktlint:standard:backing-property-naming")
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    private val mainViewModel: MainViewModel by activityViewModels()

    private val playingHistoryAdapter: PlayingHistoryHearitAdapter by lazy {
        PlayingHistoryHearitAdapter(this)
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

    @Inject
    lateinit var analyticsLogger: AnalyticsLogger

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
        setupComposeCarousel()
        setupSwipeRefresh()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        analyticsLogger.logEvent(
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
            analyticsLogger.logEvent(AnalyticsEventNames.HOME_BOOKMARK_SELECTED)
            (activity as MainActivity).selectTab(R.id.nav_library)
        }

        binding.tvHomeShortcast.setOnClickListener {
            analyticsLogger.logEvent(AnalyticsEventNames.HOME_EXPLORE_SELECTED)
            (activity as MainActivity).selectTab(R.id.nav_explore)
        }
    }

    private fun setupRecyclerView() {
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

    private fun setupComposeCarousel() {
        binding.recommendCarousel.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed,
        )
        binding.recommendCarousel.setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            if (uiState.showRecommendHearits) {
                MaterialTheme {
                    CarouselSection(
                        items = uiState.recommendHearits,
                        onItemClick = { item ->
                            logHomeHearitClick(HearitSource.RECOMMEND, item.id)
                            navigateToPlayerDetail(item.id)
                        },
                    )
                }
            }
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.apply {
            setColorSchemeResources(
                R.color.hearit_purple1,
                R.color.hearit_purple2,
                R.color.hearit_purple3,
            )

            setOnRefreshListener {
                viewModel.refreshData()
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.isRefreshing.collect { refreshing ->
                        binding.swipeRefreshLayout.isRefreshing = refreshing
                    }
                }

                launch {
                    viewModel.uiState.collect { state ->
                        updateUI(state)
                    }
                }
            }
        }

        viewModel.toastMessage.observe(viewLifecycleOwner, ::showToast)
    }

    private fun updateUI(state: HomeUiState) {
        updateLoadingState(state.isLoading)
        updateAdSections(state.isLoading)
        updateUserInfo(state.userInfo, state.isLoggedIn)

        binding.recommendCarousel.isVisible = state.showRecommendHearits
        updatePlayingHistorySection(state.playingHistoryHearits, state.showPlayingHistory)
        updateRecentUploadSection(state.recentUploadHearits, state.showRecentUpload)
        updateBookmarkSection(state.playingBookmarkHearits, state.showBookmark)
        updateCategoriesSection(state.recommendationCategories, state.showCategories)
        binding.ad = state.advertisement
    }

    private fun updateLoadingState(isLoading: Boolean) {
        binding.frHomeSkeleton.apply {
            isVisible = isLoading
            if (isLoading) startShimmer() else stopShimmer()
        }
    }

    private fun updateUserInfo(
        userInfo: UserInfo?,
        isLoggedIn: Boolean,
    ) {
        mainViewModel.updateLoginState(isLoggedIn)
        binding.userInfo = userInfo
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
        binding.rvHomeRecentUpload.isVisible = shouldShow
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

    private fun updateCategoriesSection(
        recommendationCategories: List<RecommendationCategories>,
        shouldShow: Boolean,
    ) {
        binding.rvHomeRecommendationCategories.isVisible = shouldShow
        recommendationCategoryAdapter.submitList(recommendationCategories)
    }

    private fun updateAdSections(isLoading: Boolean) {
        binding.tvHomeShortcast.isVisible = !isLoading
    }

    private fun showToast(messageResId: Int) {
        Toast.makeText(requireContext(), getString(messageResId), Toast.LENGTH_SHORT).show()
    }

    private fun navigateToSearch(
        id: Long,
        name: String,
        colorCode: String,
    ) {
        analyticsLogger.logEvent(
            AnalyticsEventNames.HOME_RECOMMENDATION_CATEGORY_SELECTED,
            mapOf(ITEM_ID to id.toString(), CATEGORY_NAME to name),
        )

        val fragment =
            SearchFragment.newInstanceWithCategory(
                categoryId = id,
                categoryName = name,
                categoryColor = colorCode,
            )

        parentFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container_view, fragment)
            .addToBackStack(null)
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
        analyticsLogger.logEvent(event, mapOf(ITEM_ID to hearitId.toString()))
    }

    override fun onClick(
        hearitId: Long,
        source: HearitSource,
    ) {
        logHomeHearitClick(source, hearitId)
        navigateToPlayerDetail(hearitId)
    }

    override fun onDestroyView() {
        binding.rvHomeRecommendationCategories.adapter = null
        binding.rvHomePlayingHistoryHearit.adapter = null
        binding.rvHomeRecentUpload.adapter = null
        binding.rvHomePlayingBookmark.adapter = null
        _binding = null
        super.onDestroyView()
    }

    private companion object {
        private const val SIDE_MARGIN = 16
    }
}
