package com.onair.hearit.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.R
import com.onair.hearit.databinding.FragmentHomeBinding
import com.onair.hearit.domain.CategoryItem
import com.onair.hearit.domain.HearitItem
import com.onair.hearit.presentation.DrawerClickListener
import com.onair.hearit.presentation.detail.PlayerDetailActivity
import java.time.LocalDateTime
import kotlin.math.abs

class HomeFragment :
    Fragment(),
    RecommendClickListener {
    @Suppress("ktlint:standard:backing-property-naming")
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels { HomeViewModelFactory() }
    private lateinit var recommendAdapter: RecommendHearitAdapter
    private val categoryAdapter = CategoryAdapter()

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
        recommendAdapter = RecommendHearitAdapter(this)

        setupInsets()
        setupListeners()
        setupRecentHearit()
        setupRecommendRecyclerView()
        setupCategoryRecyclerView()
        observeViewModel()
    }

    private fun setupInsets() {
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

        binding.ivHomeRecentHearit.setOnClickListener {
            navigateToPlayerDetail()
        }
    }

    private fun setupRecentHearit() {
        binding.recentHearit =
            HearitItem(
                id = 1L,
                title = "최근 들은 히어릿 제목",
                summary = "summary",
                audioUrl = "a",
                scriptUrl = "a",
                playTime = 123,
                categoryId = 12,
                createdAt = LocalDateTime.now(),
            )
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
        val sampleCategories =
            List(6) { i ->
                val colors = if (i % 2 == 0) "#9533F5" else "#B2B4B6"
                CategoryItem(
                    id = i.toLong(),
                    color = colors,
                    category = "카테고리 $i",
                )
            }
        categoryAdapter.submitList(sampleCategories)
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
        viewModel.recommendHearits.observe(viewLifecycleOwner) { recommendItems ->
            val repeatedItems = List(30) { index -> recommendItems[index % recommendItems.size] }
            recommendAdapter.submitList(repeatedItems) {
                scrollToMiddlePosition()
            }
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

    private fun navigateToPlayerDetail() {
        val intent = PlayerDetailActivity.newIntent(requireActivity())
        startActivity(intent)
    }

    override fun onClickRecommendHearit() {
        navigateToPlayerDetail()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
