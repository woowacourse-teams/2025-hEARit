package com.onair.hearit.presentation.library

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.onair.hearit.domain.model.Bookmark

class BookmarkAdapter(
    private val bookmarkListener: BookmarkClickListener,
) : ListAdapter<Bookmark, BookmarkViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): BookmarkViewHolder = BookmarkViewHolder.create(parent, bookmarkListener)

    override fun onBindViewHolder(
        holder: BookmarkViewHolder,
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
