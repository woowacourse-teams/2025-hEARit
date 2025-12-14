package com.onair.hearit.presentation.library

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.concurrent.futures.await
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.analytics.FirebaseAnalytics
import com.onair.hearit.R
import com.onair.hearit.analytics.AnalyticsEventNames
import com.onair.hearit.analytics.AnalyticsLogger
import com.onair.hearit.analytics.AnalyticsParamKeys
import com.onair.hearit.analytics.AnalyticsParamKeys.SCREEN_NAME_LIBRARY
import com.onair.hearit.databinding.FragmentLibraryBinding
import com.onair.hearit.presentation.IntentKeys.PREVIOUS_SCREEN_KEY
import com.onair.hearit.presentation.detail.PlayerDetailActivity
import com.onair.hearit.presentation.login.LoginActivity
import com.onair.hearit.presentation.main.MainActivity
import com.onair.hearit.presentation.main.MainViewModel
import com.onair.hearit.presentation.setting.SettingFragment
import com.onair.hearit.service.PlaybackService
import com.onair.hearit.service.PlaybackSessionCallback
import com.onair.hearit.service.model.LibraryPlayParams.Companion.EXTRA_SEED_BOOKMARK_ID
import com.onair.hearit.service.model.LibraryPlayParams.Companion.EXTRA_SEED_HEARIT_ID
import com.onair.hearit.service.model.LibraryPlayParams.Companion.EXTRA_START_POSITION_MS
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LibraryFragment :
    Fragment(),
    BookmarkClickListener {
    @Suppress("ktlint:standard:backing-property-naming")
    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!

    private val mainViewModel: MainViewModel by activityViewModels()
    private val viewModel: LibraryViewModel by viewModels()
    private val bookmarkAdapter: BookmarkAdapter by lazy { BookmarkAdapter(this) }

    @Inject
    lateinit var analyticsLogger: AnalyticsLogger

    private var mediaController: MediaController? = null

    private val playerListener =
        object : Player.Listener {
            override fun onEvents(
                player: Player,
                events: Player.Events,
            ) {
                if (events.containsAny(
                        Player.EVENT_MEDIA_ITEM_TRANSITION,
                        Player.EVENT_MEDIA_METADATA_CHANGED,
                        Player.EVENT_PLAY_WHEN_READY_CHANGED,
                        Player.EVENT_PLAYBACK_STATE_CHANGED,
                    )
                ) {
                    updatePlayAllIcon(mediaController)
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentLibraryBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.userInfo = viewModel.userInfo.value
        binding.rvBookmark.adapter = bookmarkAdapter
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        setupWindowInsets()
        setupListeners()
        setupInfiniteScroll()
        setupPlayAllButton()
        observeViewModel()
    }

    override fun onStart() {
        super.onStart()
        if (mediaController == null) {
            val token =
                SessionToken(
                    requireContext(),
                    ComponentName(
                        requireContext(),
                        PlaybackService::class.java,
                    ),
                )
            viewLifecycleOwner.lifecycleScope.launch {
                mediaController =
                    MediaController.Builder(requireContext(), token).buildAsync().await()
                mediaController?.addListener(playerListener)
                updatePlayAllIcon(mediaController)
            }
        } else {
            updatePlayAllIcon(mediaController)
        }
    }

    override fun onResume() {
        super.onResume()
        analyticsLogger.logEvent(
            FirebaseAnalytics.Event.SCREEN_VIEW,
            mapOf(
                FirebaseAnalytics.Param.SCREEN_NAME to SCREEN_NAME_LIBRARY,
                FirebaseAnalytics.Param.SCREEN_CLASS to this::class.simpleName.orEmpty(),
            ),
        )
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        binding.layoutLibraryWhenNoLogin.btnLibraryLogin.setOnClickListener {
            analyticsLogger.logEvent(
                AnalyticsEventNames.LOGIN_EVENT,
                mapOf(AnalyticsParamKeys.SOURCE_NAME to "library_login"),
            )

            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
    }

    private fun setupListeners() {
        binding.ibSetting.setOnClickListener {
            parentFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container_view, SettingFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun observeViewModel() {
        mainViewModel.hearitUpdated.observe(viewLifecycleOwner) {
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

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.userInfo.collect { userInfo ->
                    binding.userInfo = userInfo
                }
            }
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

    private fun setupPlayAllButton() {
        binding.ibPlayAll.setOnClickListener {
            val controller = mediaController
            if (controller == null) {
                startPlaylistFromTopBookmark()
                return@setOnClickListener
            }

            if (isLibraryMode(controller)) {
                if (controller.isPlaying) controller.pause() else controller.play()
            } else {
                startPlaylistFromTopBookmark()
            }
        }
    }

    @OptIn(UnstableApi::class)
    private fun startPlaylistFromTopBookmark() {
        val latestBookmark = viewModel.bookmarks.value?.firstOrNull() ?: return

        val args =
            Bundle().apply {
                putLong(EXTRA_SEED_HEARIT_ID, latestBookmark.hearitId)
                putLong(EXTRA_SEED_BOOKMARK_ID, latestBookmark.bookmarkId)
                putLong(EXTRA_START_POSITION_MS, latestBookmark.lastPlayTime ?: 0L)
            }

        mediaController?.sendCustomCommand(
            PlaybackSessionCallback.START_LIBRARY_PLAY_COMMAND,
            args,
        )
    }

    private fun updatePlayAllIcon(controller: MediaController?) {
        if (controller == null) {
            binding.ibPlayAll.setImageResource(R.drawable.img_play)
            return
        }

        if (isLibraryMode(controller)) {
            val isPlaying =
                controller.playWhenReady && controller.playbackState == Player.STATE_READY
            binding.ibPlayAll.setImageResource(if (isPlaying) R.drawable.img_pause else R.drawable.img_play)
        } else {
            binding.ibPlayAll.setImageResource(R.drawable.img_play)
        }
    }

    private fun isLibraryMode(controller: MediaController?): Boolean {
        val mode =
            controller
                ?.currentMediaItem
                ?.mediaMetadata
                ?.extras
                ?.getString(MODE_KEY)
        return mode.equals(LIBRARY_MODE, ignoreCase = true)
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
        (activity as? MainActivity)?.launchDetailActivity(intent)

        analyticsLogger.logEvent(
            AnalyticsEventNames.LIBRARY_TO_DETAIL,
            mapOf(AnalyticsParamKeys.ITEM_ID to hearitId.toString()),
        )
    }

    override fun onStop() {
        super.onStop()
        mediaController?.removeListener(playerListener)
        mediaController?.release()
        mediaController = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val LOAD_MORE_THRESHOLD = 3
        private const val MODE_KEY = "PLAYBACK_MODE"
        private const val LIBRARY_MODE = "LIBRARY"
    }
}
