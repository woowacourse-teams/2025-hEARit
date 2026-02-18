package com.onair.hearit.presentation.detail

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.concurrent.futures.await
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.firebase.analytics.FirebaseAnalytics
import com.kakao.sdk.common.util.KakaoCustomTabsClient
import com.kakao.sdk.share.ShareClient
import com.kakao.sdk.share.WebSharerClient
import com.onair.hearit.R
import com.onair.hearit.analytics.AnalyticsEventNames
import com.onair.hearit.analytics.AnalyticsLogger
import com.onair.hearit.analytics.AnalyticsParamKeys
import com.onair.hearit.analytics.AnalyticsParamKeys.SCREEN_NAME_DETAIL
import com.onair.hearit.databinding.ActivityPlayerDetailBinding
import com.onair.hearit.domain.model.Hearit
import com.onair.hearit.domain.model.LoginReason
import com.onair.hearit.presentation.IntentKeys.BOOKMARK_ID_KEY
import com.onair.hearit.presentation.IntentKeys.HEARIT_ID_KEY
import com.onair.hearit.presentation.IntentKeys.LAST_POSITION_KEY
import com.onair.hearit.presentation.IntentKeys.PREVIOUS_SCREEN_KEY
import com.onair.hearit.presentation.IntentKeys.TYPE_KEY
import com.onair.hearit.presentation.IntentValues.EXPLORE_VALUE
import com.onair.hearit.presentation.LoginRequiredDialogFragment
import com.onair.hearit.presentation.detail.adapter.PlayerDetailKeywordAdapter
import com.onair.hearit.presentation.detail.adapter.PlayerDetailSourceAdapter
import com.onair.hearit.presentation.detail.component.DetailScripts
import com.onair.hearit.presentation.detail.script.ScriptFragment
import com.onair.hearit.presentation.login.LoginActivity
import com.onair.hearit.service.PlaybackService
import com.onair.hearit.service.PlaybackSessionCallback
import com.onair.hearit.service.model.LibraryPlayParams.Companion.EXTRA_SEED_BOOKMARK_ID
import com.onair.hearit.service.model.LibraryPlayParams.Companion.EXTRA_SEED_HEARIT_ID
import com.onair.hearit.service.model.LibraryPlayParams.Companion.EXTRA_START_POSITION_MS
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.abs

