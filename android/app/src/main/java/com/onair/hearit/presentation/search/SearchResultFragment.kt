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
import com.onair.hearit.databinding.FragmentSearchResultBinding

class SearchResultFragment : Fragment() {
    @Suppress("ktlint:standard:backing-property-naming")
    private var _binding: FragmentSearchResultBinding? = null
    private val binding get() = _binding!!

    private val searchTerm: String by lazy {
        arguments?.getString("searchTerm") ?: ""
    }

    private val viewModel: SearchResultViewModel by viewModels {
        SearchResultViewModelFactory(searchTerm)
    }
    private val adapter by lazy { SearchedHearitAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentSearchResultBinding.inflate(inflater, container, false)
        binding.rvSearchedHearit.adapter = adapter
        binding.etSearchResult.setText(searchTerm)
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
        observeViewModel()

        binding.nsvSearchResult.setOnTouchListener { v, event ->
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
        binding.etSearchResult.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val searchTerm =
                    binding.etSearchResult.text
                        .toString()
                        .trim()
                if (searchTerm.isNotBlank() && searchTerm != viewModel.currentSearchTerm) {
                    hideKeyboard()
                    viewModel.search(searchTerm)
                    true
                }
            }
            false
        }
    }

    private fun observeViewModel() {
        viewModel.searchedHearits.observe(viewLifecycleOwner) { searchedHearits ->
            adapter.submitList(searchedHearits)
        }

        viewModel.toastMessage.observe(viewLifecycleOwner) { resId ->
            showToast(getString(resId))
        }
    }

    private fun showToast(message: String?) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun hideKeyboard() {
        val inputMethodManager =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = requireActivity().currentFocus ?: binding.root
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(searchTerm: String): SearchResultFragment =
            SearchResultFragment().apply {
                arguments =
                    Bundle().apply {
                        putString("searchTerm", searchTerm)
                    }
            }
    }
}
