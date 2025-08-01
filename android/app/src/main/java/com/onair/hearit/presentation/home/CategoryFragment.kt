package com.onair.hearit.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.onair.hearit.R
import com.onair.hearit.databinding.FragmentCategoryBinding
import com.onair.hearit.domain.model.SearchInput
import com.onair.hearit.presentation.CategoryClickListener
import com.onair.hearit.presentation.MainActivity
import com.onair.hearit.presentation.search.SearchResultFragment

class CategoryFragment :
    Fragment(),
    CategoryClickListener {
    @Suppress("ktlint:standard:backing-property-naming")
    private var _binding: FragmentCategoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels { HomeViewModelFactory(requireContext()) }
    private val categoryAdapter = CategoryAdapter(this)

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

        setupWindowInsets()
        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }
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

    override fun onCategoryClick(
        categoryId: Long,
        categoryName: String,
    ) {
        navigateToSearchResult(SearchInput.Category(categoryId, categoryName))
    }

    private fun setupRecyclerView() {
        binding.rvCategory.adapter = categoryAdapter
    }

    private fun setupListeners() {
        binding.ibCategoryBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun observeViewModel() {
        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            categoryAdapter.submitList(categories)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
