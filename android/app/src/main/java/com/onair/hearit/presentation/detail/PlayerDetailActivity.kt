package com.onair.hearit.presentation.detail

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.concurrent.futures.await
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.onair.hearit.R
import com.onair.hearit.analytics.AnalyticsEventNames
import com.onair.hearit.analytics.AnalyticsParamKeys
import com.onair.hearit.analytics.AnalyticsParamKeys.KEYWORD_NAME
import com.onair.hearit.databinding.ActivityPlayerDetailBinding
import com.onair.hearit.di.AnalyticsProvider
import com.onair.hearit.domain.model.Hearit
import com.onair.hearit.domain.model.SearchInput
import com.onair.hearit.presentation.IntentKeys.BOOKMARK_ID_KEY
import com.onair.hearit.presentation.IntentKeys.HEARIT_ID_KEY
import com.onair.hearit.presentation.IntentKeys.LAST_POSITION_KEY
import com.onair.hearit.presentation.IntentKeys.PREVIOUS_SCREEN_KEY
import com.onair.hearit.presentation.IntentKeys.TYPE_KEY
import com.onair.hearit.presentation.IntentValues.EXPLORE_VALUE
import com.onair.hearit.presentation.LoginRequiredDialogFragment
import com.onair.hearit.presentation.detail.script.ScriptFragment
import com.onair.hearit.presentation.dpToPx
import com.onair.hearit.presentation.login.LoginActivity
import com.onair.hearit.service.PlaybackService
import com.onair.hearit.service.PlaybackSessionCallback
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.abs

