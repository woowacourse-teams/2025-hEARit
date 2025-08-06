package com.onair.hearit.presentation.explore

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.databinding.ItemShortsBinding
import com.onair.hearit.domain.model.ShortsHearit

class ShortsViewHolder(
    private val binding: ItemShortsBinding,
    private val player: ExoPlayer,
    private val shortsClickListener: ShortsClickListener,
) : RecyclerView.ViewHolder(
        binding.root,
    ) {
    private val handler = Handler(Looper.getMainLooper())
    private var updateRunnable: Runnable? = null

    private var shortsHearit: ShortsHearit? = null
    private val exploreScriptAdapter = ExploreScriptAdapter()

    private var rotateAnimator: ObjectAnimator? = null

    init {
        binding.shortsClickListener = shortsClickListener
    }

    @OptIn(UnstableApi::class)
    fun bind(item: ShortsHearit) {
        this.shortsHearit = item
        binding.hearitItem = item
        binding.rvExploreItemScript.adapter = exploreScriptAdapter
        exploreScriptAdapter.submitList(item.script)

        binding.layoutExplorePlayer.player = player

        val mediaItem = MediaItem.fromUri(item.audioUrl)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.playWhenReady = true

        val rotate =
            ObjectAnimator.ofFloat(binding.imgExploreLp, View.ROTATION, 0f, 360f).apply {
                duration = 3000L
                repeatCount = ValueAnimator.INFINITE
                interpolator = LinearInterpolator()
            }
        rotateAnimator?.cancel()
        rotateAnimator = rotate
        rotate.start()

        binding.btnExploreItemBookmark.setOnClickListener {
            binding.btnExploreItemBookmark.isSelected = !binding.btnExploreItemBookmark.isSelected
            shortsClickListener.onClickBookmark(item.id)
        }

        binding.btnExploreItemBookmark.isSelected = item.isBookmarked

        startSubtitleSync()
    }

    private fun startSubtitleSync() {
        stopSubtitleSync()

        updateRunnable =
            object : Runnable {
                override fun run() {
                    updateSubtitleHighlight(player.currentPosition)
                    handler.postDelayed(this, 500L)
                }
            }
        handler.post(updateRunnable!!)
    }

    private fun stopSubtitleSync() {
        updateRunnable?.let { handler.removeCallbacks(it) }
        updateRunnable = null
    }

    private fun updateSubtitleHighlight(currentPositionMs: Long) {
        val item = shortsHearit ?: return

        val currentSubtitle = item.script.lastOrNull { it.start <= currentPositionMs }
        val currentId = currentSubtitle?.id
        val currentIndex = item.script.indexOfLast { it.start <= currentPositionMs }

        exploreScriptAdapter.highlightSubtitle(currentId)

        val layoutManager = binding.rvExploreItemScript.layoutManager as? LinearLayoutManager
        layoutManager?.let {
            val offset = binding.rvExploreItemScript.height / 3
            it.scrollToPositionWithOffset(currentIndex, offset)
        }
    }

    companion object {
        fun create(
            parent: ViewGroup,
            player: ExoPlayer,
            shortsClickListener: ShortsClickListener,
        ): ShortsViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemShortsBinding.inflate(inflater, parent, false)
            return ShortsViewHolder(binding, player, shortsClickListener)
        }
    }
}
