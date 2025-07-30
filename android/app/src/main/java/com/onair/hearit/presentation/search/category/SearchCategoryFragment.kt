package com.onair.hearit.presentation.search.category

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.onair.hearit.R
import com.onair.hearit.analytics.AnalyticsEventNames
import com.onair.hearit.analytics.AnalyticsParamKeys
import com.onair.hearit.analytics.AnalyticsScreenInfo
import com.onair.hearit.databinding.FragmentSearchCategoryBinding
import com.onair.hearit.di.AnalyticsProvider
import com.onair.hearit.di.CrashlyticsProvider
import com.onair.hearit.domain.model.SearchInput
import com.onair.hearit.presentation.CategoryClickListener
import com.onair.hearit.presentation.search.SearchResultFragment
import com.onair.hearit.presentation.search.SearchViewModel
import com.onair.hearit.presentation.search.SearchViewModelFactory

class SearchCategoryFragment :
    Fragment(),
    CategoryClickListener {
    @Suppress("ktlint:standard:backing-property-naming")
    private var _binding: FragmentSearchCategoryBinding? = null
    private val binding get() = _binding!!
    private val categoryAdapter by lazy { CategoryAdapter(this) }
    private val viewModel: SearchViewModel by viewModels {
        SearchViewModelFactory(CrashlyticsProvider.get())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSearchCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        setupWindowInsets()
        setupCategoryRecyclerView()
        observeViewModel()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }
    }

    private fun setupCategoryRecyclerView() {
        binding.rvSearchCategories.adapter = categoryAdapter
    }

    private fun observeViewModel() {
        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            categoryAdapter.submitList(categories)
        }
    }

    private fun navigateToSearchResult(input: SearchInput) {
        val fragment = SearchResultFragment.newInstance(input)

        parentFragmentManager
            .beginTransaction()
            .replace(R.id.fl_search_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCategoryClick(
        id: Long,
        name: String,
    ) {
        AnalyticsProvider.get().logEvent(
            AnalyticsEventNames.SEARCH_CATEGORY_SELECTED,
            mapOf(
                AnalyticsParamKeys.CATEGORY_NAME to name,
                AnalyticsParamKeys.SCREEN_NAME to AnalyticsScreenInfo.Search.NAME,
            ),
        )

        navigateToSearchResult(SearchInput.Category(id, name))
    }
}
