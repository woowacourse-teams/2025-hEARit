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
import com.onair.hearit.R
import com.onair.hearit.databinding.ItemExploreBinding
import com.onair.hearit.domain.HearitShortsItem

class ShortsViewHolder(
    parent: ViewGroup,
    private val player: ExoPlayer,
) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_explore, parent, false),
    ) {
    private val binding = ItemExploreBinding.bind(itemView)
    private val handler = Handler(Looper.getMainLooper())
    private var updateRunnable: Runnable? = null

    private lateinit var hearitShortsItem: HearitShortsItem
    private val scriptAdapter = ScriptAdapter()

    @OptIn(UnstableApi::class)
    fun bind(item: HearitShortsItem) {
        this.hearitShortsItem = item

        binding.tvExploreItemContentTitle.text = item.title
        binding.tvExploreItemContentSummary.text = item.summary

        binding.rvExploreItemScript.adapter = scriptAdapter
        scriptAdapter.submitList(item.script)

        binding.layoutExplorePlayer.player = player
        val mediaItem = MediaItem.fromUri(item.audioUri)
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
        val currentSubtitle =
            hearitShortsItem.script.lastOrNull { it.startTime <= currentPositionMs }
        val currentId = currentSubtitle?.id
        val currentIndex = hearitShortsItem.script.indexOfLast { it.startTime <= currentPositionMs }

        scriptAdapter.highlightSubtitle(currentId)

        val layoutManager = binding.rvExploreItemScript.layoutManager as? LinearLayoutManager
        layoutManager?.let {
            val offset = binding.rvExploreItemScript.height / 3
            it.scrollToPositionWithOffset(currentIndex, offset)
        }
    }
}
