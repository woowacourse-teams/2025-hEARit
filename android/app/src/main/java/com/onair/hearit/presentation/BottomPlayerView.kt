package com.onair.hearit.presentation

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.TimeBar
import com.onair.hearit.R
import com.onair.hearit.databinding.LayoutBottomPlayerControllerBinding
import kotlin.math.max

@UnstableApi
class BottomPlayerView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : ConstraintLayout(context, attrs, defStyleAttr) {
        private var player: Player? = null
        private var listener: PlayerListener? = null

        // 스냅샷(정적 표시) 상태 관리
        private var isSnapshot: Boolean = false
        private var snapshotDuration: Long = 0L // 총 길이(ms)
        private var snapshotPosition: Long = 0L // 마지막 위치(ms)

        private val binding =
            LayoutBottomPlayerControllerBinding.inflate(
                LayoutInflater.from(context),
                this,
                true,
            )

        private val window = Timeline.Window()
        private val progressRunnable = Runnable { updateProgress() }

        init {
            setupScrubListener()
            binding.exoPlay.setOnClickListener { togglePlayPause() }
        }

        fun showSnapshot(
            title: String,
            duration: Long,
            position: Long,
        ) {
            isSnapshot = true
            snapshotDuration = duration.coerceAtLeast(0) // 총 길이
            snapshotPosition = position.coerceAtLeast(0) // 마지막 위치
            setTitle(title)
            binding.exoProgress.setDuration(snapshotDuration)
            binding.exoProgress.setPosition(snapshotPosition)
            removeCallbacks(progressRunnable) // 프리뷰는 정적으로 유지
        }

        fun setPlayer(newPlayer: Player): BottomPlayerView =
            apply {
                detachPlayer()
                player = newPlayer
                listener = PlayerListener().also { newPlayer.addListener(it) }
                refresh()
            }

        fun setTitle(title: String) {
            binding.tvBottomPlayerTitle.isSelected = true
            binding.tvBottomPlayerTitle.text = title
        }

        private fun setupScrubListener() {
            binding.exoProgress.addListener(
                object : TimeBar.OnScrubListener {
                    override fun onScrubStart(
                        timeBar: TimeBar,
                        position: Long,
                    ) {
                    }

                    override fun onScrubMove(
                        timeBar: TimeBar,
                        position: Long,
                    ) {
                    }

                    override fun onScrubStop(
                        timeBar: TimeBar,
                        position: Long,
                        canceled: Boolean,
                    ) {
                        val currentPlayer = player
                        if (currentPlayer != null) {
                            currentPlayer.seekTo(position)
                            updateProgress()
                            (context as? PlaybackPositionSaver)?.savePlaybackPosition()
                        }
                    }
                },
            )
        }

        private fun refresh() {
            player?.let {
                updateTimeline(it)
                updatePlayPauseButton(it)
            }
        }

        private fun updateTimeline(player: Player) {
            val timeline = player.currentTimeline
            if (timeline.isEmpty) {
                if (!isSnapshot) binding.exoProgress.setDuration(0)
                return
            }

            timeline.getWindow(player.currentMediaItemIndex, window)
            val duration = window.durationMs.coerceAtLeast(0)

            if (isSnapshot) {
                val targetDuration = if (snapshotDuration > 0L) snapshotDuration else duration
                binding.exoProgress.setDuration(targetDuration)
                return
            }

            binding.exoProgress.setDuration(duration)
            updateProgress()
        }

        private fun updateProgress() {
            if (!isAttachedToWindow) return
            val currentPlayer = player ?: return

            removeCallbacks(progressRunnable)

            // 1) 스냅샷 상태: 플레이어가 READY 되는 '즉시' 한 번만 라이브로 전환
            if (isSnapshot) {
                if (currentPlayer.playbackState == Player.STATE_READY) {
                    isSnapshot = false
                    // 전환 시 duration은 스냅샷과 실제 중 큰 값으로 한 번만 세팅
                    binding.exoProgress.setDuration(
                        max(snapshotDuration, currentPlayer.duration.coerceAtLeast(0)),
                    )
                    binding.exoProgress.setPosition(currentPlayer.currentPosition)
                    binding.exoProgress.setBufferedPosition(currentPlayer.bufferedPosition)
                    // 이후 아래 라이브 루프로 자연스럽게 이어짐
                } else {
                    // 아직 READY 전이면 프리뷰는 고정하고 잠시 후 다시 확인
                    postDelayed(progressRunnable, 100)
                    return
                }
            }

            // 2) 라이브(=스냅샷 해제 후) 업데이트 루프
            if (currentPlayer.playbackState == Player.STATE_READY) {
                binding.exoProgress.setPosition(currentPlayer.currentPosition)
                binding.exoProgress.setBufferedPosition(currentPlayer.bufferedPosition)
            }

            // 재생 중일 때만 주기적으로 갱신
            if (currentPlayer.playWhenReady && currentPlayer.playbackState == Player.STATE_READY) {
                postDelayed(progressRunnable, binding.exoProgress.preferredUpdateDelay)
            }
        }

        private fun updatePlayPauseButton(current: Player) {
            val isPlaying = current.playWhenReady && current.playbackState == Player.STATE_READY
            val icon = if (isPlaying) R.drawable.ic_bottom_pause else R.drawable.ic_bottom_play
            binding.exoPlay.setImageResource(icon)
        }

        private fun togglePlayPause() {
            val currentPlayer = player
            if (currentPlayer == null) {
                (context as? PlaybackStarter)?.startPlayback()
                return
            }

            if (currentPlayer.isPlaying) {
                currentPlayer.pause()
                (context as? PlaybackPositionSaver)?.savePlaybackPosition()
            } else {
                currentPlayer.play()
            }
        }

        private fun detachPlayer() {
            listener?.let { player?.removeListener(it) }
            listener = null
            player = null
        }

        override fun onDetachedFromWindow() {
            super.onDetachedFromWindow()
            removeCallbacks(progressRunnable)
            detachPlayer()
        }

        private inner class PlayerListener : Player.Listener {
            override fun onEvents(
                player: Player,
                events: Player.Events,
            ) {
                if (events.contains(Player.EVENT_MEDIA_METADATA_CHANGED)) {
                    player.mediaMetadata.title
                        ?.toString()
                        ?.takeIf { it.isNotBlank() }
                        ?.let { setTitle(it) }
                }
                if (events.contains(Player.EVENT_TIMELINE_CHANGED)) {
                    updateTimeline(player)
                }
                if (
                    events.contains(Player.EVENT_PLAYBACK_STATE_CHANGED) ||
                    events.contains(Player.EVENT_IS_PLAYING_CHANGED)
                ) {
                    updatePlayPauseButton(player)
                    updateProgress()
                }
            }
        }

        companion object {
            private const val SNAPSHOT_TRANSITION_TOLERANCE_MS = 600L
        }
    }
