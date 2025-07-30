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
        childFragmentManager
            .beginTransaction()
            .replace(R.id.fl_search_container, SearchCategoryFragment())
            .commit()

        setupWindowInsets()
        setupSearchEnterKey()
        setupSearchBarClickListener()
        setupSearchEndIcon()
        observeViewModel()

        binding.ivBack.setOnClickListener {
            childFragmentManager
                .beginTransaction()
                .replace(R.id.fl_search_container, SearchCategoryFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.tvSearchCancel.setOnClickListener {
            binding.etSearch.text?.clear()
            childFragmentManager
                .beginTransaction()
                .replace(R.id.fl_search_container, SearchCategoryFragment())
                .addToBackStack(null)
                .commit()
        }

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

    private fun setupSearchEnterKey() {
        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val searchTerm =
                    binding.etSearch.text
                        .toString()
                        .trim()
                if (searchTerm.isNotBlank()) {
                    navigateToSearchResult(SearchInput.Keyword(searchTerm))
                    hideKeyboard()
                }
            }
            false
        }
    }

    private fun setupSearchEndIcon() {
        binding.tilSearch.setEndIconOnClickListener {
            val searchTerm =
                binding.etSearch.text
                    .toString()
                    .trim()
            if (searchTerm.isNotBlank()) {
                hideKeyboard()
                navigateToSearchResult(SearchInput.Keyword(searchTerm))
            }
        }
    }

    private fun setupSearchBarClickListener() {
        binding.etSearch.setOnClickListener {
            childFragmentManager
                .beginTransaction()
                .replace(R.id.fl_search_container, SearchRecentFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun observeViewModel() {
        viewModel.toastMessage.observe(viewLifecycleOwner) { resId ->
            showToast(getString(resId))
        }
    }

    private fun navigateToSearchResult(input: SearchInput) {
        val fragment = SearchResultFragment.newInstance(input)
        binding.etSearch.setText(input.term())
        childFragmentManager
            .beginTransaction()
            .replace(R.id.fl_search_container, fragment)
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
}
