package com.onair.hearit.presentation.search.category

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.R
import com.onair.hearit.databinding.FragmentSearchCategoryBinding
import com.onair.hearit.domain.model.SearchInput
import com.onair.hearit.presentation.HearitClickListener
import com.onair.hearit.presentation.IntentKeys.CATEGORY_COLOR_KEY
import com.onair.hearit.presentation.IntentKeys.CATEGORY_NAME_KEY
import com.onair.hearit.presentation.detail.PlayerDetailActivity
import com.onair.hearit.presentation.main.MainActivity
import com.onair.hearit.presentation.search.SearchViewModel
import com.onair.hearit.presentation.search.SearchViewModelFactory
import com.onair.hearit.presentation.search.recent.searchResult.SearchedHearitAdapter

class SearchCategoryFragment :
    Fragment(),
    HearitClickListener {
    @Suppress("ktlint:standard:backing-property-naming")
    private var _binding: FragmentSearchCategoryBinding? = null
    private val binding get() = _binding!!

    private val categoryName: String by lazy {
        requireArguments().getString(CATEGORY_NAME_KEY) ?: "카테고리"
    }
    private val categoryColor: String by lazy {
        requireArguments().getString(CATEGORY_COLOR_KEY) ?: "#000000"
    }

    private val viewModel: SearchViewModel by viewModels {
        val input = requireArguments().let { SearchInput.from(it) }
        SearchViewModelFactory(input)
    }
    private val searchedAdapter: SearchedHearitAdapter by lazy { SearchedHearitAdapter(this) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSearchCategoryBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        setupWindowInsets()
        setupUI()
        setupListeners()
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

    private fun setupUI() {
        binding.tvSearchCategoryName.text = categoryName
        binding.root.background = createGradientBackground()
    }

    private fun createGradientBackground(): GradientDrawable {
        val startColor = categoryColor.toColorInt()
        val endColor = ContextCompat.getColor(requireContext(), R.color.hearit_black1)

        return GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(startColor, endColor),
        ).apply {
            gradientType = GradientDrawable.LINEAR_GRADIENT
            setColors(intArrayOf(startColor, endColor), floatArrayOf(0f, 0.2f))
        }
    }

    private fun setupListeners() {
        // 아이콘 뒤로가기
        binding.ibBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        // 휴대폰 뒤로가기
        requireActivity()
            .onBackPressedDispatcher
            .addCallback(viewLifecycleOwner) {
                parentFragmentManager.popBackStack()
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

    override fun onClick(hearitId: Long) {
        val intent = PlayerDetailActivity.newIntent(requireActivity(), hearitId)
        (activity as? MainActivity)?.launchDetailActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val REFRESH_THRESHOLD = 3

        fun newInstance(input: SearchInput): SearchCategoryFragment =
            SearchCategoryFragment().apply {
                arguments = input.toBundle()
            }
    }
}
