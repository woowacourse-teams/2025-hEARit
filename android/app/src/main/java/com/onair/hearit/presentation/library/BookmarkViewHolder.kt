package com.onair.hearit.presentation.library

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.databinding.ItemBookmarkBinding
import com.onair.hearit.domain.BookmarkItem

class BookmarkViewHolder(
    private val binding: ItemBookmarkBinding,
    private val bookmarkListener: BookmarkClickListener,
) : RecyclerView.ViewHolder(binding.root) {
    init {
        binding.bookmarkListener = bookmarkListener
    }

    fun bind(bookmark: BookmarkItem) {
        binding.item = bookmark
    }

    companion object {
        fun create(
            parent: ViewGroup,
            bookmarkListener: BookmarkClickListener,
        ): BookmarkViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemBookmarkBinding.inflate(inflater, parent, false)
            return BookmarkViewHolder(binding, bookmarkListener)
        }
    }
}
