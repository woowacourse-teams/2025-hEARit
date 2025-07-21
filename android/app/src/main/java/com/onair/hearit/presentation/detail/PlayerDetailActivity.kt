package com.onair.hearit.presentation.detail

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.concurrent.futures.await
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.recyclerview.widget.LinearLayoutManager
import com.onair.hearit.R
import com.onair.hearit.databinding.ActivityPlayerDetailBinding
import com.onair.hearit.domain.ScriptLine
import kotlinx.coroutines.launch

class PlayerDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlayerDetailBinding
    private lateinit var layoutManager: LinearLayoutManager

    private var mediaController: MediaController? = null

    // 스크립트 예시 (실제 데이터로 변경해야함)
    private val scriptLines =
        listOf(
            ScriptLine(0, 1500, "I'm your pookie in the morning"),
            ScriptLine(1500, 4000, "You're my pookie in the night"),
            ScriptLine(4000, 6500, "너만 보면 Super crazy"),
            ScriptLine(6500, 8000, "Oh my 아찔 gets the vibe"),
            ScriptLine(8000, 11000, "Don't matter what I do"),
            ScriptLine(11000, 13000, "하나 둘 셋 Gimme that cue"),
            ScriptLine(13000, 14000, "Cuz I'm your pookie"),
            ScriptLine(14000, 16000, "두근두근 Gets the sign"),
            ScriptLine(16000, 18000, "That's right"),
            ScriptLine(18000, 21000, "내 Fresh new 립스틱 Pick해, 오늘의 color"),
            ScriptLine(21000, 25000, "Oh my, 새로 고침 거울 속 느낌 공기도 달라"),
            ScriptLine(25000, 29000, "밉지 않지 All I gotta do is blow a kiss"),
            ScriptLine(29000, 34000, "Even salt tastes sweet, 이건 Z to A, And never felt like this"),
            ScriptLine(
                34000,
                38000,
                "Cuz I get what I want, and I want what I get, like every time",
            ),
            ScriptLine(
                38000,
                42000,
                "Cuz I glow when I roll out of bed, No regrets I'm living my life",
            ),
            ScriptLine(42000, 46000, "지루한 걱정 따윈 no more"),
            ScriptLine(46000, 50000, "이제는 어쩜 love sick and we know it"),
            ScriptLine(
                50000,
                54000,
                "I'm your pookie in the morning, I'm your pookie in the night",
            ),
            ScriptLine(54000, 58000, "너만 보면 super crazy, 두근두근, gets the vibe"),
        )

    private lateinit var playerDetailScriptAdapter: PlayerDetailScriptAdapter

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

        setupWindowInsets()
        initMediaController()

        layoutManager = LinearLayoutManager(this)
        playerDetailScriptAdapter = PlayerDetailScriptAdapter(scriptLines)
        binding.rvScript.apply {
            this.layoutManager = this@PlayerDetailActivity.layoutManager
            adapter = playerDetailScriptAdapter
        }

        binding.btnHearitPlayerBookmark.setOnClickListener {
            it.isSelected = !it.isSelected
        }

        binding.ibPlayerDetailBack.setOnClickListener {
            finish()
        }
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
    private fun initMediaController() {
        val serviceIntent = Intent(this, PlaybackService::class.java)
        startService(serviceIntent)

        val sessionToken = SessionToken(this, ComponentName(this, PlaybackService::class.java))

        lifecycleScope.launch {
            val controller =
                MediaController
                    .Builder(this@PlayerDetailActivity, sessionToken)
                    .buildAsync()
                    .await()

            binding.playerView.player = controller
            binding.baseController.setPlayer(controller)

            controller.prepare()
            controller.play()

            startScriptSync(controller)
        }
    }

    private fun startScriptSync(controller: Player) {
        val updateRunnable =
            object : Runnable {
                override fun run() {
                    val pos = controller.currentPosition
                    val currentIndex =
                        scriptLines.indexOfFirst { pos in it.startTimeMs until it.endTimeMs }

                    if (currentIndex != -1) {
                        playerDetailScriptAdapter.highlightPosition(currentIndex)

                        val centerOffset = binding.rvScript.height / 2 - itemHeightPx / 2
                        binding.rvScript.post {
                            layoutManager.scrollToPositionWithOffset(currentIndex, centerOffset)
                        }
                    }

                    handler.postDelayed(this, updateInterval)
                }
            }

        handler.post(updateRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        mediaController?.release()
    }

    companion object {
        fun newIntent(context: Context): Intent =
            Intent(context, PlayerDetailActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
    }
}
