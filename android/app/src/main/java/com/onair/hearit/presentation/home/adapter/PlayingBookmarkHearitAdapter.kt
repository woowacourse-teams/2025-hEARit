package com.onair.hearit.presentation.home.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.onair.hearit.analytics.HearitSource
import com.onair.hearit.domain.model.Bookmark
import com.onair.hearit.presentation.HearitClickListener

class PlayingBookmarkHearitAdapter(
    private val hearitClickListener: HearitClickListener,
) : ListAdapter<Bookmark, PlayingBookmarkHearitViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): PlayingBookmarkHearitViewHolder =
        PlayingBookmarkHearitViewHolder.create(
            parent,
            HearitSource.PLAYING_BOOKMARK,
            hearitClickListener,
        )

    override fun onBindViewHolder(
        holder: PlayingBookmarkHearitViewHolder,
        position: Int,
    ) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DiffCallback =
            object : DiffUtil.ItemCallback<Bookmark>() {
                override fun areItemsTheSame(
                    oldItem: Bookmark,
                    newItem: Bookmark,
                ): Boolean = oldItem.bookmarkId == newItem.bookmarkId

                override fun areContentsTheSame(
                    oldItem: Bookmark,
                    newItem: Bookmark,
                ): Boolean = oldItem == newItem
            }
    }
}
