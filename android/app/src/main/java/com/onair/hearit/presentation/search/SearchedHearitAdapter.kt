package com.onair.hearit.presentation.search

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.onair.hearit.domain.SearchedHearitItem

class SearchedHearitAdapter : ListAdapter<SearchedHearitItem, SearchedHearitViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): SearchedHearitViewHolder = SearchedHearitViewHolder.create(parent)

    override fun onBindViewHolder(
        holder: SearchedHearitViewHolder,
        position: Int,
    ) {
        val item: SearchedHearitItem = getItem(position)
        holder.bind(item)
    }

    companion object {
        private val DiffCallback =
            object : DiffUtil.ItemCallback<SearchedHearitItem>() {
                override fun areItemsTheSame(
                    oldItem: SearchedHearitItem,
                    newItem: SearchedHearitItem,
                ): Boolean = oldItem.id == newItem.id

                override fun areContentsTheSame(
                    oldItem: SearchedHearitItem,
                    newItem: SearchedHearitItem,
                ): Boolean = oldItem == newItem
            }
    }
}
