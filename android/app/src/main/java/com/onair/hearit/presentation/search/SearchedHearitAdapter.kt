package com.onair.hearit.presentation.search

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.onair.hearit.domain.SearchedItem

class SearchedHearitAdapter : ListAdapter<SearchedItem, SearchedHearitViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): SearchedHearitViewHolder = SearchedHearitViewHolder.create(parent)

    override fun onBindViewHolder(
        holder: SearchedHearitViewHolder,
        position: Int,
    ) {
        val item: SearchedItem = getItem(position)
        holder.bind(item)
    }

    companion object {
        private val DiffCallback =
            object : DiffUtil.ItemCallback<SearchedItem>() {
                override fun areItemsTheSame(
                    oldItem: SearchedItem,
                    newItem: SearchedItem,
                ): Boolean = oldItem.id == newItem.id

                override fun areContentsTheSame(
                    oldItem: SearchedItem,
                    newItem: SearchedItem,
                ): Boolean = oldItem == newItem
            }
    }
}
