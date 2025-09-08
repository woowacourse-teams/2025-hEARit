package com.onair.hearit.presentation.search.recent.searchResult

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.onair.hearit.domain.model.SearchedHearit
import com.onair.hearit.presentation.HearitClickListener

class SearchedHearitAdapter(
    private val clickListener: HearitClickListener,
) : ListAdapter<SearchedHearit, SearchedHearitViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): SearchedHearitViewHolder = SearchedHearitViewHolder.create(parent, clickListener)

    override fun onBindViewHolder(
        holder: SearchedHearitViewHolder,
        position: Int,
    ) = holder.bind(getItem(position))

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
