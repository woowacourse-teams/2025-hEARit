package com.onair.hearit.presentation.search

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.onair.hearit.domain.model.Keyword

class KeywordAdapter(
    private val listener: KeywordClickListener,
) : ListAdapter<Keyword, KeywordViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): KeywordViewHolder = KeywordViewHolder.create(parent, listener)

    override fun onBindViewHolder(
        holder: KeywordViewHolder,
        position: Int,
    ) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DiffCallback =
            object : DiffUtil.ItemCallback<Keyword>() {
                override fun areItemsTheSame(
                    oldItem: Keyword,
                    newItem: Keyword,
                ): Boolean = oldItem.id == newItem.id

                override fun areContentsTheSame(
                    oldItem: Keyword,
                    newItem: Keyword,
                ): Boolean = oldItem == newItem
            }
    }
}
