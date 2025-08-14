package com.onair.hearit.presentation.search.result

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.databinding.FragmentSearchResultBinding
import com.onair.hearit.domain.model.SearchInput
import com.onair.hearit.presentation.detail.PlayerDetailActivity
import com.onair.hearit.presentation.home.HearitClickListener

class SearchResultFragment :
    Fragment(),
    HearitClickListener {
    @Suppress("ktlint:standard:backing-property-naming")
    private var _binding: FragmentSearchResultBinding? = null
    private val binding get() = _binding!!

    private val searchedTerm: SearchInput by lazy {
        SearchInput.from(requireArguments())
    }

    private val viewModel: SearchResultViewModel by viewModels {
        SearchResultViewModelFactory(searchedTerm)
    }
    private val searchedAdapter: SearchedHearitAdapter by lazy { SearchedHearitAdapter(this) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSearchResultBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        setupWindowInsets()
        setupRecyclerView()
        observeViewModel()
        binding.nsvSearchResult.setOnTouchListener { _, _ ->
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
        binding.rvSearchedHearit.apply {
            adapter = searchedAdapter
            addOnScrollListener(
                object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(
                        rv: RecyclerView,
                        dx: Int,
                        dy: Int,
                    ) {
                        val lm = rv.layoutManager as? LinearLayoutManager ?: return
                        val total = lm.itemCount
                        val last = lm.findLastVisibleItemPosition()

                        if (last >= total - 3) viewModel.loadNextPageIfPossible()
                    }
                },
            )
        }
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            binding.uiState = state
        }
        viewModel.searchedHearits.observe(viewLifecycleOwner) { searchedHearits ->
            searchedAdapter.submitList(searchedHearits)
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

    override fun onClick(hearitId: Long) {
        val intent = PlayerDetailActivity.newIntent(requireActivity(), hearitId)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(input: SearchInput): SearchResultFragment =
            SearchResultFragment().apply {
                arguments = input.toBundle()
            }
    }
}
