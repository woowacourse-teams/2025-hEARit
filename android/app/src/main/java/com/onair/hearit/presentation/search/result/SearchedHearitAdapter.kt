package com.onair.hearit.presentation.search.result

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.onair.hearit.domain.model.SearchedHearit

class SearchedHearitAdapter(
    private val searchResultClickListener: SearchResultClickListener,
) : ListAdapter<SearchedHearit, SearchedHearitViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): SearchedHearitViewHolder = SearchedHearitViewHolder.create(parent, searchResultClickListener)

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
