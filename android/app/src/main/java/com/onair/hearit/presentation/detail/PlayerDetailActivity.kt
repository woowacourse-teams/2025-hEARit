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
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.Player
import androidx.media3.common.Timeline
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
import com.onair.hearit.presentation.IntentValues.KEYWORD_VALUE
import com.onair.hearit.presentation.LoginRequiredDialogFragment
import com.onair.hearit.presentation.detail.script.ScriptFragment
import com.onair.hearit.presentation.dpToPx
import com.onair.hearit.presentation.login.LoginActivity
import com.onair.hearit.service.PlaybackService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.abs

class PlayerDetailActivity :
    AppCompatActivity(),
    PlayerDetailClickListener {
    private lateinit var binding: ActivityPlayerDetailBinding
    private val keywordAdapter: PlayerDetailKeywordAdapter by lazy { PlayerDetailKeywordAdapter(this) }
    private val scriptAdapter: PlayerDetailScriptAdapter by lazy { PlayerDetailScriptAdapter() }
    private val sourceAdapter: PlayerDetailSourceAdapter by lazy { PlayerDetailSourceAdapter(this) }

    private var mediaController: MediaController? = null
    private var scriptSyncJob: Job? = null
    private val updateInterval = 500L
    private val itemHeightPx: Int by lazy { SCRIPT_ITEM_HEIGHT_DP.dpToPx(this) }
    private val previousScreen by lazy {
        intent.getStringExtra(PREVIOUS_SCREEN_KEY) ?: UNKNOWN_SCREEN_ID
    }
    private val hearitId: Long by lazy { intent.getLongExtra(HEARIT_ID_KEY, -1) }
    private val lastPosition: Long by lazy { intent.getLongExtra(LAST_POSITION_KEY, 0) }

    private val viewModel: PlayerDetailViewModel by viewModels {
        PlayerDetailViewModelFactory(hearitId)
    }

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
        setupMediaController()
        setupBaseControllerBookmark()

        supportFragmentManager.addOnBackStackChangedListener {
            val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container_view)
            binding.fragmentContainerView.visibility =
                if (fragment != null && fragment.isVisible) View.VISIBLE else View.GONE
        }
    }

    private fun setupBackPressHandler() {
        val backAction = {
            if (previousScreen == EXPLORE_VALUE) {
                val resultIntent =
                    Intent().apply {
                        putExtra(TYPE_KEY, EXPLORE_VALUE)
                        putExtra(HEARIT_ID_KEY, hearitId)
                        viewModel.bookmarkId.value?.let { putExtra(BOOKMARK_ID_KEY, it) }
                    }
                setResult(RESULT_OK, resultIntent)
            } else {
                setResult(RESULT_CANCELED)
            }
            finish()
        }

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() = backAction()
            },
        )

        binding.ibPlayerDetailBack.setOnClickListener {
            backAction()
        }
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, systemBars.bottom)
            insets
        }
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
    }

    @OptIn(UnstableApi::class)
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

        binding.rvScript.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
        }
    }

    private fun setupRecyclerView() {
        binding.rvScript.adapter = scriptAdapter
        setupGestureListener()
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

    @OptIn(UnstableApi::class)
    private fun observeViewModel() {
        viewModel.hearit.observe(this) { hearit ->
            binding.hearit = hearit
            keywordAdapter.submitList(hearit.keywords)
            scriptAdapter.submitList(hearit.script)
            sourceAdapter.submitList(hearit.sources)
            handlePlayback(hearit)
        }

        viewModel.bookmarkId.observe(this) { bookmarkId ->
            binding.baseController.setBookmarkSelected(bookmarkId != null)
        }

        viewModel.toastMessage.observe(this) { msgResId ->
            Toast.makeText(this, getString(msgResId), Toast.LENGTH_SHORT).show()
        }

        viewModel.showLoginDialog.observe(this) {
            showLoginRequiredDialog()
        }
    }

    private fun startScriptSync(controller: Player) {
        scriptSyncJob =
            lifecycleScope.launch {
                while (isActive) {
                    val position = controller.currentPosition

                    val currentItem =
                        scriptAdapter.currentList.firstOrNull { position in it.start until it.end }

                    if (currentItem != null) {
                        scriptAdapter.highlightScriptLine(currentItem.id)

                        val currentIndex = scriptAdapter.currentList.indexOf(currentItem)
                        val centerOffset = binding.rvScript.height / 2 - itemHeightPx / 2

                        (binding.rvScript.layoutManager as LinearLayoutManager)
                            .scrollToPositionWithOffset(currentIndex, centerOffset)
                    }
                    delay(updateInterval)
                }
            }
    }

    @OptIn(UnstableApi::class)
    private fun setupBaseControllerBookmark() {
        binding.baseController.setOnBookmarkClickListener {
            viewModel.toggleBookmark()
        }
    }

    private fun handlePlayback(hearit: Hearit) {
        val controller = mediaController ?: return
        val currentlyPlayingId = controller.currentMediaItem?.mediaId?.toLongOrNull()
        val isDifferentHearit = currentlyPlayingId != hearit.id
        val shouldResume = intent.hasExtra(LAST_POSITION_KEY) && lastPosition > 0L
        val startPosition = if (shouldResume) lastPosition else 0L
        val source = hearit.sources.first().name

        if (isDifferentHearit) {
            startPlaybackService(hearit.audioUrl, hearit.title, startPosition, source)
        } else {
            if (!controller.isPlaying) controller.play()
            if (shouldResume && abs(controller.currentPosition - startPosition) > 1000) {
                controller.seekTo(startPosition)
            }
        }
    }

    private fun showLoginRequiredDialog() {
        LoginRequiredDialogFragment {
            navigateToLogin()
        }.show(supportFragmentManager, LOGIN_REQUIRED_DIALOG_TAG)
    }

    private fun startPlaybackService(
        audioUrl: String,
        title: String,
        startPosition: Long = 0L,
        source: String,
    ) {
        val serviceIntent =
            PlaybackService.newIntent(
                context = this,
                audioUrl = audioUrl,
                title = title,
                hearitId = hearitId,
                startPosition = startPosition,
                source = source,
            )
        startForegroundService(serviceIntent)
    }

    private fun navigateToLogin() {
        AnalyticsProvider.get().logEvent(
            AnalyticsEventNames.LOGIN_EVENT,
            mapOf(AnalyticsParamKeys.SOURCE_NAME to "detail_login"),
        )

        val intent = LoginActivity.newIntent(this)
        startActivity(intent)

        val serviceIntent = Intent(this, PlaybackService::class.java)
        this.stopService(serviceIntent)

        finish()
    }

    private fun showToast(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onClickCategory(
        id: Long,
        name: String,
    ) {
        AnalyticsProvider.get().logEvent(
            AnalyticsEventNames.DETAIL_CATEGORY_SELECTED,
            mapOf(AnalyticsParamKeys.CATEGORY_NAME to name),
        )
        val input = SearchInput.Category(id, name)
        val resultIntent = Intent().apply { putExtras(input.toBundle()) }
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
        scriptSyncJob?.cancel()
        binding.playerView.player = null
        mediaController?.release()
    }

    companion object {
        const val UNKNOWN_SCREEN_ID = "unknown"
        const val LOGIN_REQUIRED_DIALOG_TAG = "login_required_dialog"
        private const val ERROR_UNSUPPORTED_LINK_MESSAGE = "지원되지 않는 링크입니다"
        private const val ERROR_INVALID_LINK_MESSAGE = "잘못된 링크 형식입니다"
        private const val SCRIPT_ITEM_HEIGHT_DP = 16

        fun newIntent(
            context: Context,
            hearitId: Long,
            lastPosition: Long? = null,
        ): Intent =
            Intent(context, PlayerDetailActivity::class.java).apply {
                putExtra(HEARIT_ID_KEY, hearitId)
                lastPosition?.let { putExtra(LAST_POSITION_KEY, it) }
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
    }
}
