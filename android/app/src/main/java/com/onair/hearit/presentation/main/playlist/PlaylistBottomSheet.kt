package com.onair.hearit.presentation.main.playlist

import android.content.ComponentName
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.onair.hearit.databinding.BottomSheetPlaylistBinding
import com.onair.hearit.service.PlaybackService

class PlaylistBottomSheet : BottomSheetDialogFragment() {
    @Suppress("ktlint:standard:backing-property-naming")
    private var _binding: BottomSheetPlaylistBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlaylistViewModel by viewModels { PlaylistViewModelFactory() }
    private val playlistAdapter: PlaylistAdapter by lazy { PlaylistAdapter() }

    private var mediaController: MediaController? = null

    private val playerListener =
        object : Player.Listener {
            override fun onMediaItemTransition(
                mediaItem: androidx.media3.common.MediaItem?,
                reason: Int,
            ) {
                publishFromMetadata(mediaItem?.mediaMetadata)
            }

            override fun onEvents(
                player: Player,
                events: Player.Events,
            ) {
                if (events.contains(Player.EVENT_MEDIA_ITEM_TRANSITION) ||
                    events.contains(Player.EVENT_MEDIA_METADATA_CHANGED)
                ) {
                    publishFromMetadata(player.currentMediaItem?.mediaMetadata)
                }
            }
        }

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

    private fun publishFromMetadata(metadata: MediaMetadata?) {
        val id = metadata?.extras?.getLong(EXTRA_BOOKMARK_ID, -1L)?.takeIf { it > 0 }
        playlistAdapter.updatePlaying(id)
    }

    override fun onStart() {
        super.onStart()

        connectController()

        val bottomSheetDialog = dialog as? BottomSheetDialog ?: return
        val behavior = bottomSheetDialog.behavior

        (requireView().parent as View).layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT

        behavior.peekHeight = (resources.displayMetrics.heightPixels * INITIAL_PEEK_RATIO).toInt()
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED

        behavior.isFitToContents = false
        behavior.skipCollapsed = false
    }

    override fun onStop() {
        super.onStop()
        disconnectController()
    }

    private fun connectController() {
        if (mediaController != null) return
        val token =
            SessionToken(
                requireContext(),
                ComponentName(requireContext(), PlaybackService::class.java),
            )
        val future = MediaController.Builder(requireContext(), token).buildAsync()
        future.addListener(
            {
                val controller = future.get()
                mediaController = controller
                controller.addListener(playerListener)
                // 연결 직후에도 한 번 현재 상태 반영
                publishFromMetadata(controller.currentMediaItem?.mediaMetadata)
            },
            ContextCompat.getMainExecutor(requireContext()),
        )
    }

    private fun disconnectController() {
        mediaController?.removeListener(playerListener)
        mediaController?.release()
        mediaController = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val INITIAL_PEEK_RATIO = 0.5
        private const val EXTRA_BOOKMARK_ID = "BOOKMARK_ID"

        fun newInstance(): PlaylistBottomSheet = PlaylistBottomSheet()
    }
}
