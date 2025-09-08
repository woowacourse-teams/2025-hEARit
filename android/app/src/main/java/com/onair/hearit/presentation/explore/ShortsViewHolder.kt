package com.onair.hearit.presentation.explore

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.databinding.ItemShortsBinding
import com.onair.hearit.domain.model.ShortsHearit

class ShortsViewHolder(
    private val binding: ItemShortsBinding,
    private val player: ExoPlayer,
    private val listener: ShortsClickListener,
) : RecyclerView.ViewHolder(binding.root) {
    private val scriptAdapter = ExploreScriptAdapter()
    private var rotateAnimator: ObjectAnimator? = null
    private var item: ShortsHearit? = null

    init {
        binding.shortsClickListener = listener
    }

    @OptIn(UnstableApi::class)
    fun bind(item: ShortsHearit) {
        this.item = item
        binding.hearitItem = item

        binding.rvExploreItemScript.adapter = scriptAdapter
        scriptAdapter.submitList(item.script)

        binding.layoutExplorePlayer.player = player

//        binding.btnExploreItemBookmark.apply {
//            isSelected = item.isBookmarked
//            setOnClickListener {
//                listener.onClickBookmark(item.id) { id ->
//                    if (id != -1L) isSelected = !isSelected
//                }
//            }
//        }

        startLpRotation()
    }

    fun highlightScriptLine(positionMs: Long) {
        val item = item ?: return
        val index = item.script.indexOfLast { script -> script.start <= positionMs }
        val id = item.script.getOrNull(index)?.id

        scriptAdapter.highlightSubtitle(id)
        (binding.rvExploreItemScript.layoutManager as? LinearLayoutManager)
            ?.scrollToPositionWithOffset(index, binding.rvExploreItemScript.height / 3)
    }

    private fun startLpRotation() {
        rotateAnimator?.cancel()
        rotateAnimator =
            ObjectAnimator.ofFloat(binding.imgExploreLp, View.ROTATION, 0f, 360f).apply {
                duration = 3000L
                repeatCount = ValueAnimator.INFINITE
                interpolator = LinearInterpolator()
                start()
            }
    }

    fun onRecycled() {
        rotateAnimator?.cancel()
        rotateAnimator = null
        binding.rvExploreItemScript.adapter = null
        item = null
    }

    companion object {
        fun create(
            parent: ViewGroup,
            player: ExoPlayer,
            listener: ShortsClickListener,
        ): ShortsViewHolder {
            val binding =
                ItemShortsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ShortsViewHolder(binding, player, listener)
        }
    }
}
