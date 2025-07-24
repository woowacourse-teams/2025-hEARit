package com.onair.hearit.presentation.search

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.onair.hearit.domain.model.SearchedHearit
import com.onair.hearit.presentation.explore.ShortsClickListener

class SearchedHearitAdapter(
    private val shortsClickListener: ShortsClickListener,
) : ListAdapter<SearchedHearit, SearchedHearitViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): SearchedHearitViewHolder = SearchedHearitViewHolder.create(parent, shortsClickListener)

    override fun onBindViewHolder(
        holder: SearchedHearitViewHolder,
        position: Int,
    ) {
        val item: SearchedHearit = getItem(position)
        holder.bind(item)
    }

    companion object {
        private val DiffCallback =
            object : DiffUtil.ItemCallback<SearchedHearit>() {
                override fun areItemsTheSame(
                    oldItem: SearchedHearit,
                    newItem: SearchedHearit,
                ): Boolean = oldItem.id == newItem.id

                override fun areContentsTheSame(
                    oldItem: SearchedHearit,
                    newItem: SearchedHearit,
                ): Boolean = oldItem == newItem
            }
    }
}
