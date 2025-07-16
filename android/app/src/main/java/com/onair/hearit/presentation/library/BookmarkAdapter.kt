package com.onair.hearit.presentation.library

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.onair.hearit.domain.BookmarkItem

class BookmarkAdapter : ListAdapter<BookmarkItem, BookmarkViewHolder>(DIFF_CALLBACK) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): BookmarkViewHolder = BookmarkViewHolder.create(parent)

    override fun onBindViewHolder(
        holder: BookmarkViewHolder,
        position: Int,
    ) {
        val item: BookmarkItem = getItem(position)
        holder.bind(item)
    }

    companion object {
        private val DIFF_CALLBACK =
            object : DiffUtil.ItemCallback<BookmarkItem>() {
                override fun areItemsTheSame(
                    oldItem: BookmarkItem,
                    newItem: BookmarkItem,
                ): Boolean = oldItem.id == newItem.id

                override fun areContentsTheSame(
                    oldItem: BookmarkItem,
                    newItem: BookmarkItem,
                ): Boolean = oldItem == newItem
            }
    }
}
