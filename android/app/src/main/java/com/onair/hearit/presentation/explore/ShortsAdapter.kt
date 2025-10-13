package com.onair.hearit.presentation.explore

import android.view.ViewGroup
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.onair.hearit.domain.model.ExploreHearit

class ShortsAdapter(
    private val player: ExoPlayer,
    private val shortsClickListener: ShortsClickListener,
) : ListAdapter<ExploreHearit, ShortsViewHolder>((DiffCallback)) {
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
            object : DiffUtil.ItemCallback<ExploreHearit>() {
                override fun areItemsTheSame(
                    oldItem: ExploreHearit,
                    newItem: ExploreHearit,
                ): Boolean = oldItem.id == newItem.id

                override fun areContentsTheSame(
                    oldItem: ExploreHearit,
                    newItem: ExploreHearit,
                ): Boolean = oldItem == newItem
            }
    }
}
