package com.onair.hearit

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.LinearLayoutManager
import com.onair.hearit.databinding.ActivityPlayerDetailBinding

class PlayerDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlayerDetailBinding
    private lateinit var player: ExoPlayer
    private lateinit var layoutManager: LinearLayoutManager

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
        )

    private lateinit var playerDetailScriptAdapter: PlayerDetailScriptAdapter

    private val handler = Handler(Looper.getMainLooper())
    private val updateInterval = 300L

    private val itemHeightPx by lazy {
        val scale = resources.displayMetrics.density
        (16 * scale + 0.5f).toInt()
    }

    private val updateRunnable =
        object : Runnable {
            override fun run() {
                val pos = player.currentPosition
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

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_player_detail)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        player =
            ExoPlayer.Builder(this).build().apply {
                val uri = "android.resource://$packageName/${R.raw.test_audio}".toUri()
                val mediaItem = MediaItem.fromUri(uri)
                setMediaItem(mediaItem)
                prepare()
                playWhenReady = true
            }

        binding.playerView.player = player
        binding.baseController.setPlayer(player)

        // 속도 설정
        player.playbackParameters = PlaybackParameters(1.0f)

        layoutManager = LinearLayoutManager(this)
        playerDetailScriptAdapter = PlayerDetailScriptAdapter(scriptLines)
        binding.rvScript.apply {
            this.layoutManager = this@PlayerDetailActivity.layoutManager
            adapter = playerDetailScriptAdapter
        }

        // 스크립트 하이라이트 업데이트 루프 시작
        handler.post(updateRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
        handler.removeCallbacks(updateRunnable)
    }
}
