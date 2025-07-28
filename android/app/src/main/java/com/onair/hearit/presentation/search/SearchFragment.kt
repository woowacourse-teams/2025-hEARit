package com.onair.hearit.presentation.search

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.onair.hearit.R
import com.onair.hearit.analytics.AnalyticsConstants
import com.onair.hearit.analytics.logScreenView
import com.onair.hearit.databinding.FragmentSearchBinding
import com.onair.hearit.di.AnalyticsProvider
import com.onair.hearit.domain.model.SearchInput
import com.onair.hearit.presentation.CategoryClickListener
import com.onair.hearit.presentation.home.CategoryAdapter

class SearchFragment :
    Fragment(),
    CategoryClickListener,
    KeywordClickListener {
    @Suppress("ktlint:standard:backing-property-naming")
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val keywordAdapter by lazy { KeywordAdapter(this) }
    private val categoryAdapter by lazy { CategoryAdapter(this) }
    private val viewModel: SearchViewModel by viewModels { SearchViewModelFactory() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        setupWindowInsets()
        setupSearchEnterKey()
        setKeywordRecyclerView()
        setupCategoryRecyclerView()
        observeViewModel()
        setupSearchEndIcon()

        binding.nsvSearch.setOnTouchListener { _, _ ->
            hideKeyboard()
            false
        }
    }

    override fun onResume() {
        super.onResume()
        AnalyticsProvider.get().logScreenView(
            screenName = AnalyticsConstants.SCREEN_NAME_SEARCH,
            screenClass = AnalyticsConstants.SCREEN_CLASS_SEARCH,
        )
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }
    }

    private fun setupSearchEnterKey() {
        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val searchTerm =
                    binding.etSearch.text
                        .toString()
                        .trim()
                if (searchTerm.isNotBlank()) {
                    navigateToSearchResult(
                        SearchInput.Keyword(searchTerm),
                    )
                    hideKeyboard()
                }
            }
            false
        }
    }

    private fun setKeywordRecyclerView() {
        val layoutManager =
            FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                flexWrap = FlexWrap.WRAP
                justifyContent = JustifyContent.CENTER
            }

        binding.rvKeyword.layoutManager = layoutManager
        binding.rvKeyword.adapter = keywordAdapter
    }

    private fun setupCategoryRecyclerView() {
        binding.rvSearchCategories.adapter = categoryAdapter
    }

    private fun setupSearchEndIcon() {
        binding.tilSearch.setEndIconOnClickListener {
            val searchTerm =
                binding.etSearch.text
                    .toString()
                    .trim()
            if (searchTerm.isNotBlank()) {
                hideKeyboard()
                navigateToSearchResult(
                    SearchInput.Keyword(searchTerm),
                )
            }
        }
    }

    private fun observeViewModel() {
        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            categoryAdapter.submitList(categories)
        }

        viewModel.keywords.observe(viewLifecycleOwner) { keywords ->
            keywordAdapter.submitList(keywords)
        }

        viewModel.toastMessage.observe(viewLifecycleOwner) { resId ->
            showToast(getString(resId))
        }
    }

    private fun navigateToSearchResult(input: SearchInput) {
        val fragment = SearchResultFragment.newInstance(input)

        parentFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container_view, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun hideKeyboard() {
        val inputMethodManager =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = requireActivity().currentFocus ?: binding.root
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun showToast(message: String?) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onKeywordClick(keyword: String) {
        navigateToSearchResult(SearchInput.Keyword(keyword))
        hideKeyboard()
    }

    override fun onCategoryClick(
        categoryId: Long,
        categoryName: String,
    ) {
        navigateToSearchResult(SearchInput.Category(categoryId, categoryName))
        hideKeyboard()
    }
}
