package com.onair.hearit.presentation.home

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.onair.hearit.domain.model.CategoryHearit

class CategoryItemAdapter(
    private val hearitClickListener: HearitClickListener,
    private var color: String,
) : ListAdapter<CategoryHearit, CategoryItemViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): CategoryItemViewHolder = CategoryItemViewHolder.create(parent, hearitClickListener)

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
                ): Boolean = oldItem.title == newItem.title

                override fun areContentsTheSame(
                    oldItem: CategoryHearit,
                    newItem: CategoryHearit,
                ): Boolean = oldItem == newItem
            }
    }
}
