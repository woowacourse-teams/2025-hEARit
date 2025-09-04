package com.onair.hearit.presentation.search.recent.recentSearch

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.onair.hearit.databinding.FragmentRecentSearchPageBinding
import com.onair.hearit.domain.model.SearchInput
import com.onair.hearit.presentation.search.SearchViewModel
import com.onair.hearit.presentation.search.SearchViewModelFactory
import com.onair.hearit.presentation.search.recent.SearchRecentFragment

class RecentSearchPageFragment :
    Fragment(),
    RecentSearchClickListener {
    @Suppress("ktlint:standard:backing-property-naming")
    private var _binding: FragmentRecentSearchPageBinding? = null
    private val binding get() = _binding!!

    private val recentSearchAdapter: RecentSearchAdapter by lazy { RecentSearchAdapter(this) }

    private val viewModel: SearchViewModel by viewModels({ requireActivity() }) {
        SearchViewModelFactory(null)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentRecentSearchPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        setupWindowInsets()
        setupListeners()
        setupRecyclerView()
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

    private fun setupListeners() {
        binding.tvSearchRecentDelete.setOnClickListener {
            viewModel.deleteKeywords()
        }
    }

    private fun setupRecyclerView() {
        binding.rvRecentKeyword.adapter = recentSearchAdapter
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
        (parentFragment as? SearchRecentFragment)?.showSearchResultPage(input)
        Log.d("meeple_log", "click")
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
}
