package com.onair.hearit.presentation.search.result

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.onair.hearit.domain.model.Keyword

class SearchKeywordAdapter : ListAdapter<Keyword, SearchKeywordViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): SearchKeywordViewHolder = SearchKeywordViewHolder.create(parent)

    override fun onBindViewHolder(
        holder: SearchKeywordViewHolder,
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
