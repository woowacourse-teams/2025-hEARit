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
import com.onair.hearit.R
import com.onair.hearit.analytics.AnalyticsScreenInfo
import com.onair.hearit.databinding.FragmentSearchBinding
import com.onair.hearit.di.AnalyticsProvider
import com.onair.hearit.di.CrashlyticsProvider
import com.onair.hearit.domain.model.SearchInput
import com.onair.hearit.domain.term
import com.onair.hearit.presentation.search.category.SearchCategoryFragment
import com.onair.hearit.presentation.search.recent.SearchRecentFragment
import com.onair.hearit.presentation.search.result.SearchResultFragment

class SearchFragment : Fragment() {
    @Suppress("ktlint:standard:backing-property-naming")
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SearchViewModel by viewModels {
        SearchViewModelFactory(CrashlyticsProvider.get())
    }
    private val categoryFragment by lazy { SearchCategoryFragment.newInstance() }
    private val recentFragment by lazy { SearchRecentFragment.newInstance() }

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
        showCategoryFragment()
        setupWindowInsets()
        setupSearchInput()
        observeViewModel()
        setupFragmentResultListeners()
        setupBackAndCancelButtons()
        updateAppBarUIOnBackStackChanged()
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

        binding.etSearch.setOnClickListener {
            showRecentFragment()
        }
    }

    private fun performSearchFromInput() {
        val searchTerm =
            binding.etSearch.text
                ?.toString()
                ?.trim()
                .orEmpty()
        if (searchTerm.isNotBlank()) {
            navigateToSearchResult(SearchInput.Keyword(searchTerm))
            hideKeyboard()
        }
    }

    private fun setupFragmentResultListeners() {
        childFragmentManager.setFragmentResultListener(
            KEY_RECENT_SEARCH,
            viewLifecycleOwner,
        ) { _, bundle ->
            val keyword = bundle.getString(KEY_RECENT_SEARCH).orEmpty()
            navigateToSearchResult(SearchInput.Keyword(keyword))
        }

        childFragmentManager.setFragmentResultListener(
            KEY_CATEGORY,
            viewLifecycleOwner,
        ) { _, bundle ->
            val category = bundle.getString(KEY_CATEGORY).orEmpty()
            navigateToSearchResult(SearchInput.Keyword(category))
        }
    }

    private fun setupBackAndCancelButtons() {
        binding.ivBack.setOnClickListener {
            showCategoryFragment()
        }

        binding.tvSearchCancel.setOnClickListener {
            binding.etSearch.text?.clear()
            showCategoryFragment()
        }
    }

    private fun updateAppBarUIOnBackStackChanged() {
        childFragmentManager.addOnBackStackChangedListener {
            updateAppBarUI()
        }
        updateAppBarUI()
    }

    private fun updateAppBarUI() {
        val currentFragment =
            childFragmentManager.findFragmentById(R.id.fl_search_container)

        when (currentFragment) {
            is SearchCategoryFragment -> {
                binding.tvSearchLogo.visibility = View.VISIBLE
                binding.ivBack.visibility = View.GONE
                binding.tvSearchCancel.visibility = View.GONE
            }

            is SearchRecentFragment -> {
                binding.tvSearchLogo.visibility = View.VISIBLE
                binding.ivBack.visibility = View.GONE
                binding.tvSearchCancel.visibility = View.VISIBLE
            }

            is SearchResultFragment -> {
                binding.tvSearchLogo.visibility = View.GONE
                binding.ivBack.visibility = View.VISIBLE
                binding.tvSearchCancel.visibility = View.GONE
            }
        }
    }

    private fun showCategoryFragment() {
        binding.etSearch.text?.clear()
        replaceFragment(categoryFragment, TAG_SEARCH_CATEGORY)
    }

    private fun showRecentFragment() {
        replaceFragment(recentFragment, TAG_SEARCH_RECENT)
    }

    private fun navigateToSearchResult(input: SearchInput) {
        binding.etSearch.setText(input.term())
        binding.etSearch.setSelection(binding.etSearch.text?.length ?: 0)
        replaceFragment(SearchResultFragment.newInstance(input), TAG_SEARCH_RESULT)
    }

    private fun replaceFragment(
        fragment: Fragment,
        tag: String,
    ) {
        val existingFragment = childFragmentManager.findFragmentByTag(tag)
        val transaction = childFragmentManager.beginTransaction()

        if (existingFragment != null) {
            transaction
                .replace(R.id.fl_search_container, existingFragment, tag)
        } else {
            transaction
                .replace(R.id.fl_search_container, fragment, tag)
        }

        transaction.addToBackStack(tag).commit()
    }

    override fun onResume() {
        super.onResume()
        AnalyticsProvider.get().logScreenView(
            screenName = AnalyticsScreenInfo.Search.NAME,
            screenClass = AnalyticsScreenInfo.Search.CLASS,
        )
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }
    }

    private fun hideKeyboard() {
        val inputMethodManager =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = requireActivity().currentFocus ?: binding.root
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun observeViewModel() {
        viewModel.toastMessage.observe(viewLifecycleOwner) { resId ->
            showToast(getString(resId))
        }
    }

    private fun showToast(message: String?) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG_SEARCH_CATEGORY = "SearchCategory"
        private const val TAG_SEARCH_RECENT = "SearchRecent"
        private const val TAG_SEARCH_RESULT = "SearchResult"
        const val KEY_CATEGORY = "category"
        const val KEY_RECENT_SEARCH = "recent_search"
    }
}
