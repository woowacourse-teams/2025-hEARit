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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

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

        private val binding =
            LayoutBottomPlayerControllerBinding.inflate(
                LayoutInflater.from(context),
                this,
                true,
            )

        private val window = Timeline.Window()
        private var progressJob: Job? = null

        init {
            setupScrubListener()
            binding.exoPlay.setOnClickListener { togglePlayPause() }
        }

        // 제대로 Player를 끊었다가 다시 연결해서 문제가 없도록 하기 위함.
        // 새로운 플레이어에 리스너를 달아주고,프로그레스바 연결
        fun setPlayer(newPlayer: Player): BottomPlayerView =
            apply {
                detachPlayer()
                player = newPlayer
                listener = PlayerListener().also { newPlayer.addListener(it) }
                refresh()

                // post를 이용해서 UI가 완전히 그려진 후에 연결되도록 함.
                post { updateProgress() }
            }

        // marquee로 길이가 긴경우에는 돌아가도록 하기 위해서 선택된 상태를 줌.
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
                    ) = Unit

                    override fun onScrubMove(
                        timeBar: TimeBar,
                        position: Long,
                    ) = Unit

                    override fun onScrubStop(
                        timeBar: TimeBar,
                        position: Long,
                        canceled: Boolean,
                    ) {
                        player?.let {
                            it.seekTo(position)
                            updateProgress()
                        }
                    }
                },
            )
        }

        private fun refresh() {
            player?.let {
                updateTimeline(it)
                updatePlayPauseButton(it)
                updateTitle(it)
            }
        }

        private fun updateTitle(player: Player) {
            val title =
                listOf(
                    player.currentMediaItem
                        ?.mediaMetadata
                        ?.title
                        ?.toString(),
                    player.mediaMetadata.title?.toString(),
                    player.currentMediaItem?.mediaId,
                ).firstOrNull { !it.isNullOrBlank() } ?: ""

            setTitle(title)
        }

        private fun updateTimeline(player: Player) {
            val timeline = player.currentTimeline
            if (timeline.isEmpty) {
                binding.exoProgress.setDuration(0)
                return
            }
            timeline.getWindow(player.currentMediaItemIndex, window)
            val duration = window.durationMs.coerceAtLeast(0)
            binding.exoProgress.setDuration(duration)
            updateProgress()
        }

        private fun updateProgress() {
            if (!isAttachedToWindow) return
            val currentPlayer = player ?: return

            // 이미 실행 중인 경우 다시 시작하지 않음
            if (progressJob?.isActive == true) return

            progressJob =
                CoroutineScope(Dispatchers.Main.immediate).launch {
                    while (isActive) {
                        if (currentPlayer.playbackState == Player.STATE_READY && currentPlayer.playWhenReady) {
                            binding.exoProgress.setPosition(currentPlayer.currentPosition)
                            binding.exoProgress.setBufferedPosition(currentPlayer.bufferedPosition)
                        }
                        delay(binding.exoProgress.preferredUpdateDelay)
                    }
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
            } else {
                currentPlayer.play()
            }
        }

        private fun detachPlayer() {
            progressJob?.cancel()
            listener?.let { player?.removeListener(it) }
            listener = null
            player = null
        }

        // onDetachedFromWindow()는 뷰(View)가 화면에서 분리될 때 호출되는 안드로이드 생명주기 메서드
        // remove와 같은 정리 작업을 하는 경우에 사용됨.
        // detachPlayer()는 Player 객체와의 연결을 해제하는 함수
        override fun onDetachedFromWindow() {
            super.onDetachedFromWindow()
            progressJob?.cancel()
            progressJob = null
            detachPlayer()
        }

        private inner class PlayerListener : Player.Listener {
            override fun onEvents(
                player: Player,
                events: Player.Events,
            ) {
                if (events.containsAny(
                        Player.EVENT_MEDIA_METADATA_CHANGED,
                        Player.EVENT_MEDIA_ITEM_TRANSITION,
                        Player.EVENT_TIMELINE_CHANGED,
                    )
                ) {
                    refresh()
                } else if (events.containsAny(
                        Player.EVENT_PLAYBACK_STATE_CHANGED,
                        Player.EVENT_IS_PLAYING_CHANGED,
                    )
                ) {
                    updatePlayPauseButton(player)
                    updateProgress()
                }
            }
        }
    }
