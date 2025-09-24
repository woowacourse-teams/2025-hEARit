package com.onair.hearit.presentation.home

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.onair.hearit.domain.model.PlayingHistoryHearit
import com.onair.hearit.presentation.HearitClickListener

class RecentHearitAdapter(
    private val hearitClickListener: HearitClickListener,
) : ListAdapter<PlayingHistoryHearit, RecentHearitViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecentHearitViewHolder = RecentHearitViewHolder.create(parent, hearitClickListener)

    override fun onBindViewHolder(
        holder: RecentHearitViewHolder,
        position: Int,
    ) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DiffCallback =
            object : DiffUtil.ItemCallback<PlayingHistoryHearit>() {
                override fun areItemsTheSame(
                    oldItem: PlayingHistoryHearit,
                    newItem: PlayingHistoryHearit,
                ): Boolean = oldItem.id == newItem.id

                override fun areContentsTheSame(
                    oldItem: PlayingHistoryHearit,
                    newItem: PlayingHistoryHearit,
                ): Boolean = oldItem == newItem
            }
    }
}
