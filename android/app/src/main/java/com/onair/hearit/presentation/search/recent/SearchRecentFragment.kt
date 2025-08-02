package com.onair.hearit.presentation.search.recent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.onair.hearit.databinding.FragmentSearchRecentBinding
import com.onair.hearit.di.CrashlyticsProvider
import com.onair.hearit.domain.model.SearchInput
import com.onair.hearit.domain.model.SearchInput.Companion.KEYWORD_KEY
import com.onair.hearit.presentation.search.SearchViewModel
import com.onair.hearit.presentation.search.SearchViewModelFactory

class SearchRecentFragment :
    Fragment(),
    RecentSearchClickListener {
    @Suppress("ktlint:standard:backing-property-naming")
    private var _binding: FragmentSearchRecentBinding? = null
    private val binding get() = _binding!!

    private val recentSearchAdapter by lazy { RecentSearchAdapter(this) }

    private val viewModel: SearchViewModel by viewModels({ requireParentFragment() }) {
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
        setupRecyclerView()
        setupDeleteButton()
        observeViewModel()
        viewModel.getRecentKeywords()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }
    }

    private fun setupRecyclerView() {
        binding.rvRecentKeyword.adapter = recentSearchAdapter
    }

    private fun setupDeleteButton() {
        binding.tvSearchRecentDelete.setOnClickListener {
            viewModel.deleteKeywords()
        }
    }

    private fun observeViewModel() {
        viewModel.recentKeywords.observe(viewLifecycleOwner) { keywords ->
            recentSearchAdapter.submitList(keywords)
        }
        viewModel.toastMessage.observe(viewLifecycleOwner) { resId ->
            showToast(getString(resId))
        }
    }

    private fun navigateToSearchResult(input: SearchInput) {
        setFragmentResult(KEYWORD_KEY, input.toBundle())
    }

    private fun showToast(message: String?) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onRecentSearchClick(term: String) {
        navigateToSearchResult(SearchInput.Keyword(term))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): SearchRecentFragment = SearchRecentFragment()
    }
}
