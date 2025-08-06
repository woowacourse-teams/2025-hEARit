package com.onair.hearit.presentation.detail

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.ui.TimeBar
import com.onair.hearit.R
import com.onair.hearit.databinding.LayoutControllerBinding
import com.onair.hearit.presentation.PlaybackPositionSaver
import java.util.Formatter
import java.util.Locale

@UnstableApi
class BaseControllerView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : LinearLayout(context, attrs, defStyleAttr) {
        private lateinit var player: Player
        private lateinit var binding: LayoutControllerBinding

        private val formatBuilder = StringBuilder()
        private val formatter = Formatter(formatBuilder, Locale.getDefault())

        private val window = Timeline.Window()
        private var playSpeedIndex = DEFAULT_SPEED_INDEX

        private val speedOptions = floatArrayOf(0.25f, 0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f)

        private val progressRunnable = Runnable { updateProgress() }

        init {
            initView()
        }

        private fun initView() {
            binding = LayoutControllerBinding.inflate(LayoutInflater.from(context), this, true)
        }

        fun setPlayer(player: Player) =
            apply {
                this.player = player
                setupListeners()
                updateUI()
            }

        private fun setupListeners() {
            val listener = ComponentListener()

            player.addListener(listener)
            binding.exoProgress.addListener(listener)

            binding.exoPlay.setOnClickListener { togglePlayPause() }
            binding.exoRew.setOnClickListener { player.seekBack() }
            binding.exoFfwd.setOnClickListener { player.seekForward() }
            binding.playSpeed.setOnClickListener { changeSpeed() }
        }

        fun setBookmarkSelected(isSelected: Boolean) {
            binding.btnDetailBookmark.isSelected = isSelected
        }

        fun setOnBookmarkClickListener(listener: () -> Unit) {
            binding.btnDetailBookmark.setOnClickListener {
                binding.btnDetailBookmark.isSelected = !binding.btnDetailBookmark.isSelected
                listener()
            }
        }

        private fun togglePlayPause() {
            if (player.playWhenReady) {
                player.pause()
                (context as? PlaybackPositionSaver)?.savePlaybackPosition()
            } else {
                player.play()
            }
            updatePlayPauseButton()
        }

        private fun changeSpeed() {
            playSpeedIndex = (playSpeedIndex + 1) % speedOptions.size
            val speed = speedOptions[playSpeedIndex]
            player.playbackParameters = player.playbackParameters.withSpeed(speed)
            binding.playSpeed.text = "${speed}x"
        }

        private fun updateUI() {
            updateTimeline()
            updatePlayPauseButton()
            updateSpeedLabel()
        }

        private fun updateTimeline() {
            val timeline = player.currentTimeline
            val index = player.currentMediaItemIndex

            if (timeline.isEmpty || index >= timeline.windowCount) return

            timeline.getWindow(index, window)
            binding.exoProgress.setDuration(window.durationMs)
            updateProgress()
        }

        private fun updateProgress() {
            if (!isAttachedToWindow) return

            val pos = player.currentPosition
            val buf = player.bufferedPosition
            val duration = player.duration

            binding.exoPosition.text = Util.getStringForTime(formatBuilder, formatter, pos)
            binding.exoDuration.text =
                "-${Util.getStringForTime(formatBuilder, formatter, duration - pos)}"

            binding.exoProgress.setPosition(pos)
            binding.exoProgress.setBufferedPosition(buf)

            removeCallbacks(progressRunnable)
            if (player.playWhenReady && player.playbackState == Player.STATE_READY) {
                postDelayed(progressRunnable, PROGRESS_UPDATE_INTERVAL)
            }
        }

        private fun updatePlayPauseButton() {
            val icon =
                if (player.playWhenReady && player.playbackState == Player.STATE_READY) {
                    R.drawable.img_pause
                } else {
                    R.drawable.img_play
                }
            binding.exoPlay.setImageResource(icon)
        }

        private fun updateSpeedLabel() {
            val speed = player.playbackParameters.speed
            binding.playSpeed.text = "${speed}x"
        }

        private inner class ComponentListener :
            Player.Listener,
            TimeBar.OnScrubListener {
            override fun onTimelineChanged(
                timeline: Timeline,
                reason: Int,
            ) {
                updateTimeline()
            }

            override fun onEvents(
                player: Player,
                events: Player.Events,
            ) {
                if (events.containsAny(
                        Player.EVENT_TIMELINE_CHANGED,
                        Player.EVENT_PLAYBACK_STATE_CHANGED,
                        Player.EVENT_IS_PLAYING_CHANGED,
                        Player.EVENT_PLAYBACK_PARAMETERS_CHANGED,
                    )
                ) {
                    updateUI()
                }
            }

            override fun onScrubStart(
                timeBar: TimeBar,
                position: Long,
            ) {
                binding.exoPosition.text = Util.getStringForTime(formatBuilder, formatter, position)
            }

            override fun onScrubMove(
                timeBar: TimeBar,
                position: Long,
            ) {
                binding.exoPosition.text = Util.getStringForTime(formatBuilder, formatter, position)
            }

            override fun onScrubStop(
                timeBar: TimeBar,
                position: Long,
                canceled: Boolean,
            ) {
                player.seekTo(position)
                updateProgress()
                (context as? PlaybackPositionSaver)?.savePlaybackPosition()
            }
        }

        companion object {
            private const val DEFAULT_SPEED_INDEX = 3
            private const val PROGRESS_UPDATE_INTERVAL = 1000L
        }
    }
