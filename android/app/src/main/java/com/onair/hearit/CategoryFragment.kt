package com.onair.hearit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.onair.hearit.databinding.FragmentCategoryBinding

class CategoryFragment : Fragment() {
    @Suppress("ktlint:standard:backing-property-naming")
    private var _binding: FragmentCategoryBinding? = null
    private val binding get() = _binding!!

    private val categoryAdapter = CategoryAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvCategory.adapter = categoryAdapter
        val sampleCategories =
            List(8) { i ->
                val colors = if (i % 2 == 0) "#9533F5" else "#B2B4B6"
                CategoryItem(
                    id = i.toLong(),
                    color = colors,
                    category = "카테고리 $i",
                )
            }
        categoryAdapter.submitList(sampleCategories)

        binding.ibCategoryBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