@OptIn(UnstableApi::class)
class PlayerDetailActivity :
    AppCompatActivity(),
    PlayerDetailClickListener {
    private lateinit var binding: ActivityPlayerDetailBinding

    private val keywordAdapter by lazy { PlayerDetailKeywordAdapter(this) }
    private val scriptAdapter by lazy { PlayerDetailScriptAdapter() }
    private val sourceAdapter by lazy { PlayerDetailSourceAdapter(this) }

    private var mediaController: MediaController? = null

    private val updateIntervalMs = 500L
    private val itemHeightPx by lazy { SCRIPT_ITEM_HEIGHT_DP.dpToPx(this) }

    private val previousScreen by lazy {
        intent.getStringExtra(PREVIOUS_SCREEN_KEY) ?: UNKNOWN_SCREEN_ID
    }
    private val hearitId: Long by lazy { intent.getLongExtra(HEARIT_ID_KEY, -1) }
    private val lastPosition: Long by lazy { intent.getLongExtra(LAST_POSITION_KEY, 0) }

    private val viewModel: PlayerDetailViewModel by viewModels {
        PlayerDetailViewModelFactory(hearitId)
    }

    private val playerListener =
        object : Player.Listener {
            override fun onMediaItemTransition(
                mediaItem: MediaItem?,
                reason: Int,
            ) {
                if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_SEEK ||
                    reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO
                ) {
                    mediaItem?.mediaId?.toLongOrNull()?.let { viewModel.refreshData(it) }
                }
            }
        }

    // (옵션) 남은 곡이 적으면 서비스에 프리패치 명령을 보내고 싶다면 사용
    private val prefetchListener =
        object : Player.Listener {
            override fun onMediaItemTransition(
                item: MediaItem?,
                reason: Int,
            ) {
                val controller = mediaController ?: return
                val remaining = controller.mediaItemCount - (controller.currentMediaItemIndex + 1)
                if (remaining <= 2) {
                    controller.sendCustomCommand(
                        PlaybackSessionCallback.PREFETCH_NEXT,
                        Bundle.EMPTY,
                    )
                }
            }
        }

    private var isPlaybackInitiated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_player_detail)
        binding.lifecycleOwner = this
        binding.clickListener = this
        binding.viewModel = viewModel

        setupBackPressHandler()
        setupWindowInsets()
        setupRecyclerView()
        observeViewModel()

        supportFragmentManager.addOnBackStackChangedListener {
            val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container_view)
            binding.fragmentContainerView.visibility =
                if (fragment?.isVisible == true) View.VISIBLE else View.GONE
        }
    }

    override fun onStart() {
        super.onStart()
        connectController()
    }

    private fun setupMediaController() {
        val sessionToken = SessionToken(this, ComponentName(this, PlaybackService::class.java))

        lifecycleScope.launch {
            val controller =
                MediaController
                    .Builder(this@PlayerDetailActivity, sessionToken)
                    .buildAsync()
                    .await()

            mediaController = controller
            binding.playerView.player = controller
            binding.baseController.setPlayer(controller)

            controller.addListener(playerListener)

            // 라이브러리 모드일 땐 프리패치 리스너 등록
            if (previousScreen == LIBRARY_SCREEN_ID) {
                controller.addListener(prefetchListener)
            }

            val playingId = controller.currentMediaItem?.mediaId?.toLongOrNull()
            val isDifferentHearit = playingId != hearitId

            if (isDifferentHearit) {
                controller.addListener(
                    object : Player.Listener {
                        override fun onTimelineChanged(
                            timeline: Timeline,
                            reason: Int,
                        ) {
                            if (timeline.windowCount > 0) {
                                controller.removeListener(this)
                                controller.play()
                            }
                        }
                    },
                )
            }
            startScriptSync(controller)
        }
    }

    private fun setupRecyclerView() {
        binding.rvScript.adapter = scriptAdapter
        setupScriptTapGesture()

        val layoutManager =
            FlexboxLayoutManager(this).apply {
                flexDirection = FlexDirection.ROW
                flexWrap = FlexWrap.WRAP
                justifyContent = JustifyContent.FLEX_START
            }
        binding.layoutDetailSummaryKeywords.rvKeyword.layoutManager = layoutManager
        binding.layoutDetailSummaryKeywords.rvKeyword.adapter = keywordAdapter
        binding.layoutDetailSource.rvDetailSource.adapter = sourceAdapter
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupGestureListener() {
        val gestureDetector =
            GestureDetector(
                this,
                object : GestureDetector.SimpleOnGestureListener() {
                    override fun onSingleTapUp(e: MotionEvent): Boolean {
                        supportFragmentManager
                            .beginTransaction()
                            .setCustomAnimations(R.anim.slide_up, 0)
                            .replace(
                                R.id.fragment_container_view,
                                ScriptFragment.newInstance(hearitId),
                            ).addToBackStack(null)
                            .commit()
                        return true
                    }
                },
            )
        binding.rvScript.setOnTouchListener { _, event -> detector.onTouchEvent(event) }
    }

    private fun observeViewModel() {
        viewModel.hearit.observe(this) { hearit ->
            binding.hearit = hearit
            keywordAdapter.submitList(hearit?.keywords)
            scriptAdapter.submitList(hearit?.script)
            sourceAdapter.submitList(hearit?.sources)

            // hearit 로드 뒤, 아직 재생 시작 안했으면 시도
            if (!isPlaybackInitiated && hearit != null) {
                isPlaybackInitiated = true
                handlePlayback(hearit)
            }
        }

        viewModel.bookmarkId.observe(this) { bookmarkId ->
            binding.baseController.setBookmarkSelected(bookmarkId != null)
        }

        viewModel.toastMessage.observe(this) { resId ->
            Toast.makeText(this, getString(resId), Toast.LENGTH_SHORT).show()
        }

        viewModel.showLoginDialog.observe(this) {
            showLoginRequiredDialog()
        }
    }

    private fun maybeStartPlayback(
        controller: Player,
        hearit: Hearit,
    ) {
        val currentId = controller.currentMediaItem?.mediaId?.toLongOrNull()
        val isDifferent = currentId != hearit.id
        val shouldResume = intent.hasExtra(LAST_POSITION_KEY) && lastPosition > 0L
        val startPosition = if (shouldResume) lastPosition else hearit.lastPlayTime ?: 0L
        val source = hearit.sources.firstOrNull()?.name ?: "hEARit"
        val startPosition = if (shouldResume) lastPosition else 0L

        if (isDifferent) {
            startPlaybackService(
                audioUrl = hearit.audioUrl,
                title = hearit.title,
                hearitId = hearit.id,
                startPosition = startPosition,
                source = source,
                bookmarkId = hearit.bookmarkId,
            )
        } else {
            if (!controller.isPlaying) controller.play()
            if (shouldResume && abs(controller.currentPosition - startPosition) > 1_000) {
                controller.seekTo(startPosition)
        if (previousScreen == LIBRARY_SCREEN_ID) {
            // 재생목록 모드: 서비스가 큐 세팅을 담당
            if (isDifferentHearit || controller.mediaItemCount == 0) {
                startLibraryPlayback(
                    seedHearitId = hearit.id,
                    seedBookmarkId = viewModel.bookmarkId.value,
                    startPosMs = startPosition,
                )
            } else {
                if (!controller.isPlaying) controller.play()
            }
        } else {
            // 단일 재생 모드
            if (isDifferentHearit) {
                playSingleWithController(hearit, startPosition, previousScreen)
            } else {
                if (!controller.isPlaying) controller.play()
                if (shouldResume && abs(controller.currentPosition - startPosition) > 1000) {
                    controller.seekTo(startPosition)
                }
            }
        }
    }

    private fun handlePlayback(hearit: Hearit) {
        val controller = mediaController ?: return
        maybeStartPlayback(controller, hearit)
    }

    @OptIn(UnstableApi::class)
    private fun startLibraryPlayback(
        seedHearitId: Long,
        seedBookmarkId: Long?,
        startPosMs: Long,
    ) {
        val controller = mediaController ?: return
        val args =
            Bundle().apply {
                putLong("SEED_HEARIT_ID", seedHearitId)
                putLong("SEED_BOOKMARK_ID", seedBookmarkId ?: -1)
                putLong("START_POSITION", startPosMs.coerceAtLeast(0L))
            }

        controller.sendCustomCommand(
            PlaybackSessionCallback.START_LIBRARY_PLAY,
            args,
        )
    }

    // 단일 재생 전용: 커맨드 전송 없이 setMediaItem만 수행
    private fun playSingleWithController(
        hearit: Hearit,
        startPosition: Long = 0L,
        previousScreen: String,
    ) {
        val controller = mediaController ?: return

        val extras =
            Bundle().apply {
                putLong(KEY_BOOKMARK_ID, hearit.bookmarkId ?: -1L)
                putString(KEY_PLAYBACK_MODE, previousScreen)
                putLong(KEY_START_POSITION, startPosition)
            }

        val item =
            MediaItem
                .Builder()
                .setMediaId(hearit.id.toString())
                .setUri(hearit.audioUrl.toUri())
                .setMediaMetadata(
                    MediaMetadata
                        .Builder()
                        .setTitle(hearit.title)
                        .setArtist(hearit.sources.firstOrNull()?.name ?: "hEARit")
                        .setExtras(extras)
                        .build(),
                ).build()

        controller.setMediaItem(item, startPosition)
        controller.prepare()
        controller.play()
    }

    private fun navigateToLogin() {
        AnalyticsProvider.get().logEvent(
            AnalyticsEventNames.LOGIN_EVENT,
            mapOf(AnalyticsParamKeys.SOURCE_NAME to "detail_login"),
        )
        startActivity(LoginActivity.newIntent(this))
        stopService(Intent(this, PlaybackService::class.java))
        finish()
    }

    private fun showToast(message: String?) {
        Toast.makeText(this, message ?: "", Toast.LENGTH_SHORT).show()
    }

    override fun onClickCategory(
        id: Long,
        name: String,
        colorCode: String,
    ) {
        AnalyticsProvider.get().logEvent(
            AnalyticsEventNames.DETAIL_CATEGORY_SELECTED,
            mapOf(AnalyticsParamKeys.CATEGORY_NAME to name),
        )
        val input = SearchInput.Category(id, name, colorCode)
        val resultIntent =
            Intent().apply {
                putExtras(input.toBundle())
            }
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    override fun onClickSource(
        name: String,
        url: String,
    ) {
        try {
            val uri = url.toUri()
            if (uri.scheme !in listOf("http", "https")) {
                Timber.w(ERROR_UNSUPPORTED_LINK_MESSAGE)
                showToast(ERROR_UNSUPPORTED_LINK_MESSAGE)
                return
            }

            AnalyticsProvider.get().logEvent(
                AnalyticsEventNames.DETAIL_SOURCE_SELECTED,
                mapOf(AnalyticsParamKeys.SOURCE_NAME to name),
            )

            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        } catch (e: Exception) {
            Timber.w(e)
            showToast(ERROR_INVALID_LINK_MESSAGE)
        }
    }

    override fun onClickKeyword(term: String) {
        AnalyticsProvider.get().logEvent(
            AnalyticsEventNames.DETAIL_KEYWORD_SELECTED,
            mapOf(KEYWORD_NAME to term),
        )
        val input = SearchInput.Keyword(term)
        val resultIntent =
            Intent().apply {
                putExtra(TYPE_KEY, KEYWORD_VALUE)
                putExtras(input.toBundle())
            }
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        disconnectController()
        mediaController?.removeListener(playerListener)
        if (previousScreen == LIBRARY_SCREEN_ID) {
            mediaController?.removeListener(prefetchListener)
        }
        scriptSyncJob?.cancel()
        mediaController?.release()
        mediaController = null
    }

    companion object {
        const val BOOKMARK_ID = "bookmarkId"
        const val LIBRARY_SCREEN_ID = "library"
        const val UNKNOWN_SCREEN_ID = "unknown"
        const val LOGIN_REQUIRED_DIALOG_TAG = "login_required_dialog"
        private const val ERROR_UNSUPPORTED_LINK_MESSAGE = "지원되지 않는 링크입니다"
        private const val ERROR_INVALID_LINK_MESSAGE = "잘못된 링크 형식입니다"
        private const val SCRIPT_ITEM_HEIGHT_DP = 16

        private const val KEY_BOOKMARK_ID = "BOOKMARK_ID"
        private const val KEY_PLAYBACK_MODE = "PLAYBACK_MODE"
        private const val KEY_START_POSITION = "START_POSITION"
        private const val EXTRA_LIMIT = "LIMIT"

        fun newIntent(
            context: Context,
            hearitId: Long,
            lastPosition: Long? = null,
            bookmarkId: Long? = null,
            source: String = UNKNOWN_SCREEN_ID,
        ): Intent =
            Intent(context, PlayerDetailActivity::class.java).apply {
                putExtra(HEARIT_ID_KEY, hearitId)
                lastPosition?.let { putExtra(LAST_POSITION_KEY, it) }
                bookmarkId?.let { putExtra(BOOKMARK_ID, it) }
                putExtra(AnalyticsParamKeys.SOURCE_NAME, source)
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
    }
}
