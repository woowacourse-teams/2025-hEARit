package com.onair.hearit.presentation.search.recent

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.onair.hearit.R
import com.onair.hearit.databinding.FragmentSearchRecentBinding
import com.onair.hearit.domain.model.SearchInput
import com.onair.hearit.domain.term
import com.onair.hearit.presentation.IntentKeys.KEYWORD_KEY
import com.onair.hearit.presentation.search.SearchViewModel
import com.onair.hearit.presentation.search.SearchViewModelFactory
import com.onair.hearit.presentation.search.recent.recentSearch.RecentSearchAdapter
import com.onair.hearit.presentation.search.recent.recentSearch.RecentSearchClickListener
import com.onair.hearit.presentation.search.recent.recentSearch.RecentSearchPageFragment
import com.onair.hearit.presentation.search.recent.searchResult.SearchResultPageFragment
import kotlinx.coroutines.launch

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
    private var globalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null
    private var lastSearchTerm: String? = null
    private val initialKeyword: String?
        get() = arguments?.getString(KEYWORD_KEY)

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
        observeViewModel()
        navigateToRecent()
        viewModel.getRecentKeywords()

        initialKeyword?.let { term ->
            binding.etSearch.setText(term)
            binding.etSearch.setSelection(term.length)
            viewLifecycleOwner.lifecycleScope.launch {
                navigateToSearchResult(SearchInput.Keyword(term))
            }
        }
    }

    fun showSearchResultPage(input: SearchInput) {
        updateSearchInput(input.term())
        showSearchResultFragment(input)
        viewModel.saveRecentKeyword(input.term())
        hideKeyboard()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupListeners() {
        binding.ibSearchRecentBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        setupKeyboardAutoFocus()
        setupSearchInputListeners()
    }

    private fun setupKeyboardAutoFocus() {
        globalLayoutListener =
            ViewTreeObserver.OnGlobalLayoutListener {
                binding.etSearch.requestFocus()
                showKeyboard()
                binding.etSearch.viewTreeObserver.removeOnGlobalLayoutListener(globalLayoutListener)
                globalLayoutListener = null
            }
        binding.etSearch.viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupSearchInputListeners() {
        // 검색창 터치시 최근 검색으로 이동
        binding.etSearch.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                navigateToRecent()
            }
            false
        }

        // 키보드 검색 버튼 클릭시
        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearchFromInput()
            }
            false
        }

        binding.btnSearch.setOnClickListener {
            performSearchFromInput()
        }
    }

    private fun performSearchFromInput() {
        val searchTerm =
            binding.etSearch.text
                ?.toString()
                ?.trim()
        if (searchTerm.isNullOrEmpty()) return
        if (searchTerm == lastSearchTerm) return

        lastSearchTerm = searchTerm
        viewModel.saveRecentKeyword(searchTerm)
        navigateToSearchResult(SearchInput.Keyword(searchTerm))
        hideKeyboard()
    }

    private fun updateSearchInput(term: String) {
        binding.etSearch.setText(term)
        binding.etSearch.setSelection(term.length)
        lastSearchTerm = term
    }

    private fun observeViewModel() {
        viewModel.recentKeywords.observe(viewLifecycleOwner) { keywords ->
            recentSearchAdapter.submitList(keywords)
        }
        viewModel.toastMessage.observe(viewLifecycleOwner) { resId ->
            showToast(getString(resId))
        }
    }

    private fun showSearchResultFragment(input: SearchInput) {
        childFragmentManager
            .beginTransaction()
            .replace(
                R.id.fragment_search_container_view,
                SearchResultPageFragment.newInstance(input),
            ).commit()
    }

    private fun navigateToRecent() {
        childFragmentManager
            .beginTransaction()
            .replace(
                R.id.fragment_search_container_view,
                RecentSearchPageFragment(),
            ).commit()
    }

    private fun navigateToSearchResult(input: SearchInput) {
        childFragmentManager
            .beginTransaction()
            .replace(
                R.id.fragment_search_container_view,
                SearchResultPageFragment.newInstance(input),
            ).commit()
    }

    private fun showToast(message: String?) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun showKeyboard() {
        val inputMethodManager =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(binding.etSearch, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard() {
        val inputMethodManager =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = requireActivity().currentFocus ?: binding.root
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onRecentSearchClick(term: String) {
        navigateToSearchResult(SearchInput.Keyword(term))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        globalLayoutListener?.let {
            binding.etSearch.viewTreeObserver.removeOnGlobalLayoutListener(it)
        }
        _binding = null
    }

    companion object {
        fun newInstance(term: String): SearchRecentFragment =
            SearchRecentFragment().apply {
                arguments =
                    Bundle().apply {
                        putString(KEYWORD_KEY, term)
                    }
            }
    }
}
