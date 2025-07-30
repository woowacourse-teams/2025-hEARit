package com.onair.hearit.presentation.home

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.onair.hearit.domain.model.SlideItem

class RecommendHearitAdapter(
    private val recommendClickListener: RecommendClickListener,
) : ListAdapter<SlideItem, RecommendViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecommendViewHolder = RecommendViewHolder.create(parent, recommendClickListener)

    override fun onBindViewHolder(
        holder: RecommendViewHolder,
        position: Int,
    ) {
        holder.bind(getItem(position))
    }

    companion object {
        val DiffCallback =
            object : DiffUtil.ItemCallback<SlideItem>() {
                override fun areItemsTheSame(
                    oldItem: SlideItem,
                    newItem: SlideItem,
                ): Boolean = oldItem.id == newItem.id

                override fun areContentsTheSame(
                    oldItem: SlideItem,
                    newItem: SlideItem,
                ): Boolean = oldItem == newItem
            }
    }
}
