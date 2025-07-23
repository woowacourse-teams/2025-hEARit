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
import com.onair.hearit.databinding.FragmentSearchBinding
import com.onair.hearit.domain.model.Keyword
import com.onair.hearit.presentation.home.CategoryAdapter

class SearchFragment : Fragment() {
    @Suppress("ktlint:standard:backing-property-naming")
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val adapter = KeywordAdapter()
    private val categoryAdapter = CategoryAdapter()
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

        binding.nsvSearch.setOnTouchListener { _, _ ->
            hideKeyboard()
            false
        }
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
                    navigateToSearchResult(searchTerm)
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
        binding.rvKeyword.adapter = this.adapter

        val keywords: List<Keyword> =
            listOf(
                Keyword("# Kotlin"),
                Keyword("# Spring"),
                Keyword("# NotebookLM"),
                Keyword("# Parcelable"),
                Keyword("# Serialization"),
                Keyword("# hearit"),
                Keyword("# JPA"),
                Keyword("# Activity"),
                Keyword("# HTTP"),
            )

        adapter.submitList(keywords)
    }

    private fun setupCategoryRecyclerView() {
        binding.rvSearchCategories.adapter = categoryAdapter
    }

    private fun observeViewModel() {
        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            categoryAdapter.submitList(categories)
        }

        viewModel.toastMessage.observe(viewLifecycleOwner) { resId ->
            showToast(getString(resId))
        }
    }

    private fun navigateToSearchResult(searchTerm: String) {
        val fragment = SearchResultFragment.newInstance(searchTerm)

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
}
