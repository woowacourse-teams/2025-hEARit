package com.onair.hearit.presentation.search.recent

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.onair.hearit.domain.model.RecentKeyword

class RecentKeywordAdapter(
    private val listener: KeywordClickListener,
) : ListAdapter<RecentKeyword, RecentKeywordViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecentKeywordViewHolder = RecentKeywordViewHolder.create(parent, listener)

    override fun onBindViewHolder(
        holder: RecentKeywordViewHolder,
        position: Int,
    ) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DiffCallback =
            object : DiffUtil.ItemCallback<RecentKeyword>() {
                override fun areItemsTheSame(
                    oldItem: RecentKeyword,
                    newItem: RecentKeyword,
                ): Boolean = oldItem.id == newItem.id

                override fun areContentsTheSame(
                    oldItem: RecentKeyword,
                    newItem: RecentKeyword,
                ): Boolean = oldItem == newItem
            }
    }
}
