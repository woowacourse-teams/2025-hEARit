package com.onair.hearit.presentation.home

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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
import com.onair.hearit.domain.model.SearchInput
import com.onair.hearit.presentation.CategoryClickListener
import com.onair.hearit.presentation.DrawerClickListener
import com.onair.hearit.presentation.MainActivity
import com.onair.hearit.presentation.detail.PlayerDetailActivity
import com.onair.hearit.presentation.search.SearchResultFragment
import kotlin.math.abs

class HomeFragment :
    Fragment(),
    RecommendClickListener,
    CategoryClickListener,
    RecentHearitClickListener {
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
    private val categoryAdapter: CategoryAdapter by lazy { CategoryAdapter(this) }
    private lateinit var indicatorContainer: LinearLayout
    private val snapHelper = PagerSnapHelper()
    private val playerDetailLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                viewModel.getRecentHearit()
            }
        }

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
        binding.recentClickListener = this

        setupWindowInsets()
        setupListeners()
        setupRecommendRecyclerView()
        setupCategoryRecyclerView()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        viewModel.getRecentHearit()
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

//        CrashlyticsProvider.get().log("some debug log")
//        CrashlyticsProvider.get().recordException(IllegalStateException("Something went wrong"))
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
                    val sizeInPx = (8 * resources.displayMetrics.density).toInt()
                    layoutParams =
                        LinearLayout.LayoutParams(sizeInPx, sizeInPx).apply {
                            marginStart = sizeInPx / 2
                            marginEnd = sizeInPx / 2
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
        val itemWidth = (260 * resources.displayMetrics.density).toInt()
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

        val scale = 0.85f + (1 - d).coerceIn(0f, 1f) * 0.15f
        val translationX = distanceFromCenter * 0.2f

        child.pivotY = child.height / 2f
        child.translationY = 0f

        child.scaleX = scale
        child.scaleY = scale
        child.translationX = translationX

        // 중심에 가까울수록 불투명, 멀수록 더 투명
        child.z = (1 - d) * 20f
        child.alpha = 0.3f + (1 - d) * 0.8f
    }

    private fun setupCategoryRecyclerView() {
        binding.rvHomeCategory.adapter = categoryAdapter
    }

    private fun observeViewModel() {
        viewModel.userInfo.observe(viewLifecycleOwner) { userInfo ->
            binding.userInfo = userInfo
        }

        viewModel.recentHearit.observe(viewLifecycleOwner) { recentHearit ->
            binding.recentHearit = recentHearit
        }

        viewModel.recommendHearits.observe(viewLifecycleOwner) { recommendItems ->
            recommendAdapter.submitList(recommendItems) {
                scrollToMiddlePosition()
                setupIndicator(recommendItems.size)
            }
        }

        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            categoryAdapter.submitList(categories)
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
        playerDetailLauncher.launch(intent)
    }

    private fun navigateToSearchResult(input: SearchInput) {
        val fragment = SearchResultFragment.newInstance(input)

        (activity as? MainActivity)?.apply {
            selectTab(R.id.nav_search)
        }

        parentFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container_view, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onClickRecentHearit(hearitId: Long) {
        navigateToPlayerDetail(hearitId)
    }

    override fun onClickRecommendHearit(
        hearitId: Long,
        title: String,
    ) {
        navigateToPlayerDetail(hearitId)
        (requireActivity() as? MainActivity)?.showPlayerControlView()
    }

    override fun onCategoryClick(
        id: Long,
        name: String,
    ) {
        navigateToSearchResult(SearchInput.Category(id, name))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
