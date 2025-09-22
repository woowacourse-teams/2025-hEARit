package com.onair.hearit.presentation.detail

import android.content.Context
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.PopupMenu
import androidx.core.view.get
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.ui.TimeBar
import com.onair.hearit.R
import com.onair.hearit.databinding.LayoutControllerBinding
import java.util.Formatter
import java.util.Locale
import kotlin.math.abs

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
        private val speedOptions = floatArrayOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f)
        private var playSpeedIndex = defaultSpeedIndex()

        private val progressRunnable = Runnable { updateProgress() }

        init {
            initView()
        }

        private fun initView() {
            binding = LayoutControllerBinding.inflate(LayoutInflater.from(context), this, true)

            binding.exoPosition.text = DEFAULT_POSITION_TEXT
            binding.exoDuration.text = DEFAULT_DURATION_TEXT
        }

        fun setPlayer(player: Player) =
            apply {
                this.player = player
                setupListeners()
                syncSpeedIndexWithPlayer()
                updateUI()
            }

        private fun setupListeners() {
            val listener = ComponentListener()

            player.addListener(listener)
            binding.exoProgress.addListener(listener)

            binding.exoPlay.setOnClickListener { togglePlayPause() }
            binding.exoRew.setOnClickListener { player.seekBack() }
            binding.exoFfwd.setOnClickListener { player.seekForward() }
            binding.playSpeed.setOnClickListener { showSpeedMenu() }
        }

        fun setBookmarkSelected(isSelected: Boolean) {
            binding.btnDetailBookmark.isSelected = isSelected
        }

        fun setOnBookmarkClickListener(listener: () -> Unit) {
            binding.btnDetailBookmark.setOnClickListener { it ->
                if (!binding.btnDetailBookmark.isSelected) {
                    it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                }
                listener()
            }
        }

        private fun togglePlayPause() {
            if (player.playWhenReady) {
                player.pause()
            } else {
                player.play()
            }
            updatePlayPauseButton()
        }

        private fun syncSpeedIndexWithPlayer() {
            val currentSpeed = player.playbackParameters.speed
            playSpeedIndex =
                speedOptions
                    .indexOfFirst { floatsAreEqualWithinTolerance(it, currentSpeed) }
                    .takeIf { it >= 0 }
                    ?: defaultSpeedIndex()
            updateSpeedLabel()
        }

        private fun applySpeed(speed: Float) {
            player.playbackParameters = player.playbackParameters.withSpeed(speed)
            playSpeedIndex = speedOptions
                .indexOfFirst { floatsAreEqualWithinTolerance(it, speed) }
                .takeIf { it >= 0 }
                ?: defaultSpeedIndex()
            updateSpeedLabel()
        }

        private fun showSpeedMenu() {
            val popup = PopupMenu(context, binding.playSpeed)

            // 배속 메뉴 구성
            speedOptions.forEachIndexed { index, speed ->
                popup.menu.add(0, index, index, "${speed}x")
            }

            // 현재 속도 체크
            val currentSpeed = player.playbackParameters.speed
            val checkedIndex =
                speedOptions
                    .indexOfFirst { floatsAreEqualWithinTolerance(it, currentSpeed) }
                    .takeIf { it >= 0 } ?: defaultSpeedIndex()

            popup.menu[checkedIndex].isChecked = true
            popup.menu.setGroupCheckable(0, true, true)

            popup.setOnMenuItemClickListener { item ->
                val index = item.itemId
                if (index in speedOptions.indices) {
                    applySpeed(speedOptions[index])
                    true
                } else {
                    false
                }
            }
            popup.show()
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
            val winDuration = if (window.durationMs == C.TIME_UNSET) 0L else window.durationMs
            binding.exoProgress.setDuration(winDuration)

            updateProgress()
        }

        private fun calculateUpdateIntervalMs(): Long {
            val speed = player.playbackParameters.speed.coerceAtLeast(0.1f)
            val interval = (PROGRESS_UPDATE_BASE_MS / speed)
            return interval.coerceIn(PROGRESS_UPDATE_MIN_MS, PROGRESS_UPDATE_MAX_MS).toLong()
        }

        private fun updateProgress() {
            if (!isAttachedToWindow) return

            val pos = player.currentPosition
            val rawDuration = player.duration
            val duration = if (rawDuration == C.TIME_UNSET) 0L else rawDuration
            val buf = player.bufferedPosition

            binding.exoPosition.text = Util.getStringForTime(formatBuilder, formatter, pos)
            val remaining = (duration - pos).coerceAtLeast(0L)
            val remainingStr = Util.getStringForTime(formatBuilder, formatter, remaining)
            binding.exoDuration.text =
                context.getString(R.string.player_detail_player_duration_remaining, remainingStr)

            binding.exoProgress.setPosition(pos)
            binding.exoProgress.setBufferedPosition(buf)

            removeCallbacks(progressRunnable)
            if (player.playWhenReady && player.playbackState == Player.STATE_READY) {
                postDelayed(progressRunnable, calculateUpdateIntervalMs())
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
            val speedStr = String.format(Locale.getDefault(), "%.2f", speed).trimEnd('0').trimEnd('.')
            binding.playSpeed.text =
                context.getString(R.string.player_detail_player_speed_label, speedStr)
        }

        private fun defaultSpeedIndex(): Int = speedOptions.indexOfFirst { floatsAreEqualWithinTolerance(it, 1f) }.takeIf { it >= 0 } ?: 0

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
                if (events.contains(Player.EVENT_PLAYBACK_PARAMETERS_CHANGED)) {
                    syncSpeedIndexWithPlayer()
                    removeCallbacks(progressRunnable)
                    updateProgress()
                }

                if (events.containsAny(
                        Player.EVENT_MEDIA_ITEM_TRANSITION,
                        Player.EVENT_TIMELINE_CHANGED,
                        Player.EVENT_PLAYBACK_STATE_CHANGED,
                        Player.EVENT_IS_PLAYING_CHANGED,
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
            }
        }

        companion object {
            private const val DEFAULT_POSITION_TEXT = "00:00"
            private const val DEFAULT_DURATION_TEXT = "-00:00"
            private const val PROGRESS_UPDATE_BASE_MS = 1000f
            private const val PROGRESS_UPDATE_MIN_MS = 100f
            private const val PROGRESS_UPDATE_MAX_MS = 2000f
            private const val FLOAT_EQUALITY_TOLERANCE = 0.001f

            private fun floatsAreEqualWithinTolerance(
                first: Float,
                second: Float,
            ): Boolean = abs(first - second) < FLOAT_EQUALITY_TOLERANCE
        }
    }
