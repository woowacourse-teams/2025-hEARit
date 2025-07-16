package com.onair.hearit.presentation.search

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.onair.hearit.domain.KeywordItem

class KeywordAdapter : ListAdapter<KeywordItem, KeywordViewHolder>(DiffUtil) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): KeywordViewHolder = KeywordViewHolder.create(parent)

    override fun onBindViewHolder(
        holder: KeywordViewHolder,
        position: Int,
    ) {
        holder.bind(getItem(position))
    }

    companion object {
        val DiffUtil =
            object : DiffUtil.ItemCallback<KeywordItem>() {
                override fun areItemsTheSame(
                    oldItem: KeywordItem,
                    newItem: KeywordItem,
                ): Boolean = oldItem.keyword == newItem.keyword

                override fun areContentsTheSame(
                    oldItem: KeywordItem,
                    newItem: KeywordItem,
                ): Boolean = oldItem == newItem
            }
    }
}
