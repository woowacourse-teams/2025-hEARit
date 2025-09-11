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
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.analytics.AnalyticsEventNames
import com.onair.hearit.analytics.AnalyticsParamKeys
import com.onair.hearit.databinding.FragmentLibraryBinding
import com.onair.hearit.di.AnalyticsProvider
import com.onair.hearit.presentation.IntentKeys.PREVIOUS_SCREEN_KEY
import com.onair.hearit.presentation.detail.PlayerDetailActivity
import com.onair.hearit.presentation.login.LoginActivity
import com.onair.hearit.presentation.main.MainActivity
import com.onair.hearit.presentation.main.MainViewModel
import com.onair.hearit.presentation.navigate
import com.onair.hearit.presentation.toDetailResult

class LibraryFragment :
    Fragment(),
    BookmarkClickListener {
    @Suppress("ktlint:standard:backing-property-naming")
    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!

    private val mainViewModel: MainViewModel by activityViewModels()
    private val viewModel: LibraryViewModel by viewModels { LibraryViewModelFactory() }
    private val bookmarkAdapter: BookmarkAdapter by lazy { BookmarkAdapter(this) }

    private val playerDetailLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            viewModel.refreshBookmarks()
            if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult
            val detailResult = result.data.toDetailResult() ?: return@registerForActivityResult
            (requireActivity() as MainActivity).apply { detailResult.navigate(this) }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentLibraryBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.rvBookmark.adapter = bookmarkAdapter
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

        binding.layoutLibraryWhenNoLogin.btnLibraryLogin.setOnClickListener {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        binding.layoutLibraryWhenNoLogin.btnLibraryLogin.setOnClickListener {
            AnalyticsProvider.get().logEvent(
                AnalyticsEventNames.LOGIN_EVENT,
                mapOf(AnalyticsParamKeys.SOURCE_NAME to "library_login"),
            )

            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
    }

    private fun observeViewModel() {
        mainViewModel.bookmarkUpdated.observe(viewLifecycleOwner) {
            viewModel.refreshBookmarks()
        }

        viewModel.bookmarks.observe(viewLifecycleOwner) { bookmarks ->
            bookmarkAdapter.submitList(bookmarks)
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

                    if (lastVisibleItem >= totalItemCount - LOAD_MORE_THRESHOLD && viewModel.isLoading.value != true) {
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
        val intent =
            PlayerDetailActivity.newIntent(requireActivity(), hearitId).apply {
                putExtra(AnalyticsParamKeys.SOURCE_NAME, PlayerDetailActivity.LIBRARY_SCREEN_ID)
                putExtra(PREVIOUS_SCREEN_KEY, PlayerDetailActivity.LIBRARY_SCREEN_ID)
            }
        playerDetailLauncher.launch(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val LOAD_MORE_THRESHOLD = 3
    }
}
