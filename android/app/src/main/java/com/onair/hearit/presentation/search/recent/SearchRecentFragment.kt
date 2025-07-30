package com.onair.hearit.presentation.search.recent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.onair.hearit.databinding.FragmentSearchRecentBinding
import com.onair.hearit.di.CrashlyticsProvider
import com.onair.hearit.domain.model.SearchInput
import com.onair.hearit.domain.term
import com.onair.hearit.presentation.search.SearchViewModel
import com.onair.hearit.presentation.search.SearchViewModelFactory
import com.onair.hearit.presentation.search.result.SearchResultFragment

class SearchRecentFragment :
    Fragment(),
    KeywordClickListener {
    @Suppress("ktlint:standard:backing-property-naming")
    private var _binding: FragmentSearchRecentBinding? = null
    private val binding get() = _binding!!
    private val recentKeywordAdapter by lazy { RecentKeywordAdapter(this) }
    private val viewModel: SearchViewModel by viewModels {
        SearchViewModelFactory(CrashlyticsProvider.get())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSearchRecentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        setupWindowInsets()
        setupRecentKeywordRecyclerView()
        viewModel.getRecentKeywords()
        observeViewModel()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }
    }

    private fun setupRecentKeywordRecyclerView() {
        binding.rvRecentKeyword.adapter = recentKeywordAdapter
    }

    private fun observeViewModel() {
        viewModel.recentKeywords.observe(viewLifecycleOwner) { keywords ->
            recentKeywordAdapter.submitList(keywords)
        }
    }

    private fun navigateToSearchResult(input: SearchInput) {
        setFragmentResult("recent_keyword", bundleOf("keyword" to input.term()))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onKeywordClick(term: String) {
        navigateToSearchResult(SearchInput.Keyword(term))
    }
}
