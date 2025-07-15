package com.onair.hearit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.onair.hearit.databinding.FragmentHomeBinding
import java.time.LocalDateTime

class HomeFragment : Fragment() {
    @Suppress("ktlint:standard:backing-property-naming")
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val recommendAdapter = RecommendHearitAdapter()
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

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        binding.ivProfile.setOnClickListener {
            (activity as? DrawerClickListener)?.openDrawer()
        }

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

        binding.rvHomeRecommendHearit.adapter = recommendAdapter
        val sampleRecommends =
            listOf(
                RecommendHearitItem(
                    1L,
                    "추천 제목 1",
                    getString(R.string.home_example_recommend_description),
                ),
                RecommendHearitItem(
                    2L,
                    "추천 제목 2",
                    getString(R.string.home_example_recommend_description),
                ),
                RecommendHearitItem(
                    3L,
                    "추천 제목 3",
                    getString(R.string.home_example_recommend_description),
                ),
            )
        recommendAdapter.submitList(sampleRecommends)

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

        binding.ivHomeAllCategory.setOnClickListener {
            parentFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container_view, CategoryFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
