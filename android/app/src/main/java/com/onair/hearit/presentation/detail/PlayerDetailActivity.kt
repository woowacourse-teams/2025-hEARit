package com.onair.hearit.presentation.detail

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.onair.hearit.analytics.AnalyticsParamKeys
import com.onair.hearit.analytics.AnalyticsScreenInfo
import com.onair.hearit.databinding.ActivityPlayerDetailBinding
import com.onair.hearit.di.AnalyticsProvider
import com.onair.hearit.di.CrashlyticsProvider
import com.onair.hearit.presentation.detail.script.ScriptFragment
import kotlinx.coroutines.launch

class PlayerDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlayerDetailBinding
    private val scriptAdapter by lazy { PlayerDetailScriptAdapter() }
    private val keywordAdapter by lazy { PlayerDetailKeywordAdapter() }

    private var mediaController: MediaController? = null

    private val hearitId: Long by lazy {
        intent.getLongExtra(HEARIT_ID, -1)
    }
    private val viewModel: PlayerDetailViewModel by viewModels {
        PlayerDetailViewModelFactory(hearitId, CrashlyticsProvider.get())
    }

    private val handler = Handler(Looper.getMainLooper())
    private val updateInterval = 300L

    private val itemHeightPx by lazy {
        val scale = resources.displayMetrics.density
        (16 * scale + 0.5f).toInt()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        bindLayout()
        setupBackPressHandler()
        setupWindowInsets()
        setupScriptRecyclerView()
        setKeywordRecyclerView()
        observeViewModel()
        setupMediaController()
        setupClickListener()

        val previousScreen = intent.getStringExtra(AnalyticsParamKeys.SOURCE) ?: "unknown"
        AnalyticsProvider.get().logScreenView(
            screenName = AnalyticsScreenInfo.Detail.NAME,
            screenClass = AnalyticsScreenInfo.Detail.CLASS,
            previousScreen = previousScreen,
        )

        supportFragmentManager.addOnBackStackChangedListener {
            val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container_view)
            binding.fragmentContainerView.visibility =
                if (fragment != null && fragment.isVisible) View.VISIBLE else View.GONE
        }
    }

    private fun bindLayout() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_player_detail)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
    }

    private fun setupBackPressHandler() {
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {}
            },
        )
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

    private fun setupClickListener() {
        binding.ibPlayerDetailBack.setOnClickListener {
            setResult(RESULT_OK)
            finish()
        }

        setupGestureListener()
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
                            .replace(
                                R.id.fragment_container_view,
                                ScriptFragment.newInstance(hearitId),
                            ).addToBackStack(null)
                            .commit()
                        return true
                    }

                    override fun onScroll(
                        e1: MotionEvent?,
                        e2: MotionEvent,
                        distanceX: Float,
                        distanceY: Float,
                    ): Boolean = false
                },
            )

        binding.rvScript.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
        }
    }

    private fun setupScriptRecyclerView() {
        binding.rvScript.adapter = scriptAdapter
    }

    private fun observeViewModel() {
        viewModel.hearit.observe(this) { hearit ->
            binding.hearit = hearit
            scriptAdapter.submitList(hearit.script)

            val currentlyPlayingId = mediaController?.currentMediaItem?.mediaId?.toLongOrNull()
            if (currentlyPlayingId != hearit.id) {
                startPlaybackService(
                    audioUrl = hearit.audioUrl,
                    title = hearit.title,
                )
            }
        }

        viewModel.keywords.observe(this) { keywords ->
            keywordAdapter.submitList(keywords)
        }

        viewModel.toastMessage.observe(this) { msgResId ->
            Toast.makeText(this, getString(msgResId), Toast.LENGTH_SHORT).show()
        }
    }

    private fun startScriptSync(controller: Player) {
        val updateRunnable =
            object : Runnable {
                override fun run() {
                    val pos = controller.currentPosition

                    val currentItem =
                        scriptAdapter.currentList.firstOrNull { pos in it.start until it.end }

                    if (currentItem != null) {
                        scriptAdapter.highlightScriptLine(currentItem.id)

                        val currentIndex = scriptAdapter.currentList.indexOf(currentItem)
                        val centerOffset = binding.rvScript.height / 2 - itemHeightPx / 2

                        (binding.rvScript.layoutManager as LinearLayoutManager)
                            .scrollToPositionWithOffset(currentIndex, centerOffset)
                    }

                    handler.postDelayed(this, updateInterval)
                }
            }

        handler.post(updateRunnable)
    }

    private fun startPlaybackService(
        audioUrl: String,
        title: String,
    ) {
        val serviceIntent =
            PlaybackService.newIntent(
                context = this,
                audioUrl = audioUrl,
                title = title,
                hearitId = hearitId,
            )
        startService(serviceIntent)
    }

    private fun setKeywordRecyclerView() {
        val layoutManager =
            FlexboxLayoutManager(this).apply {
                flexDirection = FlexDirection.ROW
                flexWrap = FlexWrap.WRAP
                justifyContent = JustifyContent.FLEX_START
            }

        binding.layoutSeeMore.rvKeyword.layoutManager = layoutManager
        binding.layoutSeeMore.rvKeyword.adapter = keywordAdapter
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        mediaController?.release()
    }

    companion object {
        const val HEARIT_ID = "hearit_id"

        fun newIntent(
            context: Context,
            hearitId: Long,
        ): Intent =
            Intent(context, PlayerDetailActivity::class.java).apply {
                putExtra(HEARIT_ID, hearitId)
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
    }
}
