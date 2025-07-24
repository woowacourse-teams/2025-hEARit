package com.onair.hearit.presentation.detail

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
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
import com.onair.hearit.R
import com.onair.hearit.databinding.ActivityPlayerDetailBinding
import com.onair.hearit.service.PlaybackService
import kotlinx.coroutines.launch

class PlayerDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlayerDetailBinding
    private val adapter: PlayerDetailScriptAdapter by lazy { PlayerDetailScriptAdapter() }

    private var mediaController: MediaController? = null

    private val hearitId: Long by lazy {
        intent.getLongExtra(HEARIT_ID, -1)
    }
    private val viewModel: PlayerDetailViewModel by viewModels {
        PlayerDetailViewModelFactory(hearitId)
    }

    private val handler = Handler(Looper.getMainLooper())
    private val updateInterval = 300L

    private val itemHeightPx by lazy {
        val scale = resources.displayMetrics.density
        (16 * scale + 0.5f).toInt()
    }

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_player_detail)
        binding.lifecycleOwner = this

        setupWindowInsets()
        binding.rvScript.adapter = adapter

        observeViewModel()
        setupMediaController()
        setupClickListener()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
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

            controller.addListener(
                object : Player.Listener {
                    override fun onTimelineChanged(
                        timeline: Timeline,
                        reason: Int,
                    ) {
                        if (timeline.windowCount > 0) {
                            binding.baseController.setPlayer(controller)
                        }
                    }
                },
            )

            controller.prepare()
            controller.play()

            startScriptSync(controller)
        }
    }

    private fun setupClickListener() {
        binding.btnHearitPlayerBookmark.setOnClickListener {
            it.isSelected = !it.isSelected
        }

        binding.ibPlayerDetailBack.setOnClickListener {
            setResult(RESULT_OK)
            finish()
        }
    }

    private fun observeViewModel() {
        viewModel.hearit.observe(this) { hearit ->
            binding.hearit = hearit
            adapter.submitList(hearit.script)

            startPlaybackService(
                audioUrl = hearit.audioUrl,
                title = hearit.title,
            )
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
                        adapter.currentList.firstOrNull { pos in it.start until it.end }

                    if (currentItem != null) {
                        adapter.highlightScriptLine(currentItem.id)

                        val currentIndex = adapter.currentList.indexOf(currentItem)
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
            Intent(this, PlaybackService::class.java).apply {
                putExtra("AUDIO_URL", audioUrl)
                putExtra("TITLE", title)
            }
        startService(serviceIntent)
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
