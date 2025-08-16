package com.onair.hearit.presentation.main.playlist

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.onair.hearit.domain.model.Bookmark

class PlaylistAdapter : ListAdapter<Bookmark, PlaylistViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): PlaylistViewHolder = PlaylistViewHolder.create(parent)

    override fun onBindViewHolder(
        holder: PlaylistViewHolder,
        position: Int,
    ) {
        val item: Bookmark = getItem(position)
        holder.bind(item)
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
