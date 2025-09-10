package com.onair.hearit.presentation.explore

import android.view.ViewGroup
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.onair.hearit.domain.model.ShortsHearit

class ShortsAdapter(
    private val player: ExoPlayer,
    private val shortsClickListener: ShortsClickListener,
) : ListAdapter<ShortsHearit, ShortsViewHolder>((DiffCallback)) {
    private var currentViewHolder: ShortsViewHolder? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ShortsViewHolder = ShortsViewHolder.create(parent, player, shortsClickListener)

    override fun onBindViewHolder(
        holder: ShortsViewHolder,
        position: Int,
    ) {
        holder.bind(getItem(position))
    }

    override fun getItemCount(): Int = currentList.size

    override fun onViewRecycled(holder: ShortsViewHolder) {
        holder.onRecycled()
        super.onViewRecycled(holder)
    }

    companion object {
        private val DiffCallback =
            object : DiffUtil.ItemCallback<ShortsHearit>() {
                override fun areItemsTheSame(
                    oldItem: ShortsHearit,
                    newItem: ShortsHearit,
                ): Boolean = oldItem.id == newItem.id

                override fun areContentsTheSame(
                    oldItem: ShortsHearit,
                    newItem: ShortsHearit,
                ): Boolean = oldItem == newItem
            }
    }
}
