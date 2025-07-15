package com.onair.hearit

import android.view.ViewGroup
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

class ShortsAdapter(
    private val player: ExoPlayer,
) : ListAdapter<HearitShortsItem, ShortsViewHolder>((ExploreShortsDiffUtil)) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ShortsViewHolder = ShortsViewHolder(parent, player)

    override fun onBindViewHolder(
        holder: ShortsViewHolder,
        position: Int,
    ) {
        holder.bind(getItem(position))
    }

    override fun getItemCount(): Int = currentList.size

    object ExploreShortsDiffUtil : DiffUtil.ItemCallback<HearitShortsItem>() {
        override fun areItemsTheSame(
            oldItem: HearitShortsItem,
            newItem: HearitShortsItem,
        ): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: HearitShortsItem,
            newItem: HearitShortsItem,
        ): Boolean = oldItem == newItem
    }
}
