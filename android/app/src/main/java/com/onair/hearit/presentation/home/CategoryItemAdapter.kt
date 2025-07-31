package com.onair.hearit.presentation.home

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.onair.hearit.domain.model.RecommendCategoryHearit

class CategoryItemAdapter : ListAdapter<RecommendCategoryHearit, CategoryItemViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): CategoryItemViewHolder = CategoryItemViewHolder.create(parent)

    override fun onBindViewHolder(
        holder: CategoryItemViewHolder,
        position: Int,
    ) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DiffCallback =
            object : DiffUtil.ItemCallback<RecommendCategoryHearit>() {
                override fun areItemsTheSame(
                    oldItem: RecommendCategoryHearit,
                    newItem: RecommendCategoryHearit,
                ): Boolean = oldItem.title == newItem.title

                override fun areContentsTheSame(
                    oldItem: RecommendCategoryHearit,
                    newItem: RecommendCategoryHearit,
                ): Boolean = oldItem == newItem
            }
    }
}
