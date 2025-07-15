package com.onair.hearit

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.ui.DefaultTimeBar
import androidx.media3.ui.TimeBar
import com.onair.hearit.databinding.LayoutControllerBinding
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

        private lateinit var timeBar: DefaultTimeBar
        private lateinit var playButton: ImageButton
        private lateinit var rewindButton: ImageButton
        private lateinit var forwardButton: ImageButton
        private lateinit var curPositionView: TextView
        private lateinit var durPositionView: TextView
        private lateinit var playSpeedView: TextView

        private lateinit var listener: CustomComponentListener

        private val formatBuilder = StringBuilder()
        private val formatter = Formatter(formatBuilder, Locale.getDefault())

        private val window = Timeline.Window()

        private var playSpeedPosition = DEFAULT_SPEED_POSITION

        private val speedList = floatArrayOf(0.25f, 0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f)

        private val progressRunnable = Runnable { updateProgress() }

        init {
            initView()
        }

        fun setPlayer(player: Player) =
            apply {
                this.player = player
                listener = CustomComponentListener()

                player.addListener(listener)
                timeBar.addListener(listener)
                playButton.setOnClickListener(listener)
                rewindButton.setOnClickListener(listener)
                forwardButton.setOnClickListener(listener)
                playSpeedView.setOnClickListener(listener)

                updateAllUi()
            }

        private fun initView() {
            binding = LayoutControllerBinding.inflate(LayoutInflater.from(context), this, true)

            timeBar = binding.exoProgress
            playButton = binding.exoPlay
            rewindButton = binding.exoRew
            forwardButton = binding.exoFfwd
            curPositionView = binding.exoPosition
            durPositionView = binding.exoDuration
            playSpeedView = binding.playSpeed
        }

        private fun updateAllUi() {
            updateTimeline()
            updatePlayPauseButton()
            updatePlaySpeed()
        }

        private fun updateTimeline() {
            player.currentTimeline.getWindow(player.currentMediaItemIndex, window)
            timeBar.setDuration(window.durationMs)
            updateProgress()
        }

        private fun updateProgress() {
            if (!isAttachedToWindow) return

            val pos = player.currentPosition
            val buf = player.bufferedPosition

            curPositionView.text = Util.getStringForTime(formatBuilder, formatter, pos)
            durPositionView.text =
                "-${Util.getStringForTime(formatBuilder, formatter, player.duration - pos)}"

            timeBar.setPosition(pos)
            timeBar.setBufferedPosition(buf)

            removeCallbacks(progressRunnable)

            if (player.playWhenReady && player.playbackState == Player.STATE_READY) {
                postDelayed(progressRunnable, timeBar.preferredUpdateDelay)
            }
        }

        private fun updatePlayPauseButton() {
            if (player.playWhenReady && player.playbackState == Player.STATE_READY) {
                playButton.setImageResource(R.drawable.ic_pause)
            } else {
                playButton.setImageResource(R.drawable.ic_play)
            }
        }

        private fun updatePlaySpeed() {
            playSpeedView.text = "${speedList[playSpeedPosition]}x"
        }

        private fun dispatchPlayPause() {
            if (player.playWhenReady) player.pause() else player.play()
        }

        private fun setNextSpeed() {
            playSpeedPosition = (playSpeedPosition + 1) % speedList.size
            player.playbackParameters =
                player.playbackParameters.withSpeed(speedList[playSpeedPosition])
            updatePlaySpeed()
        }

        inner class CustomComponentListener :
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
                curPositionView.text = Util.getStringForTime(formatBuilder, formatter, position)
            }

            override fun onScrubMove(
                timeBar: TimeBar,
                position: Long,
            ) {
                curPositionView.text = Util.getStringForTime(formatBuilder, formatter, position)
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
                when (v) {
                    playButton -> dispatchPlayPause()
                    rewindButton -> player.seekBack()
                    forwardButton -> player.seekForward()
                    playSpeedView -> setNextSpeed()
                }
            }
        }

        companion object {
            const val DEFAULT_SPEED_POSITION = 3
        }
    }
