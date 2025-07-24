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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.databinding.FragmentSearchResultBinding
import com.onair.hearit.presentation.detail.PlayerDetailActivity
import com.onair.hearit.presentation.explore.ShortsClickListener

class SearchResultFragment :
    Fragment(),
    ShortsClickListener {
    @Suppress("ktlint:standard:backing-property-naming")
    private var _binding: FragmentSearchResultBinding? = null
    private val binding get() = _binding!!

    private val searchTerm: String by lazy {
        arguments?.getString("searchTerm") ?: ""
    }

    private val viewModel: SearchResultViewModel by viewModels {
        SearchResultViewModelFactory(searchTerm)
    }
    private val adapter by lazy { SearchedHearitAdapter(this) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentSearchResultBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
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
        setupRecyclerView()
        observeViewModel()
        setupSearchEndIcon()

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

    private fun setupRecyclerView() {
        binding.rvSearchedHearit.adapter = adapter

        binding.rvSearchedHearit.addOnScrollListener(
            object : RecyclerView.OnScrollListener() {
                override fun onScrolled(
                    recyclerView: RecyclerView,
                    dx: Int,
                    dy: Int,
                ) {
                    super.onScrolled(recyclerView, dx, dy)

                    val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return
                    val totalItemCount = layoutManager.itemCount
                    val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                    if (lastVisibleItem >= totalItemCount - 3) {
                        viewModel.loadNextPageIfPossible()
                    }
                }
            },
        )
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

    private fun setupSearchEndIcon() {
        binding.tilSearchResult.setEndIconOnClickListener {
            val searchTerm =
                binding.etSearchResult.text
                    .toString()
                    .trim()
            if (searchTerm.isNotBlank()) {
                hideKeyboard()
                viewModel.search(searchTerm)
            }
        }
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { uiState ->
            binding.uiState = uiState
        }

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

    override fun onClickHearitInfo(hearitId: Long) {
        val intent = PlayerDetailActivity.newIntent(requireActivity(), hearitId)
        startActivity(intent)
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
