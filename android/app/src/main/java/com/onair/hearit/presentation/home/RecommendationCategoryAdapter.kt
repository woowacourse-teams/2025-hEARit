package com.onair.hearit.presentation.home

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.onair.hearit.domain.model.RecommendationCategories
import com.onair.hearit.presentation.HearitClickListener

class RecommendationCategoryAdapter(
    private val hearitClickListener: HearitClickListener,
    private val navigateClickListener: (Long, String, String) -> Unit,
) : ListAdapter<RecommendationCategories, RecommendationCategoryViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecommendationCategoryViewHolder = RecommendationCategoryViewHolder.create(parent, hearitClickListener)

    override fun onBindViewHolder(
        holder: RecommendationCategoryViewHolder,
        position: Int,
    ) {
        holder.bind(getItem(position), navigateClickListener)
    }

    companion object {
        private val DiffCallback =
            object : DiffUtil.ItemCallback<RecommendationCategories>() {
                override fun areItemsTheSame(
                    oldItem: RecommendationCategories,
                    newItem: RecommendationCategories,
                ): Boolean = oldItem.categoryId == newItem.categoryId

                override fun areContentsTheSame(
                    oldItem: RecommendationCategories,
                    newItem: RecommendationCategories,
                ): Boolean = oldItem == newItem
            }
    }
}
