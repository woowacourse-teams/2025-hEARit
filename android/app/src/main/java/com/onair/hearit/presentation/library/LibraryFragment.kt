package com.onair.hearit.presentation.library

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.analytics.AnalyticsScreenInfo
import com.onair.hearit.databinding.FragmentLibraryBinding
import com.onair.hearit.di.AnalyticsProvider
import com.onair.hearit.presentation.detail.PlayerDetailActivity
import com.onair.hearit.presentation.login.LoginActivity

class LibraryFragment :
    Fragment(),
    BookmarkClickListener {
    @Suppress("ktlint:standard:backing-property-naming")
    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LibraryViewModel by viewModels { LibraryViewModelFactory() }
    private val adapter: BookmarkAdapter by lazy { BookmarkAdapter(this) }

    private val playerDetailLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                viewModel.refreshBookmarks()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentLibraryBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.rvBookmark.adapter = adapter
        binding.rvBookmark.layoutManager = LinearLayoutManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        setupWindowInsets()
        observeViewModel()
        setupInfiniteScroll()
    }

    override fun onResume() {
        super.onResume()
        AnalyticsProvider.get().logScreenView(
            screenName = AnalyticsScreenInfo.Library.NAME,
            screenClass = AnalyticsScreenInfo.Library.CLASS,
        )
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        binding.layoutLibraryWhenNoLogin.btnLibraryLogin.setOnClickListener {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
    }

    private fun observeViewModel() {
        viewModel.bookmarks.observe(viewLifecycleOwner) { bookmarks ->
            adapter.submitList(bookmarks)
        }

        viewModel.toastMessage.observe(viewLifecycleOwner) { resId ->
            Toast.makeText(requireContext(), getString(resId), Toast.LENGTH_SHORT).show()
        }

        viewModel.uiState.observe(viewLifecycleOwner) { uiState ->
            binding.uiState = uiState
        }

        viewModel.userInfo.observe(viewLifecycleOwner) { userInfo ->
            binding.userInfo = userInfo
        }
    }

    private fun setupInfiniteScroll() {
        val layoutManager = binding.rvBookmark.layoutManager as? LinearLayoutManager ?: return
        binding.rvBookmark.addOnScrollListener(
            object : RecyclerView.OnScrollListener() {
                override fun onScrolled(
                    recyclerView: RecyclerView,
                    dx: Int,
                    dy: Int,
                ) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy <= 0) return

                    val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                    val totalItemCount = layoutManager.itemCount

                    if (lastVisibleItem >= totalItemCount - 3 && viewModel.isLoading.value != true) {
                        viewModel.loadNextPage()
                    }
                }
            },
        )
    }

    override fun onClickOption(bookmarkId: Long) {
        val sheet = BookmarkOptionBottomSheet.newInstance(bookmarkId)
        sheet.show(childFragmentManager, sheet.tag)
    }

    override fun onClickBookmarkedHearit(hearitId: Long) {
        val intent = PlayerDetailActivity.newIntent(requireActivity(), hearitId)
        playerDetailLauncher.launch(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
