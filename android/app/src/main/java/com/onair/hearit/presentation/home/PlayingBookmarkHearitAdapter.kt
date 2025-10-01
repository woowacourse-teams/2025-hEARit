package com.onair.hearit.presentation.home

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.onair.hearit.domain.model.PlayingBookmarkHearit
import com.onair.hearit.presentation.HearitClickListener

class PlayingBookmarkHearitAdapter(
    private val hearitClickListener: HearitClickListener,
) : ListAdapter<PlayingBookmarkHearit, PlayingBookmarkHearitViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): PlayingBookmarkHearitViewHolder = PlayingBookmarkHearitViewHolder.create(parent, hearitClickListener)

    override fun onBindViewHolder(
        holder: PlayingBookmarkHearitViewHolder,
        position: Int,
    ) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DiffCallback =
            object : DiffUtil.ItemCallback<PlayingBookmarkHearit>() {
                override fun areItemsTheSame(
                    oldItem: PlayingBookmarkHearit,
                    newItem: PlayingBookmarkHearit,
                ): Boolean = oldItem.id == newItem.id

                override fun areContentsTheSame(
                    oldItem: PlayingBookmarkHearit,
                    newItem: PlayingBookmarkHearit,
                ): Boolean = oldItem == newItem
            }
    }
}
