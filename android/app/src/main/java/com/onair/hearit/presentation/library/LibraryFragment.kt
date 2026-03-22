package com.onair.hearit.presentation.library

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.concurrent.futures.await
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.firebase.analytics.FirebaseAnalytics
import com.onair.hearit.R
import com.onair.hearit.analytics.AnalyticsEventNames
import com.onair.hearit.analytics.AnalyticsLogger
import com.onair.hearit.analytics.AnalyticsParamKeys
import com.onair.hearit.analytics.AnalyticsParamKeys.SCREEN_NAME_LIBRARY
import com.onair.hearit.presentation.IntentKeys.PREVIOUS_SCREEN_KEY
import com.onair.hearit.presentation.detail.PlayerDetailActivity
import com.onair.hearit.presentation.library.component.LibraryScreen
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
class LibraryFragment : Fragment() {
    private val mainViewModel: MainViewModel by activityViewModels()
    private val viewModel: LibraryViewModel by viewModels()

    @Inject
    lateinit var analyticsLogger: AnalyticsLogger

    private var mediaController: MediaController? = null
    private var isPlayingState by mutableStateOf(false)

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
    ): View =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme {
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                    val userInfo by viewModel.userInfo.collectAsStateWithLifecycle()
                    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
                    val totalCount by viewModel.totalCount.collectAsStateWithLifecycle()
                    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

                    LibraryScreen(
                        uiState = uiState,
                        userInfo = userInfo,
                        bookmarks = bookmarks,
                        totalCount = totalCount,
                        isPlaying = isPlayingState,
                        isLoading = isLoading,
                        onSettingClick = { navigateToSetting() },
                        onLoginClick = { navigateToLogin() },
                        onPlayAllClick = { handlePlayAll() },
                        onItemClick = { hearitId -> navigateToDetail(hearitId) },
                        onOptionClick = { bookmarkId -> showBookmarkOptions(bookmarkId) },
                        onLoadMore = { viewModel.loadNextPage() },
                    )
                }
            }
        }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
    }

    private fun observeViewModel() {
        mainViewModel.hearitUpdated.observe(viewLifecycleOwner) {
            viewModel.refreshBookmarks()
        }

        viewModel.toastMessage.observe(viewLifecycleOwner) { resId ->
            Toast.makeText(requireContext(), getString(resId), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStart() {
        super.onStart()
        if (mediaController == null) {
            val token =
                SessionToken(
                    requireContext(),
                    ComponentName(requireContext(), PlaybackService::class.java),
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

    private fun navigateToSetting() {
        parentFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container_view, SettingFragment())
            .addToBackStack(null)
            .commit()
    }

    private fun navigateToLogin() {
        analyticsLogger.logEvent(
            AnalyticsEventNames.LOGIN_EVENT,
            mapOf(AnalyticsParamKeys.SOURCE_NAME to "library_login"),
        )
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    private fun navigateToDetail(hearitId: Long) {
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

    private fun showBookmarkOptions(bookmarkId: Long) {
        val sheet = BookmarkOptionBottomSheet.newInstance(bookmarkId)
        sheet.show(childFragmentManager, sheet.tag)
    }

    private fun handlePlayAll() {
        val controller = mediaController
        if (controller == null) {
            startPlaylistFromTopBookmark()
            return
        }

        if (isLibraryMode(controller)) {
            if (controller.isPlaying) controller.pause() else controller.play()
        } else {
            startPlaylistFromTopBookmark()
        }
    }

    @OptIn(UnstableApi::class)
    private fun startPlaylistFromTopBookmark() {
        val latestBookmark = viewModel.bookmarks.value.firstOrNull() ?: return

        val args =
            Bundle().apply {
                putLong(EXTRA_SEED_HEARIT_ID, latestBookmark.hearitId)
                putLong(EXTRA_SEED_BOOKMARK_ID, latestBookmark.bookmarkId)
                putLong(EXTRA_START_POSITION_MS, latestBookmark.lastPlayTime ?: 0L)
            }

        mediaController?.sendCustomCommand(PlaybackSessionCallback.START_LIBRARY_PLAY_COMMAND, args)
    }

    private fun updatePlayAllIcon(controller: MediaController?) {
        if (controller == null) {
            isPlayingState = false
            return
        }

        isPlayingState =
            if (isLibraryMode(controller)) {
                controller.playWhenReady && controller.playbackState == Player.STATE_READY
            } else {
                false
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

    override fun onStop() {
        super.onStop()
        mediaController?.removeListener(playerListener)
        mediaController?.release()
        mediaController = null
    }

    companion object {
        private const val MODE_KEY = "PLAYBACK_MODE"
        private const val LIBRARY_MODE = "LIBRARY"
    }
}
