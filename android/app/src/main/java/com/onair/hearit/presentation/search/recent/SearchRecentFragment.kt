package com.onair.hearit.presentation.search.recent

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.onair.hearit.databinding.FragmentSearchRecentBinding
import com.onair.hearit.domain.model.SearchInput
import com.onair.hearit.presentation.IntentKeys.KEYWORD_KEY
import com.onair.hearit.presentation.search.SearchViewModel
import com.onair.hearit.presentation.search.SearchViewModelFactory

class SearchRecentFragment :
    Fragment(),
    RecentSearchClickListener {
    @Suppress("ktlint:standard:backing-property-naming")
    private var _binding: FragmentSearchRecentBinding? = null
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
        _binding = FragmentSearchRecentBinding.inflate(inflater, container, false)
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
        setupSearchInput()
        focusSearch()
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
        binding.ibSearchRecentBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.tvSearchRecentDelete.setOnClickListener {
            viewModel.deleteKeywords()
        }
    }

    private fun setupRecyclerView() {
        binding.rvRecentKeyword.adapter = recentSearchAdapter
    }

    private fun setupSearchInput() {
        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearchFromInput()
            }
            false
        }
        binding.tilSearch.setEndIconOnClickListener {
            performSearchFromInput()
        }
    }

    private fun performSearchFromInput() {
        binding.etSearch.text
            ?.toString()
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let { searchTerm ->
                navigateToSearchResult(SearchInput.Keyword(searchTerm))
//                hideKeyboard()
            }
    }

    private fun focusSearch() {
        binding.etSearch.viewTreeObserver.addOnGlobalLayoutListener(
            object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    binding.etSearch.requestFocus()
                    val imm =
                        requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(binding.etSearch, InputMethodManager.SHOW_IMPLICIT)
                    binding.etSearch.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            },
        )
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
