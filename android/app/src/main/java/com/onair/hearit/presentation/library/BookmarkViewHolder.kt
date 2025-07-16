package com.onair.hearit.presentation.library

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.databinding.ItemBookmarkBinding
import com.onair.hearit.domain.BookmarkItem

class BookmarkViewHolder(
    val binding: ItemBookmarkBinding,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(bookmark: BookmarkItem) {
        binding.item = bookmark
    }

    companion object {
        fun create(parent: ViewGroup): BookmarkViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemBookmarkBinding.inflate(inflater, parent, false)
            return BookmarkViewHolder(binding)
        }
    }
}
