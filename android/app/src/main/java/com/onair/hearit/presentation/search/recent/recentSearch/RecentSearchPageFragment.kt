package com.onair.hearit.presentation.search.recent.recentSearch

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.onair.hearit.databinding.FragmentRecentSearchPageBinding
import com.onair.hearit.domain.model.SearchInput
import com.onair.hearit.presentation.search.SearchViewModel
import com.onair.hearit.presentation.search.recent.SearchRecentFragment
import com.onair.hearit.presentation.showToast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RecentSearchPageFragment :
    Fragment(),
    RecentSearchClickListener {
    @Suppress("ktlint:standard:backing-property-naming")
    private var _binding: FragmentRecentSearchPageBinding? = null
    private val binding get() = _binding!!

    private val recentSearchAdapter: RecentSearchAdapter by lazy { RecentSearchAdapter(this) }

    private val viewModel: SearchViewModel by viewModels({ requireParentFragment() })

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
            showToast(resId)
        }
    }

    override fun onRecentSearchClick(term: String) {
        val input: SearchInput = SearchInput.Keyword(term)
        (parentFragment as? SearchRecentFragment)
            ?.showSearchResultPage(input)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
