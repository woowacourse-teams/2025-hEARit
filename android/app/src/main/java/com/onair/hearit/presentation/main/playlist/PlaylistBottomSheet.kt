package com.onair.hearit.presentation.main.playlist

import android.content.ComponentName
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.onair.hearit.databinding.BottomSheetPlaylistBinding
import com.onair.hearit.domain.model.Bookmark
import com.onair.hearit.presentation.IntentKeys.PREVIOUS_SCREEN_KEY
import com.onair.hearit.presentation.PlaybackStarter
import com.onair.hearit.presentation.detail.PlayerDetailActivity
import com.onair.hearit.presentation.main.MainActivity
import com.onair.hearit.service.PlaybackService
import com.onair.hearit.service.PlaybackSessionCallback
import com.onair.hearit.service.model.LibraryPlayParams.Companion.EXTRA_SEED_BOOKMARK_ID
import com.onair.hearit.service.model.LibraryPlayParams.Companion.EXTRA_SEED_HEARIT_ID
import com.onair.hearit.service.model.LibraryPlayParams.Companion.EXTRA_START_POSITION_MS
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlaylistBottomSheet :
    BottomSheetDialogFragment(),
    PlaylistClickListener {
    @Suppress("ktlint:standard:backing-property-naming")
    private var _binding: BottomSheetPlaylistBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlaylistViewModel by viewModels()
    private val playlistAdapter: PlaylistAdapter by lazy { PlaylistAdapter(this) }

    private var mediaController: MediaController? = null

    private val playerListener =
        object : Player.Listener {
            override fun onMediaItemTransition(
                mediaItem: MediaItem?,
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

                if (events.contains(Player.EVENT_IS_PLAYING_CHANGED) ||
                    events.contains(Player.EVENT_PLAYBACK_STATE_CHANGED)
                ) {
                    playlistAdapter.updateIsPlaying(player.isPlaying)
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
        setupInfiniteScroll()
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

    private fun observeViewModel() {
        viewModel.bookmarks.observe(viewLifecycleOwner) { bookmarks ->
            playlistAdapter.submitList(bookmarks)
        }
    }

    private fun publishFromMetadata(metadata: MediaMetadata?) {
        val id = metadata?.extras?.getLong(EXTRA_BOOKMARK_ID, -1L)?.takeIf { it > 0 }
        val mode = metadata?.extras?.getString(MODE_KEY)
        playlistAdapter.updatePlaying(id, mode)
    }

    override fun onStop() {
        super.onStop()
        disconnectController()
    }

    private fun setupInfiniteScroll() {
        val layoutManager = binding.rvPlaylist.layoutManager as? LinearLayoutManager ?: return
        binding.rvPlaylist.addOnScrollListener(
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
                // 연결 직후에도 현재 아이템 + 재생 여부 모두 반영
                publishFromMetadata(controller.currentMediaItem?.mediaMetadata)
                playlistAdapter.updateIsPlaying(controller.isPlaying)
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

    override fun onClickHearit(hearitId: Long) {
        val intent =
            PlayerDetailActivity.newIntent(requireActivity(), hearitId).apply {
                putExtra(PREVIOUS_SCREEN_KEY, PlayerDetailActivity.LIBRARY_SCREEN_ID)
            }
        (activity as? MainActivity)?.launchDetailActivity(intent)
    }

    @OptIn(UnstableApi::class)
    override fun onClickPlayToggle(item: Bookmark) {
        val controller = mediaController
        if (controller == null) {
            (activity as? PlaybackStarter)?.startPlayback()
            return
        }

        val current = controller.currentMediaItem
        val currentMeta: MediaMetadata? = current?.mediaMetadata
        val currentBookmarkId =
            currentMeta?.extras?.getLong(EXTRA_BOOKMARK_ID, -1L)?.takeIf { it > 0 }
        val currentMode = currentMeta?.extras?.getString(MODE_KEY)

        val isSameLibraryItem =
            (currentBookmarkId == item.bookmarkId) &&
                (currentMode.equals("LIBRARY"))

        if (isSameLibraryItem) {
            when (controller.playbackState) {
                Player.STATE_ENDED -> {
                    controller.seekToDefaultPosition()
                    controller.play()
                }

                Player.STATE_IDLE -> {
                    controller.prepare()
                    controller.play()
                }

                Player.STATE_BUFFERING, Player.STATE_READY -> {
                    if (controller.isPlaying) controller.pause() else controller.play()
                }
            }
            return
        }

        // 다른 아이템이면: 서비스에 라이브러리 재생 시작 커맨드 전송
        val args =
            Bundle().apply {
                putLong(EXTRA_SEED_HEARIT_ID, item.hearitId)
                putLong(EXTRA_SEED_BOOKMARK_ID, item.bookmarkId)
                putLong(EXTRA_START_POSITION_MS, item.lastPlayTime ?: 0L)
            }
        controller.sendCustomCommand(
            PlaybackSessionCallback.START_LIBRARY_PLAY_COMMAND,
            args,
        )
    }

    companion object {
        private const val INITIAL_PEEK_RATIO = 0.5
        private const val LOAD_MORE_THRESHOLD = 3
        private const val EXTRA_BOOKMARK_ID = "BOOKMARK_ID"
        private const val MODE_KEY = "PLAYBACK_MODE"

        fun newInstance(): PlaylistBottomSheet = PlaylistBottomSheet()
    }
}
