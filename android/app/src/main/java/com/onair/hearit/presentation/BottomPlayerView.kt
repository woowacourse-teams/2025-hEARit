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
        private val progressRunnable = Runnable { updateProgress() }

        init {
            setupScrubListener()
            binding.exoPlay.setOnClickListener { togglePlayPause() }
        }

        fun setPlayer(
            newPlayer: Player,
            startPosition: Long = 0L,
        ): BottomPlayerView =
            apply {
                detachPlayer()
                player = newPlayer
                listener = PlayerListener().also { newPlayer.addListener(it) }

                newPlayer.seekTo(startPosition)
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
                        player?.seekTo(position)
                        updateProgress()

                        (context as? PlaybackPositionSaver)?.savePlaybackPosition()
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
                binding.exoProgress.setDuration(0)
                return
            }

            timeline.getWindow(player.currentMediaItemIndex, window)
            val duration = window.durationMs

            binding.exoProgress.setDuration(duration)
            updateProgress()
        }

        private fun updateProgress() {
            if (!isAttachedToWindow) return
            val current = player ?: return

            binding.exoProgress.setPosition(current.currentPosition)
            binding.exoProgress.setBufferedPosition(current.bufferedPosition)

            removeCallbacks(progressRunnable)
            if (current.playWhenReady && current.playbackState == Player.STATE_READY) {
                postDelayed(progressRunnable, binding.exoProgress.preferredUpdateDelay)
            }
        }

        private fun updatePlayPauseButton(current: Player) {
            val isPlaying = current.playWhenReady && current.playbackState == Player.STATE_READY
            val icon = if (isPlaying) R.drawable.ic_bottom_pause else R.drawable.ic_bottom_play
            binding.exoPlay.setImageResource(icon)
        }

        private fun togglePlayPause() {
            player?.let {
                if (it.playWhenReady) {
                    it.pause()
                    (context as? PlaybackPositionSaver)?.savePlaybackPosition()
                } else {
                    it.play()
                }
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
                    player.mediaMetadata.title?.toString()?.takeIf { it.isNotBlank() }?.let {
                        setTitle(it)
                    }
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
    }
