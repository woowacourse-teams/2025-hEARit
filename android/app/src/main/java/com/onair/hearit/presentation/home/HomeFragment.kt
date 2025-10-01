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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.onair.hearit.R
import com.onair.hearit.analytics.AnalyticsEventNames
import com.onair.hearit.databinding.FragmentHomeBinding
import com.onair.hearit.di.AnalyticsProvider
import com.onair.hearit.domain.model.Category
import com.onair.hearit.domain.model.PlayingBookmarkHearit
import com.onair.hearit.domain.model.RecentUploadHearit
import com.onair.hearit.presentation.HearitClickListener
import com.onair.hearit.presentation.IntentKeys.CATEGORY_COLOR_KEY
import com.onair.hearit.presentation.IntentKeys.CATEGORY_ID_KEY
import com.onair.hearit.presentation.IntentKeys.CATEGORY_NAME_KEY
import com.onair.hearit.presentation.detail.PlayerDetailActivity
import com.onair.hearit.presentation.dpToPx
import com.onair.hearit.presentation.main.DrawerClickListener
import com.onair.hearit.presentation.main.MainActivity
import com.onair.hearit.presentation.main.MainViewModel
import com.onair.hearit.presentation.search.category.CategoryComposeFragment

class HomeFragment :
    Fragment(),
    HearitClickListener {
    @Suppress("ktlint:standard:backing-property-naming")
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels { HomeViewModelFactory() }
    private val mainViewModel: MainViewModel by activityViewModels()

    private val recentAdapter: PlayingHistoryHearitAdapter by lazy {
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

    private val groupedCategoryAdapter: GroupedCategoryAdapter by lazy {
        GroupedCategoryAdapter(
            this,
            navigateClickListener = { id, name, colorCode ->
                navigateToSearch(
                    id,
                    name,
                    colorCode,
                )
            },
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
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        setupWindowInsets()
        setupListeners()
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }
    }

    private fun setupListeners() {
        binding.ivProfile.setOnClickListener {
            (activity as? DrawerClickListener)?.openDrawer()
        }

        binding.tvHomePlayingBookmarkTitle.setOnClickListener {
            (activity as MainActivity).selectTab(R.id.nav_library)
        }

        binding.ibHomePlayingBookmark.setOnClickListener {
            (activity as MainActivity).selectTab(R.id.nav_library)
        }

        binding.tvHomeShortcast.setOnClickListener {
            AnalyticsProvider.get().logEvent(AnalyticsEventNames.HOME_EXPLORE_SELECTED)
            (activity as MainActivity).selectTab(R.id.nav_explore)
        }

        binding.tvHomeWootaeco.setOnClickListener {
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

        binding.rvHomeRecentUpload.adapter = recentUploadAdapter

        binding.rvHomePlayingBookmark.adapter = playingBookmarkAdapter

        binding.rvHomePlayingHearit.apply {
            adapter = recentAdapter
            addItemDecoration(HorizontalMarginItemDecoration(SIDE_MARGIN.dpToPx(requireContext())))
        }

        binding.rvHomeGroupedCategory.adapter = groupedCategoryAdapter
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.frHomeSkeleton.apply {
                if (isLoading) startShimmer() else stopShimmer()
            }
        }

        viewModel.isLoggedIn.observe(viewLifecycleOwner) { isLoggedIn ->
            mainViewModel.updateLoginState(isLoggedIn)
        }

        viewModel.userInfo.observe(viewLifecycleOwner) { userInfo ->
            binding.userInfo = userInfo
        }

        viewModel.recommendHearits.observe(viewLifecycleOwner) { recommendHearits ->
            recommendAdapter.submitList(recommendHearits) {
                if (view != null && viewLifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                    scrollToMiddlePosition()
                    setupIndicator()
                }
            }
        }

        viewModel.playingHistoryHearits.observe(viewLifecycleOwner) { recentHearits ->
            binding.tvHomePlayingHistoryHearitTitle.isVisible = recentHearits.isNotEmpty()
            recentAdapter.submitList(recentHearits)
        }

        viewModel.recentUploadHearits.observe(viewLifecycleOwner) { recentUploadHearits ->
            val dummy =
                listOf(
                    RecentUploadHearit(
                        id = 1,
                        title = "더미데이터1 더미데이터2 터미네이터3",
                        category = Category(id = 1, colorCode = "#8C46D2", name = "IT 트렌드"),
                    ),
                )
            recentUploadAdapter.submitList(dummy)
        }

        viewModel.playingBookmarkHearits.observe(viewLifecycleOwner) { playingBookmarkHearits ->
            val dummy =
                listOf(
                    PlayingBookmarkHearit(
                        id = 1,
                        title = "더미데이터1 더미데이터2 터미네이터3",
                        playTime = 600,
                        lastPlayTime = 500000,
                        createdAt = "aaaaa",
                        category = Category(id = 1, colorCode = "#1883B5", name = "IT 트렌드"),
                    ),
                )
            playingBookmarkAdapter.submitList(dummy)
        }

        viewModel.groupedCategory.observe(viewLifecycleOwner) { groupedCategory ->
            groupedCategoryAdapter.submitList(groupedCategory)
        }

        viewModel.toastMessage.observe(viewLifecycleOwner) { resId ->
            showToast(getString(resId))
        }
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

    private fun showToast(message: String?) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToSearch(
        id: Long,
        name: String,
        colorCode: String,
    ) {
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

    override fun onClick(hearitId: Long) {
        navigateToPlayerDetail(hearitId)
    }

    override fun onDestroyView() {
        centerScrollListener?.let {
            binding.rvHomeRecommend.removeOnScrollListener(it)
        }
        centerScrollListener = null
        snapHelper.attachToRecyclerView(null)
        binding.rvHomeRecommend.adapter = null
        binding.rvHomeGroupedCategory.adapter = null
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
