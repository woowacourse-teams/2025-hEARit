package com.onair.hearit.presentation.explore

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.databinding.ItemShortsBinding
import com.onair.hearit.domain.HearitShorts

class ShortsViewHolder(
    private val binding: ItemShortsBinding,
    private val player: ExoPlayer,
    private val shortsClickListener: ShortsClickListener,
) : RecyclerView.ViewHolder(
        binding.root,
    ) {
    private val handler = Handler(Looper.getMainLooper())
    private var updateRunnable: Runnable? = null

    private var hearitShorts: HearitShorts? = null
    private val scriptAdapter = ScriptAdapter()

    init {
        binding.shortsClickListener = shortsClickListener
    }

    @OptIn(UnstableApi::class)
    fun bind(item: HearitShorts) {
        this.hearitShorts = item

        binding.hearitItem = item
        binding.rvExploreItemScript.adapter = scriptAdapter
        scriptAdapter.submitList(item.script)

        binding.layoutExplorePlayer.player = player

        val mediaItem = MediaItem.fromUri(item.audioUrl)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.playWhenReady = true

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
        val item = hearitShorts ?: return

        val currentSubtitle = item.script.lastOrNull { it.start <= currentPositionMs }
        val currentId = currentSubtitle?.id
        val currentIndex = item.script.indexOfLast { it.start <= currentPositionMs }

        scriptAdapter.highlightSubtitle(currentId)

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
