package com.onair.hearit.presentation.search.recent.searchResult

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.analytics.AnalyticsEventNames
import com.onair.hearit.analytics.AnalyticsLogger
import com.onair.hearit.analytics.AnalyticsParamKeys.ITEM_ID
import com.onair.hearit.analytics.HearitSource
import com.onair.hearit.databinding.FragmentSearchResultPageBinding
import com.onair.hearit.presentation.HearitClickListener
import com.onair.hearit.presentation.detail.PlayerDetailActivity
import com.onair.hearit.presentation.main.MainActivity
import com.onair.hearit.presentation.main.MainViewModel
import com.onair.hearit.presentation.search.SearchViewModel
import com.onair.hearit.presentation.showToast
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SearchResultPageFragment :
    Fragment(),
    HearitClickListener {
    @Suppress("ktlint:standard:backing-property-naming")
    private var _binding: FragmentSearchResultPageBinding? = null
    private val binding get() = _binding!!

    private val mainViewModel: MainViewModel by activityViewModels()

    private val viewModel: SearchViewModel by activityViewModels()

    private val searchedAdapter: SearchedHearitAdapter by lazy { SearchedHearitAdapter(this) }

    @Inject
    lateinit var analyticsLogger: AnalyticsLogger

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSearchResultPageBinding.inflate(inflater, container, false)
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
        fetchData()
        observeViewModel()
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
                        recyclerView: RecyclerView,
                        dx: Int,
                        dy: Int,
                    ) {
                        val layoutManager =
                            recyclerView.layoutManager as? LinearLayoutManager ?: return
                        val threshold = layoutManager.itemCount - REFRESH_THRESHOLD
                        val last = layoutManager.findLastVisibleItemPosition()

                        if (last >= threshold) viewModel.loadNextPageIfPossible()
                    }
                },
            )
        }
    }

    private fun fetchData() {
        viewModel.fetchResultData(true)
    }

    private fun observeViewModel() {
        mainViewModel.hearitUpdated.observe(viewLifecycleOwner) {
            viewModel.refreshSearchResults()
        }

        viewModel.searchUiState.observe(viewLifecycleOwner) { state ->
            binding.uiState = state
        }
        viewModel.searchedHearits.observe(viewLifecycleOwner) { searchedHearits ->
            searchedAdapter.submitList(searchedHearits)
        }
        viewModel.toastMessage.observe(viewLifecycleOwner) { resId ->
            showToast(resId)
        }
    }

    override fun onClick(
        hearitId: Long,
        source: HearitSource,
    ) {
        analyticsLogger.logEvent(
            AnalyticsEventNames.SEARCH_HEARIT_SELECTED,
            mapOf(ITEM_ID to hearitId.toString()),
        )

        val intent = PlayerDetailActivity.newIntent(requireActivity(), hearitId)
        (activity as? MainActivity)?.launchDetailActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val REFRESH_THRESHOLD = 3
    }
}
