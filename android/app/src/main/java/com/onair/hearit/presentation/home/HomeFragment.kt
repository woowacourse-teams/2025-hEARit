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
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.R
import com.onair.hearit.analytics.AnalyticsScreenInfo
import com.onair.hearit.databinding.FragmentHomeBinding
import com.onair.hearit.di.AnalyticsProvider
import com.onair.hearit.di.CrashlyticsProvider
import com.onair.hearit.domain.model.RecommendHearits
import com.onair.hearit.domain.model.SearchInput.Companion.CATEGORY_ID_KEY
import com.onair.hearit.domain.model.SearchInput.Companion.CATEGORY_KEY
import com.onair.hearit.domain.model.SearchInput.Companion.CATEGORY_NAME_KEY
import com.onair.hearit.presentation.DrawerClickListener
import com.onair.hearit.presentation.MainActivity
import com.onair.hearit.presentation.detail.PlayerDetailActivity
import com.onair.hearit.presentation.explore.ExploreFragment
import com.onair.hearit.presentation.search.SearchFragment
import kotlin.math.abs

class HomeFragment :
    Fragment(),
    HearitClickListener {
    @Suppress("ktlint:standard:backing-property-naming")
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(
            requireContext(),
            CrashlyticsProvider.get(),
        )
    }
    private val recommendAdapter: RecommendHearitAdapter by lazy {
        RecommendHearitAdapter(
            this,
            navigateClickListener = { navigateToExplore() },
        )
    }
    private val groupedCategoryAdapter: GroupedCategoryAdapter by lazy {
        GroupedCategoryAdapter(
            this,
            navigateClickListener = { id, name -> navigateToSearch(id, name) },
        )
    }
    private val snapHelper = PagerSnapHelper()
    private lateinit var indicatorContainer: LinearLayout

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
        binding.lifecycleOwner = this
        setupWindowInsets()
        setupListeners()
        setupRecommendRecyclerView()
        setupCategoryRecyclerView()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        AnalyticsProvider.get().logScreenView(
            screenName = AnalyticsScreenInfo.Home.NAME,
            screenClass = AnalyticsScreenInfo.Home.CLASS,
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
        binding.ivProfile.setOnClickListener {
            (activity as? DrawerClickListener)?.openDrawer()
        }
    }

    private fun setupRecommendRecyclerView() {
        binding.rvHomeRecommend.apply {
            adapter = recommendAdapter
            snapHelper.attachToRecyclerView(this)

            addOnScrollListener(
                object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(
                        recyclerView: RecyclerView,
                        dx: Int,
                        dy: Int,
                    ) {
                        updateCenterEffect(recyclerView)
                    }

                    override fun onScrollStateChanged(
                        recyclerView: RecyclerView,
                        newState: Int,
                    ) {
                        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                            val layoutManager =
                                recyclerView.layoutManager as? LinearLayoutManager ?: return
                            val snapView = snapHelper.findSnapView(layoutManager) ?: return
                            val position = layoutManager.getPosition(snapView)
                            updateIndicator(position)
                        }
                    }
                },
            )
        }
    }

    private fun setupCategoryRecyclerView() {
        binding.rvHomeGroupedCategory.adapter = groupedCategoryAdapter
    }

    private fun observeViewModel() {
        viewModel.userInfo.observe(viewLifecycleOwner) { userInfo ->
            binding.userInfo = userInfo
        }

        viewModel.recommendHearits.observe(viewLifecycleOwner) { recommendItems ->
            val contentItems = recommendItems.map { RecommendHearits.Content(it) }
            val items =
                buildList {
                    add(RecommendHearits.LeftNavigateItem)
                    addAll(contentItems)
                    add(RecommendHearits.RightNavigateItem)
                }
            recommendAdapter.submitList(items) {
                scrollToMiddlePosition()
                setupIndicator(contentItems.size)
            }
        }

        viewModel.groupedCategory.observe(viewLifecycleOwner) { groupedCategory ->
            groupedCategoryAdapter.submitList(groupedCategory)
        }

        viewModel.toastMessage.observe(viewLifecycleOwner) { resId ->
            showToast(getString(resId))
        }
    }

    private fun setupIndicator(size: Int) {
        indicatorContainer = binding.indicatorContainer
        indicatorContainer.removeAllViews()

        repeat(size) {
            val dot =
                View(requireContext()).apply {
                    val sizeInPx = (INDICATOR_SIZE_DP * resources.displayMetrics.density).toInt()
                    val marginPx = (INDICATOR_MARGIN_DP * resources.displayMetrics.density).toInt()
                    layoutParams =
                        LinearLayout.LayoutParams(sizeInPx, sizeInPx).apply {
                            marginStart = marginPx
                            marginEnd = marginPx
                        }
                }
            indicatorContainer.addView(dot)
        }
        setCurrentIndicator(2)
    }

    private fun updateCenterEffect(recyclerView: RecyclerView) {
        val centerX = recyclerView.width / 2
        for (i in 0 until recyclerView.childCount) {
            val child = recyclerView.getChildAt(i) ?: continue
            applyCenterScalingEffect(child, centerX, recyclerView)
        }
    }

    private fun updateIndicator(position: Int) {
        val count = indicatorContainer.childCount
        if (count == 0) return

        val indicatorIndex = position - 1
        if (indicatorIndex in 0 until count) {
            setCurrentIndicator(indicatorIndex)
        }
    }

    private fun setCurrentIndicator(index: Int) {
        for (i in 0 until indicatorContainer.childCount) {
            val dot = indicatorContainer.getChildAt(i)
            val drawableRes =
                if (i == index) R.drawable.indicator_selected else R.drawable.indicator_unselected
            dot.setBackgroundResource(drawableRes)
        }
    }

    private fun applyCenterScalingEffect(
        child: View,
        centerX: Int,
        recyclerView: RecyclerView,
    ) {
        val childCenterX = (child.left + child.right) / 2
        val distanceFromCenter = (centerX - childCenterX).toFloat()
        val d = abs(distanceFromCenter) / recyclerView.width.coerceAtLeast(1)
        val scale = MIN_SCALE + (1 - d).coerceIn(0f, 1f) * MAX_SCALE_DELTA
        val translationX = distanceFromCenter * TRANSLATION_FACTOR

        child.pivotY = child.height / 2f
        child.translationY = 0f
        child.scaleX = scale
        child.scaleY = scale
        child.translationX = translationX

        // 중심에 가까울수록 불투명, 멀수록 더 투명
        child.z = (1 - d) * MAX_ELEVATION
        child.alpha = MIN_ALPHA + (1 - d) * MAX_ALPHA_DELTA
    }

    // 리스트 중앙에 포지션 배치
    private fun scrollToMiddlePosition() {
        binding.rvHomeRecommend.post {
            val middlePosition = recommendAdapter.currentList.size / 2
            val layoutManager = binding.rvHomeRecommend.layoutManager as LinearLayoutManager
            val recyclerViewCenter = binding.rvHomeRecommend.width / 2
            val itemWidth = (ITEM_WIDTH_DP * resources.displayMetrics.density).toInt()
            val offset = recyclerViewCenter - (itemWidth / 2)
            layoutManager.scrollToPositionWithOffset(middlePosition, offset)
        }
    }

    private fun showToast(message: String?) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToExplore() {
        parentFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container_view, ExploreFragment())
            .addToBackStack(null)
            .commit()

        (requireActivity() as MainActivity).selectTab(R.id.nav_explore)
    }

    private fun navigateToSearch(
        id: Long,
        name: String,
    ) {
        parentFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container_view, SearchFragment())
            .addToBackStack(null)
            .commit()

        (requireActivity() as MainActivity).selectTab(R.id.nav_search)
        parentFragmentManager.executePendingTransactions()

        parentFragmentManager.setFragmentResult(
            CATEGORY_KEY,
            bundleOf(
                CATEGORY_ID_KEY to id,
                CATEGORY_NAME_KEY to name,
            ),
        )
    }

    private fun navigateToPlayerDetail(hearitId: Long) {
        val intent = PlayerDetailActivity.newIntent(requireActivity(), hearitId)
        startActivity(intent)
    }

    override fun onClick(hearitId: Long) {
        navigateToPlayerDetail(hearitId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private companion object {
        private const val MIN_SCALE = 0.85f
        private const val MAX_SCALE_DELTA = 0.15f
        private const val TRANSLATION_FACTOR = 0.2f
        private const val MAX_ELEVATION = 20f
        private const val MIN_ALPHA = 0.3f
        private const val MAX_ALPHA_DELTA = 0.8f
        private const val ITEM_WIDTH_DP = 260
        private const val INDICATOR_SIZE_DP = 8
        private const val INDICATOR_MARGIN_DP = 4
    }
}
