package com.onair.hearit.presentation.home

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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.R
import com.onair.hearit.databinding.FragmentHomeBinding
import com.onair.hearit.presentation.CategoryClickListener
import com.onair.hearit.presentation.DrawerClickListener
import com.onair.hearit.presentation.MainActivity
import com.onair.hearit.presentation.detail.PlayerDetailActivity
import com.onair.hearit.presentation.search.SearchResultFragment
import kotlin.math.abs

class HomeFragment :
    Fragment(),
    RecommendClickListener,
    CategoryClickListener {
    @Suppress("ktlint:standard:backing-property-naming")
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels { HomeViewModelFactory(requireContext()) }
    private val recommendAdapter: RecommendHearitAdapter by lazy { RecommendHearitAdapter(this) }
    private val categoryAdapter: CategoryAdapter by lazy { CategoryAdapter(this) }

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

        binding.tvHomeNoRecentHearitText.setOnClickListener {
            (activity as? MainActivity)?.apply {
                selectTab(R.id.nav_explore)
            }
        }
        setupWindowInsets()
        setupListeners()
        setupRecommendRecyclerView()
        setupCategoryRecyclerView()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        viewModel.getRecentHearit()
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

        binding.ivHomeAllCategory.setOnClickListener {
            parentFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container_view, CategoryFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun setupRecommendRecyclerView() {
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(binding.rvHomeRecommendHearit)
        binding.rvHomeRecommendHearit.adapter = recommendAdapter

        // 중심 아이템 강조 효과
        binding.rvHomeRecommendHearit.addOnScrollListener(
            object : RecyclerView.OnScrollListener() {
                override fun onScrolled(
                    recyclerView: RecyclerView,
                    dx: Int,
                    dy: Int,
                ) {
                    val centerX = recyclerView.width / 2
                    for (i in 0 until recyclerView.childCount) {
                        val child = recyclerView.getChildAt(i) ?: continue
                        applyCenterScalingEffect(child, centerX, recyclerView)
                    }
                }
            },
        )
    }

    private fun setupCategoryRecyclerView() {
        binding.rvHomeCategory.adapter = categoryAdapter
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
        child.z = (1 - d) * 10f
        child.alpha = 0.5f + (1 - d) * 0.5f
    }

    private fun observeViewModel() {
        viewModel.userInfo.observe(viewLifecycleOwner) { userInfo ->
            binding.userInfo = userInfo
        }

        viewModel.recentHearit.observe(viewLifecycleOwner) { recentHearit ->
            binding.recentHearit = recentHearit
        }

        viewModel.recommendHearits.observe(viewLifecycleOwner) { recommendItems ->
            val repeatedItems = List(30) { index -> recommendItems[index % recommendItems.size] }
            recommendAdapter.submitList(repeatedItems) {
                scrollToMiddlePosition()
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
        binding.rvHomeRecommendHearit.post {
            val middlePosition = recommendAdapter.currentList.size / 2
            val layoutManager = binding.rvHomeRecommendHearit.layoutManager as LinearLayoutManager
            val recyclerViewCenter = binding.rvHomeRecommendHearit.width / 2
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

    private val playerDetailLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                viewModel.getRecentHearit()
            }
        }

    override fun onClickRecommendHearit(
        hearitId: Long,
        title: String,
    ) {
        navigateToPlayerDetail(hearitId)
        viewModel.saveRecentHearit(hearitId, title)
        (requireActivity() as? MainActivity)?.showPlayerControlView()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun navigateToSearchResult(searchTerm: String) {
        val fragment = SearchResultFragment.newInstance(searchTerm)

        (activity as? MainActivity)?.apply {
            selectTab(R.id.nav_search)
        }

        parentFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container_view, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onCategoryClick(category: String) {
        navigateToSearchResult(category)
    }
}
