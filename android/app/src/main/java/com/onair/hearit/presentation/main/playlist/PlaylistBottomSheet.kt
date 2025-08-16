package com.onair.hearit.presentation.main.playlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.onair.hearit.databinding.BottomSheetPlaylistBinding

class PlaylistBottomSheet : BottomSheetDialogFragment() {
    @Suppress("ktlint:standard:backing-property-naming")
    private var _binding: BottomSheetPlaylistBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlaylistViewModel by viewModels { PlaylistViewModelFactory() }
    private val playlistAdapter: PlaylistAdapter by lazy { PlaylistAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = BottomSheetPlaylistBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.rvPlaylist.adapter = playlistAdapter
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.bookmarks.observe(viewLifecycleOwner) { bookmarks ->
            playlistAdapter.submitList(bookmarks)
        }
    }

    override fun onStart() {
        super.onStart()

        val bottomSheetDialog = dialog as? BottomSheetDialog ?: return
        val behavior = bottomSheetDialog.behavior

        (requireView().parent as View).layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT

        behavior.peekHeight = (resources.displayMetrics.heightPixels * INITIAL_PEEK_RATIO).toInt()
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED

        behavior.isFitToContents = false
        behavior.skipCollapsed = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val INITIAL_PEEK_RATIO = 0.5

        fun newInstance(): PlaylistBottomSheet = PlaylistBottomSheet()
    }
}