@AndroidEntryPoint
@OptIn(UnstableApi::class)
class PlayerDetailActivity :
    AppCompatActivity(),
    PlayerDetailClickListener {
    private lateinit var binding: ActivityPlayerDetailBinding

    private val keywordAdapter by lazy { PlayerDetailKeywordAdapter() }
    private val sourceAdapter by lazy { PlayerDetailSourceAdapter(this) }

    @Inject
    lateinit var analyticsLogger: AnalyticsLogger

    private var mediaController: MediaController? = null

    private val updateIntervalMs = 500L

    private val previousScreen by lazy {
        intent.getStringExtra(PREVIOUS_SCREEN_KEY) ?: UNKNOWN_SCREEN_ID
    }
    private val hearitId: Long by lazy { intent.getLongExtra(HEARIT_ID_KEY, -1) }
    private val lastPosition: Long by lazy { intent.getLongExtra(LAST_POSITION_KEY, 0) }

    /** 딥링크 or 앱 내부 등 현재 사용하고자 하는 id*/
    private val currentHearitId: Long
        get() = viewModel.hearit.value?.id ?: hearitId

    private val viewModel: PlayerDetailViewModel by viewModels()
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

    private var isPlaybackInitiated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_player_detail)
        binding.lifecycleOwner = this
        binding.clickListener = this
        binding.viewModel = viewModel

        setupBaseControllerBookmark()
        setupLikeButton()
        setupBackPressHandler()
        setupWindowInsets()
        setupRecyclerView()
        setupDetailScript()
        observeViewModel()
        startScriptSyncLoop()
        handleIncomingIntent(intent)

        binding.btnDetailShare.setOnClickListener { startKakaoInvite(this@PlayerDetailActivity) }
    }

    override fun onStart() {
        super.onStart()
        connectController()
    }

    override fun onResume() {
        super.onResume()
        analyticsLogger.logEvent(
            FirebaseAnalytics.Event.SCREEN_VIEW,
            mapOf(
                FirebaseAnalytics.Param.SCREEN_NAME to SCREEN_NAME_DETAIL,
                FirebaseAnalytics.Param.SCREEN_CLASS to this::class.simpleName.orEmpty(),
            ),
        )
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent) {
        // 딥링크로부터 받은 id를 추출하기 위함
        val deepLinkHearitId =
            intent.data
                ?.takeIf { uri -> uri.scheme?.startsWith("kakao") == true }
                ?.getQueryParameter("id")
                ?.toLongOrNull()
                ?.takeIf { it > -1 }

        // 딥링크 id -> Activity가 최초 실행될 때 저장된 id
        val targetId = deepLinkHearitId ?: hearitId

        if (targetId > -1) {
            viewModel.refreshData(targetId)
        }
    }

    private fun connectController() {
        if (mediaController != null) return
        val sessionToken = SessionToken(this, ComponentName(this, PlaybackService::class.java))

        lifecycleScope.launch {
            runCatching {
                MediaController
                    .Builder(this@PlayerDetailActivity, sessionToken)
                    .buildAsync()
                    .await()
            }.onSuccess { controller ->
                mediaController = controller
                binding.playerView.player = controller
                binding.baseController.setPlayer(controller)
                controller.addListener(playerListener)
            }
        }
    }

    private fun disconnectController() {
        mediaController?.removeListener(playerListener)
        mediaController?.release()
        mediaController = null
    }

    private fun setupRecyclerView() {
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

    private fun setupDetailScript() {
        binding.cvScript.setContent {
            val hearit by viewModel.hearit.observeAsState()
            val highlightedId by viewModel.highlightedId.collectAsStateWithLifecycle()

            DetailScripts(
                scriptLines = hearit?.script.orEmpty(),
                highlightedId = highlightedId,
                onClick = { openScriptFragment() },
            )
        }
    }

    private fun openScriptFragment() {
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.slide_up, 0)
            .replace(R.id.fragment_container_view, ScriptFragment.newInstance())
            .addToBackStack(null)
            .commit()
    }

    private fun observeViewModel() {
        viewModel.hearit.observe(this) { hearit ->
            binding.hearit = hearit
            keywordAdapter.submitList(hearit?.keywords)
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

        viewModel.showLoginDialog.observe(this) { reason ->
            val messageRes =
                when (reason) {
                    LoginReason.BOOKMARK -> R.string.all_login_required_bookmark
                    LoginReason.LIKE -> R.string.all_login_required_like
                }
            showLoginRequiredDialog(messageRes)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.isLiked.collect {
                        binding.btnLike.root.isSelected = it
                    }
                }

                launch {
                    viewModel.likeCount.collect {
                        binding.btnLike.tvLike.text = it.toString()
                    }
                }
            }
        }
    }

    private fun handlePlayback(hearit: Hearit) {
        val controller = mediaController ?: return
        val currentlyPlayingId = controller.currentMediaItem?.mediaId?.toLongOrNull()
        val isDifferentHearit = currentlyPlayingId != hearit.id
        val shouldResume = intent.hasExtra(LAST_POSITION_KEY) && lastPosition > 0L
        val startPosition = if (shouldResume) lastPosition else hearit.lastPlayTime ?: 0L

        if (previousScreen == LIBRARY_SCREEN_ID) {
            // 재생목록 모드: 서비스가 큐 세팅을 담당
            if (isDifferentHearit || controller.mediaItemCount == 1) {
                startLibraryPlayback(
                    seedHearitId = hearit.id,
                    seedBookmarkId = viewModel.bookmarkId.value,
                    startPositionMs = startPosition,
                )
                return
            }
        } else {
            // 단일 재생 모드
            if (isDifferentHearit || controller.mediaItemCount != 1) {
                playSingleWithController(hearit, previousScreen, startPosition)
            } else {
                if (!controller.isPlaying) controller.play()
                if (shouldResume && abs(controller.currentPosition - startPosition) > 1000) {
                    controller.seekTo(startPosition)
                }
            }
        }
    }

    @OptIn(UnstableApi::class)
    private fun startLibraryPlayback(
        seedHearitId: Long,
        seedBookmarkId: Long?,
        startPositionMs: Long,
    ) {
        val controller = mediaController ?: return
        val args =
            Bundle().apply {
                putLong(EXTRA_SEED_HEARIT_ID, seedHearitId)
                putLong(EXTRA_SEED_BOOKMARK_ID, seedBookmarkId ?: -1)
                putLong(EXTRA_START_POSITION_MS, startPositionMs)
            }

        controller.sendCustomCommand(
            PlaybackSessionCallback.START_LIBRARY_PLAY_COMMAND,
            args,
        )
    }

    private fun setupBaseControllerBookmark() {
        binding.baseController.setOnBookmarkClickListener {
            viewModel.toggleBookmark()
        }
    }

    private fun setupLikeButton() {
        binding.btnLike.root.setOnClickListener {
            viewModel.toggleLike()
        }
    }

    // 단일 재생 전용: 커맨드 전송 없이 setMediaItem만 수행
    private fun playSingleWithController(
        hearit: Hearit,
        previousScreen: String,
        startPositionMs: Long,
    ) {
        val controller = mediaController ?: return

        val audioUri =
            hearit.audioUrl
                ?.takeIf { it.isNotBlank() }
                ?.toUri()
                ?: run {
                    Timber.w("Missing audioUrl for hearit id=${hearit.id}")
                    Toast.makeText(this, "오디오 URL이 없어 재생할 수 없어요.", Toast.LENGTH_SHORT).show()
                    return
                }

        val extras =
            Bundle().apply {
                putLong(KEY_BOOKMARK_ID, hearit.bookmarkId ?: -1L)
                putString(KEY_PLAYBACK_MODE, previousScreen)
            }

        val item =
            MediaItem
                .Builder()
                .setMediaId(hearit.id.toString())
                .setUri(audioUri)
                .setMediaMetadata(
                    MediaMetadata
                        .Builder()
                        .setTitle(hearit.title)
                        .setArtist(hearit.sources.firstOrNull()?.name ?: "hEARit")
                        .setExtras(extras)
                        .build(),
                ).build()

        controller.setMediaItem(item, startPositionMs)
        controller.prepare()
        controller.play()
    }

    private fun startScriptSyncLoop() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (true) {
                    mediaController?.let { controller ->
                        val position = controller.currentPosition
                        val current =
                            viewModel.hearit.value
                                ?.script
                                ?.firstOrNull { position in it.start until it.end }
                        viewModel.setHighlightedId(current?.id)
                    }
                    delay(updateIntervalMs)
                }
            }
        }
    }

    private fun setupBackPressHandler() {
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    handleBackAction()
                }
            },
        )

        binding.ibPlayerDetailBack.setOnClickListener { handleBackAction() }
    }

    /** 뒤로가기 액션 처리 */
    private fun handleBackAction() {
        lifecycleScope.launch {
            runCatching {
                mediaController
                    ?.sendCustomCommand(
                        PlaybackSessionCallback.FLUSH_PLAYBACK_COMMAND,
                        Bundle.EMPTY,
                    )?.await()
            }
            finishWithResult()
        }
    }

    /** 결과 Intent 세팅 후 finish */
    private fun finishWithResult() {
        if (previousScreen == EXPLORE_VALUE) {
            val resultIntent =
                Intent().apply {
                    putExtra(TYPE_KEY, EXPLORE_VALUE)
                    putExtra(HEARIT_ID_KEY, currentHearitId)
                    viewModel.bookmarkId.value?.let { putExtra(BOOKMARK_ID_KEY, it) }
                }
            setResult(RESULT_OK, resultIntent)
        } else {
            setResult(RESULT_CANCELED)
        }
        finish()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, systemBars.bottom)
            insets
        }
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
    }

    private fun showLoginRequiredDialog(
        @StringRes messageRes: Int,
    ) {
        LoginRequiredDialogFragment(
            messageRes = messageRes,
            onPositive = { navigateToLogin() },
        ).show(supportFragmentManager, LOGIN_REQUIRED_DIALOG_TAG)
    }

    private fun navigateToLogin() {
        analyticsLogger.logEvent(
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

    private fun startKakaoInvite(context: Context) {
        val hearit = viewModel.hearit.value ?: return
        val arguments =
            hashMapOf(
                TEMPLATE_TITLE_KEY to hearit.title,
                TEMPLATE_HEARIT_ID_KEY to hearit.id.toString(),
            )

        if (ShareClient.instance.isKakaoTalkSharingAvailable(context)) {
            ShareClient.instance.shareCustom(
                context,
                TEMPLATE_ID,
                arguments,
            ) { sharingResult, error ->
                if (error != null) {
                    Timber.e(error, getString(R.string.player_detail_invite_error_kakao))
                    showToast(getString(R.string.player_detail_invite_error_kakao))
                } else if (sharingResult != null) {
                    analyticsLogger.logEvent(
                        AnalyticsEventNames.DETAIL_KAKAO_SHARE,
                        mapOf(AnalyticsParamKeys.ITEM_ID to hearitId.toString()),
                    )
                    startActivity(sharingResult.intent)
                }
            }
        } else {
            // 카카오톡 미설치: 웹 공유 사용 권장
            val sharerUrl = WebSharerClient.instance.makeCustomUrl(TEMPLATE_ID, arguments)

            // 1. CustomTabsServiceConnection 지원 브라우저 열기
            // ex) Chrome, 삼성 인터넷, FireFox, 웨일 등
            try {
                KakaoCustomTabsClient.openWithDefault(context, sharerUrl)
                analyticsLogger.logEvent(
                    AnalyticsEventNames.DETAIL_KAKAO_SHARE,
                    mapOf(AnalyticsParamKeys.ITEM_ID to hearitId.toString()),
                )
                return
            } catch (e: UnsupportedOperationException) {
                // CustomTabsServiceConnection 지원 브라우저가 없을 때 예외처리
                Timber.w(e, getString(R.string.player_detail_invite_error_browser))
                showToast(getString(R.string.player_detail_invite_error_browser))
            }

            // 2. CustomTabsServiceConnection 미지원 브라우저 열기
            // ex) 다음, 네이버 등
            try {
                KakaoCustomTabsClient.open(context, sharerUrl)
                analyticsLogger.logEvent(
                    AnalyticsEventNames.DETAIL_KAKAO_SHARE,
                    mapOf(AnalyticsParamKeys.ITEM_ID to hearitId.toString()),
                )
                return
            } catch (e: ActivityNotFoundException) {
                // 디바이스에 설치된 인터넷 브라우저가 없을 때 예외처리
                Timber.e(e, getString(R.string.player_detail_invite_error_browser))
                showToast(getString(R.string.player_detail_invite_error_browser))
            }
        }
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

            analyticsLogger.logEvent(
                AnalyticsEventNames.DETAIL_SOURCE_SELECTED,
                mapOf(AnalyticsParamKeys.SOURCE_NAME to name),
            )
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        } catch (e: Exception) {
            Timber.w(e)
            showToast(ERROR_INVALID_LINK_MESSAGE)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disconnectController()
    }

    companion object {
        const val BOOKMARK_ID = "bookmarkId"
        const val LIBRARY_SCREEN_ID = "library"
        const val UNKNOWN_SCREEN_ID = "unknown"
        const val LOGIN_REQUIRED_DIALOG_TAG = "login_required_dialog"
        private const val ERROR_UNSUPPORTED_LINK_MESSAGE = "지원되지 않는 링크입니다"
        private const val ERROR_INVALID_LINK_MESSAGE = "잘못된 링크 형식입니다"
        private const val TEMPLATE_ID = 124931L
        private const val TEMPLATE_TITLE_KEY = "title"
        private const val TEMPLATE_HEARIT_ID_KEY = "id"
        private const val KEY_BOOKMARK_ID = "BOOKMARK_ID"
        private const val KEY_PLAYBACK_MODE = "PLAYBACK_MODE"

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
