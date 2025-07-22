package com.onair.hearit.presentation.search

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.onair.hearit.domain.Keyword

class KeywordAdapter : ListAdapter<Keyword, KeywordViewHolder>(DiffUtil) {
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
            object : DiffUtil.ItemCallback<Keyword>() {
                override fun areItemsTheSame(
                    oldItem: Keyword,
                    newItem: Keyword,
                ): Boolean = oldItem.keyword == newItem.keyword

                override fun areContentsTheSame(
                    oldItem: Keyword,
                    newItem: Keyword,
                ): Boolean = oldItem == newItem
            }
    }
}
