package com.onair.hearit.presentation

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.ui.DefaultTimeBar
import androidx.media3.ui.TimeBar
import com.onair.hearit.R
import com.onair.hearit.databinding.LayoutBottomPlayerControllerBinding
import java.util.Formatter
import java.util.Locale

@UnstableApi
class BottomPlayerView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : ConstraintLayout(context, attrs, defStyleAttr) {
        private lateinit var player: Player
        private lateinit var binding: LayoutBottomPlayerControllerBinding

        private lateinit var timeBar: DefaultTimeBar
        private lateinit var playButton: ImageButton
        private lateinit var durationTextView: TextView

        private val formatBuilder = StringBuilder()
        private val formatter = Formatter(formatBuilder, Locale.getDefault())
        private val window = Timeline.Window()

        private val progressRunnable = Runnable { updateProgress() }
        private lateinit var listener: PlayerListener

        init {
            initView()
        }

        private fun initView() {
            binding =
                LayoutBottomPlayerControllerBinding.inflate(
                    LayoutInflater.from(context),
                    this,
                    true,
                )

            timeBar = binding.exoProgress
            playButton = binding.exoPlay
            durationTextView = binding.exoDuration
        }

        fun setPlayer(player: Player): BottomPlayerView =
            apply {
                this.player = player
                listener = PlayerListener()

                player.addListener(listener)
                timeBar.addListener(listener)
                playButton.setOnClickListener(listener)

                updateUi()
            }

        fun setTitle(title: String) {
            binding.tvBottomPlayerTitle.text = title
        }

        private fun updateUi() {
            if (player.currentTimeline.windowCount > 0) {
                updateTimeline()
            }
            updatePlayPauseButton()
        }

        private fun updateTimeline() {
            player.currentTimeline.getWindow(player.currentMediaItemIndex, window)
            val duration = window.durationMs
            timeBar.setDuration(duration)
            setDuration(duration)
            updateProgress()
        }

        private fun updateProgress() {
            if (!isAttachedToWindow) return

            val position = player.currentPosition
            val buffered = player.bufferedPosition

            timeBar.setPosition(position)
            timeBar.setBufferedPosition(buffered)

            removeCallbacks(progressRunnable)

            if (player.playWhenReady && player.playbackState == Player.STATE_READY) {
                postDelayed(progressRunnable, timeBar.preferredUpdateDelay)
            }
        }

        fun setDuration(duration: Long) {
            durationTextView.text =
                if (duration > 0) {
                    formatTime(duration)
                } else {
                    "--:--"
                }
        }

        private fun updatePlayPauseButton() {
            val resId =
                if (player.playWhenReady && player.playbackState == Player.STATE_READY) {
                    R.drawable.ic_bottom_pause
                } else {
                    R.drawable.ic_bottom_play
                }
            playButton.setImageResource(resId)
        }

        private fun togglePlayPause() {
            if (player.playWhenReady) {
                player.pause()
            } else {
                player.play()
            }
        }

        private fun formatTime(millis: Long): String = Util.getStringForTime(formatBuilder, formatter, millis)

        override fun onDetachedFromWindow() {
            super.onDetachedFromWindow()
            removeCallbacks(progressRunnable)
        }

        private inner class PlayerListener :
            Player.Listener,
            TimeBar.OnScrubListener,
            OnClickListener {
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
                if (events.contains(Player.EVENT_MEDIA_METADATA_CHANGED)) {
                    val title = player.mediaMetadata.title?.toString()
                    if (!title.isNullOrBlank()) {
                        setTitle(title)
                        setDuration(player.duration)
                    }
                }
                if (events.contains(Player.EVENT_TIMELINE_CHANGED)) {
                    updateTimeline()
                }
                if (events.contains(Player.EVENT_PLAYBACK_STATE_CHANGED) ||
                    events.contains(Player.EVENT_IS_PLAYING_CHANGED)
                ) {
                    updatePlayPauseButton()
                    updateProgress()
                }
            }

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
                player.seekTo(position)
                updateProgress()
            }

            override fun onClick(v: View?) {
                if (v === playButton) {
                    togglePlayPause()
                }
            }
        }
    }
