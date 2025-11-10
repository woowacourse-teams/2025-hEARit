package com.onair.hearit.presentation.search

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.firebase.analytics.FirebaseAnalytics
import com.onair.hearit.R
import com.onair.hearit.analytics.AnalyticsEventNames
import com.onair.hearit.analytics.AnalyticsParamKeys
import com.onair.hearit.analytics.AnalyticsParamKeys.SCREEN_NAME_SEARCH
import com.onair.hearit.databinding.FragmentSearchBinding
import com.onair.hearit.di.AnalyticsProvider
import com.onair.hearit.presentation.IntentKeys.CATEGORY_COLOR_KEY
import com.onair.hearit.presentation.IntentKeys.CATEGORY_ID_KEY
import com.onair.hearit.presentation.IntentKeys.CATEGORY_NAME_KEY
import com.onair.hearit.presentation.search.category.CategoryComposeFragment
import com.onair.hearit.presentation.search.recent.SearchRecentFragment

class SearchFragment :
    Fragment(),
    CategoryClickListener {
    @Suppress("ktlint:standard:backing-property-naming")
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SearchViewModel by viewModels { SearchViewModelFactory(null) }
    private val categoryAdapter: CategoryAdapter by lazy { CategoryAdapter(this) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        setupWindowInsets()
        setupCategoryRecyclerView()
        setupSearchInput()
        observeViewModel()
        viewModel.getCategories()
    }

    override fun onResume() {
        super.onResume()
        AnalyticsProvider.get().logEvent(
            FirebaseAnalytics.Event.SCREEN_VIEW,
            mapOf(
                FirebaseAnalytics.Param.SCREEN_NAME to SCREEN_NAME_SEARCH,
                FirebaseAnalytics.Param.SCREEN_CLASS to this::class.simpleName.orEmpty(),
            ),
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupSearchInput() {
        binding.btnSearch.setOnClickListener { navigateToRecent() }

        binding.etSearch.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                navigateToRecent()
            }
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

    private fun setupCategoryRecyclerView() {
        binding.rvSearchCategories.adapter = categoryAdapter
    }

    private fun observeViewModel() {
        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            categoryAdapter.submitList(categories)
        }

        viewModel.toastMessage.observe(viewLifecycleOwner) { resId ->
            showToast(resId)
        }
    }

    private fun navigateToRecent() {
        parentFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container_view, SearchRecentFragment())
            .addToBackStack(null)
            .commit()
    }

    private fun showToast(
        @StringRes resId: Int?,
    ) {
        resId?.let {
            Toast.makeText(requireContext(), getString(it), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCategoryClick(
        id: Long,
        name: String,
        colorCode: String,
    ) {
        AnalyticsProvider.get().logEvent(
            AnalyticsEventNames.SEARCH_CATEGORY_SELECTED,
            mapOf(AnalyticsParamKeys.CATEGORY_NAME to name),
        )

        val fragment =
            CategoryComposeFragment().apply {
                arguments =
                    bundleOf(
                        CATEGORY_ID_KEY to id,
                        CATEGORY_NAME_KEY to name,
                        CATEGORY_COLOR_KEY to colorCode,
                    )
            }

        parentFragmentManager.beginTransaction().apply {
            val currentFragment =
                parentFragmentManager.findFragmentById(R.id.fragment_container_view)
            if (currentFragment != null) hide(currentFragment)

            add(R.id.fragment_container_view, fragment)
            addToBackStack(null)
            commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
