package com.onair.hearit.presentation.home.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.onair.hearit.analytics.HearitSource
import com.onair.hearit.domain.model.CategoryHearit
import com.onair.hearit.presentation.HearitClickListener

class CategoryItemAdapter(
    private val hearitClickListener: HearitClickListener,
    private var color: String,
) : ListAdapter<CategoryHearit, CategoryItemViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): CategoryItemViewHolder =
        CategoryItemViewHolder.create(
            parent,
            HearitSource.RECOMMENDATION_CATEGORY,
            hearitClickListener,
        )

    override fun onBindViewHolder(
        holder: CategoryItemViewHolder,
        position: Int,
    ) {
        holder.bind(getItem(position), color)
    }

    fun updateColor(newColor: String) {
        color = newColor
    }

    companion object {
        private val DiffCallback =
            object : DiffUtil.ItemCallback<CategoryHearit>() {
                override fun areItemsTheSame(
                    oldItem: CategoryHearit,
                    newItem: CategoryHearit,
                ): Boolean = oldItem.hearitId == newItem.hearitId

                override fun areContentsTheSame(
                    oldItem: CategoryHearit,
                    newItem: CategoryHearit,
                ): Boolean = oldItem == newItem
            }
    }
}
