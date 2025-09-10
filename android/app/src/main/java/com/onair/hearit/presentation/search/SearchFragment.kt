package com.onair.hearit.presentation.search

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.onair.hearit.R
import com.onair.hearit.databinding.FragmentSearchBinding
import com.onair.hearit.domain.model.SearchInput
import com.onair.hearit.presentation.IntentKeys.CATEGORY_COLOR_KEY
import com.onair.hearit.presentation.IntentKeys.CATEGORY_ID_KEY
import com.onair.hearit.presentation.IntentKeys.CATEGORY_KEY
import com.onair.hearit.presentation.IntentKeys.CATEGORY_NAME_KEY
import com.onair.hearit.presentation.search.category.SearchCategoryFragment
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

        parentFragmentManager.setFragmentResultListener(
            CATEGORY_KEY,
            viewLifecycleOwner,
        ) { _, bundle ->
            val id = bundle.getLong(CATEGORY_ID_KEY)
            val name = bundle.getString(CATEGORY_NAME_KEY) ?: return@setFragmentResultListener
            val colorCode = bundle.getString(CATEGORY_COLOR_KEY) ?: "#000000"
            onCategoryClick(id, name, colorCode)
        }
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
            showToast(getString(resId))
        }
    }

    private fun navigateToRecent() {
        parentFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container_view, SearchRecentFragment())
            .addToBackStack(null)
            .commit()
    }

    private fun showToast(message: String?) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onCategoryClick(
        id: Long,
        name: String,
        colorCode: String,
    ) {
        parentFragmentManager
            .beginTransaction()
            .replace(
                R.id.fragment_container_view,
                SearchCategoryFragment.newInstance(
                    SearchInput.Category(
                        id,
                        name,
                        colorCode,
                    ),
                ),
            ).addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
