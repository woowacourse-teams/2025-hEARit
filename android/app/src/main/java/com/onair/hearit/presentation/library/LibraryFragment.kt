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
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.onair.hearit.databinding.FragmentLibraryBinding
import com.onair.hearit.presentation.detail.PlayerDetailActivity
import com.onair.hearit.presentation.login.LoginActivity

class LibraryFragment :
    Fragment(),
    BookmarkClickListener {
    @Suppress("ktlint:standard:backing-property-naming")
    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LibraryViewModel by viewModels { LibraryViewModelFactory() }
    private val adapter by lazy { BookmarkAdapter(this) }

    private val playerDetailLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                viewModel.fetchData(page = 0)
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
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        setupWindowInsets()
        observeViewModel()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        observeViewModel()

        binding.layoutLibraryWhenNoLogin.btnLibraryLogin.setOnClickListener {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
    }

    private fun observeViewModel() {
        viewModel.bookmarks.observe(viewLifecycleOwner) { bookmarks ->
            adapter.submitList(bookmarks)

            binding.layoutLibraryWhenNoBookmark.isVisible = bookmarks.isEmpty()
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

    override fun onClickOption() {
        val sheet = BookmarkOptionBottomSheet()
        sheet.show(parentFragmentManager, sheet.tag)
    }

    override fun onClickBookmarkedHearit(hearitId: Long) {
        val intent = PlayerDetailActivity.newIntent(requireActivity(), hearitId)
        playerDetailLauncher.launch(intent)
    }
}
