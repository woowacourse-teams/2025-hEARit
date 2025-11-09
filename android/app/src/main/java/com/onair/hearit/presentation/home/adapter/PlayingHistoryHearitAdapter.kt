package com.onair.hearit.presentation.home.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.onair.hearit.analytics.HearitSource
import com.onair.hearit.domain.model.PlayingHistoryHearit
import com.onair.hearit.presentation.HearitClickListener

class PlayingHistoryHearitAdapter(
    private val hearitClickListener: HearitClickListener,
) : ListAdapter<PlayingHistoryHearit, PlayingHistoryHearitViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): PlayingHistoryHearitViewHolder =
        PlayingHistoryHearitViewHolder.create(
            parent,
            HearitSource.PLAYING_HISTORY,
            hearitClickListener,
        )

    override fun onBindViewHolder(
        holder: PlayingHistoryHearitViewHolder,
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
