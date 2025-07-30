package com.onair.hearit.presentation.search.recent

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.onair.hearit.domain.model.RecentSearch

class RecentSearchAdapter(
    private val listener: RecentSearchClickListener,
) : ListAdapter<RecentSearch, RecentSearchViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecentSearchViewHolder = RecentSearchViewHolder.create(parent, listener)

    override fun onBindViewHolder(
        holder: RecentSearchViewHolder,
        position: Int,
    ) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DiffCallback =
            object : DiffUtil.ItemCallback<RecentSearch>() {
                override fun areItemsTheSame(
                    oldItem: RecentSearch,
                    newItem: RecentSearch,
                ): Boolean = oldItem.term == newItem.term

                override fun areContentsTheSame(
                    oldItem: RecentSearch,
                    newItem: RecentSearch,
                ): Boolean = oldItem == newItem
            }
    }
}
