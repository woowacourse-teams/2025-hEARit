package com.onair.hearit.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
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
import com.onair.hearit.presentation.DrawerClickListener
import com.onair.hearit.presentation.detail.PlayerDetailActivity
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
    private val recommendAdapter: RecommendHearitAdapter by lazy { RecommendHearitAdapter(this) }
    private val groupedCategoryAdapter: GroupedCategoryAdapter by lazy { GroupedCategoryAdapter(this) }
    private lateinit var indicatorContainer: LinearLayout
    private val snapHelper = PagerSnapHelper()

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
                        val layoutManager =
                            recyclerView.layoutManager as? LinearLayoutManager ?: return
                        val snapView = snapHelper.findSnapView(layoutManager) ?: return
                        val position = layoutManager.getPosition(snapView)
                        updateCenterEffect(recyclerView)
                        updateIndicator(position)
                    }
                },
            )
        }
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
        setCurrentIndicator(position % count)
    }

    private fun setupIndicator(size: Int) {
        indicatorContainer = binding.indicatorContainer
        indicatorContainer.removeAllViews()

        repeat(size) { index ->
            val dot =
                View(requireContext()).apply {
                    val sizeInPx = (INDICATOR_SIZE_DP * resources.displayMetrics.density).toInt()
                    val marginPx = (INDICATOR_MARGIN_DP * resources.displayMetrics.density).toInt()
                    layoutParams =
                        LinearLayout.LayoutParams(sizeInPx, sizeInPx).apply {
                            marginStart = marginPx
                            marginEnd = marginPx
                        }
                    setBackgroundResource(R.drawable.indicator_unselected)
                    setOnClickListener { scrollToIndex(index) }
                }
            indicatorContainer.addView(dot)
        }
        setCurrentIndicator(0)
    }

    private fun scrollToIndex(index: Int) {
        val layoutManager = binding.rvHomeRecommend.layoutManager as? LinearLayoutManager ?: return
        val recyclerCenter = binding.rvHomeRecommend.width / 2
        val itemWidth = (ITEM_WIDTH_DP * resources.displayMetrics.density).toInt()
        val offset = recyclerCenter - (itemWidth / 2)

        layoutManager.scrollToPositionWithOffset(index, offset)
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

    private fun setupCategoryRecyclerView() {
        binding.rvHomeRecommendCategory.adapter = groupedCategoryAdapter
    }

    private fun observeViewModel() {
        viewModel.userInfo.observe(viewLifecycleOwner) { userInfo ->
            binding.userInfo = userInfo
        }

        viewModel.recommendHearits.observe(viewLifecycleOwner) { recommendItems ->
            recommendAdapter.submitList(recommendItems) {
                scrollToMiddlePosition()
                setupIndicator(recommendItems.size)
            }
        }

        viewModel.groupedCategory.observe(viewLifecycleOwner) { groupedCategory ->
            groupedCategoryAdapter.submitList(groupedCategory)
        }

        viewModel.toastMessage.observe(viewLifecycleOwner) { resId ->
            showToast(getString(resId))
        }
    }

    // 리스트 중앙에 포지션 배치
    private fun scrollToMiddlePosition() {
        binding.rvHomeRecommend.post {
            val middlePosition = recommendAdapter.currentList.size / 2
            val layoutManager = binding.rvHomeRecommend.layoutManager as LinearLayoutManager
            val recyclerViewCenter = binding.rvHomeRecommend.width / 2
            val itemWidth = (260 * resources.displayMetrics.density).toInt()
            val offset = recyclerViewCenter - (itemWidth / 2)
            layoutManager.scrollToPositionWithOffset(middlePosition, offset)
        }
    }

    private fun showToast(message: String?) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
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
