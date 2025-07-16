package com.onair.hearit.presentation.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.onair.hearit.databinding.FragmentSearchBinding
import com.onair.hearit.domain.CategoryItem
import com.onair.hearit.domain.KeywordItem
import com.onair.hearit.presentation.home.CategoryAdapter

class SearchFragment : Fragment() {
    @Suppress("ktlint:standard:backing-property-naming")
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val adapter = KeywordAdapter()
    private val categoryAdapter = CategoryAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
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

        setKeywordRecyclerView()
        setCategoriesRecyclerView()
    }

    private fun setKeywordRecyclerView() {
        val layoutManager =
            FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                flexWrap = FlexWrap.WRAP
                justifyContent = JustifyContent.CENTER
            }

        binding.rvKeyword.layoutManager = layoutManager
        binding.rvKeyword.adapter = this.adapter

        val keywords: List<KeywordItem> =
            listOf(
                KeywordItem("# Kotlin"),
                KeywordItem("# Spring"),
                KeywordItem("# NotebookLM"),
                KeywordItem("# Parcelable"),
                KeywordItem("#Serialization"),
                KeywordItem("# hearit"),
                KeywordItem("# JPA"),
                KeywordItem("# Activity"),
                KeywordItem("# HTTP"),
            )

        adapter.submitList(keywords)
    }

    private fun setCategoriesRecyclerView() {
        binding.rvSearchCategories.adapter = categoryAdapter
        val sampleCategories =
            List(20) { i ->
                val colors = if (i % 2 == 0) "#9533F5" else "#B2B4B6"
                CategoryItem(
                    id = i.toLong(),
                    color = colors,
                    category = "카테고리 $i",
                )
            }
        categoryAdapter.submitList(sampleCategories)
    }
}
