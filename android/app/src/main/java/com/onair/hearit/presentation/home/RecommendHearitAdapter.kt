package com.onair.hearit.presentation.home

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.onair.hearit.domain.model.RecommendHearit

class RecommendHearitAdapter(
    private val hearitClickListener: HearitClickListener,
) : ListAdapter<RecommendHearit, RecommendViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecommendViewHolder = RecommendViewHolder.create(parent, hearitClickListener)

    override fun onBindViewHolder(
        holder: RecommendViewHolder,
        position: Int,
    ) {
        holder.bind(getItem(position))
    }

    companion object {
        val DiffCallback =
            object : DiffUtil.ItemCallback<RecommendHearit>() {
                override fun areItemsTheSame(
                    oldItem: RecommendHearit,
                    newItem: RecommendHearit,
                ): Boolean = oldItem.id == newItem.id

                override fun areContentsTheSame(
                    oldItem: RecommendHearit,
                    newItem: RecommendHearit,
                ): Boolean = oldItem == newItem
            }
    }
}
