package com.onair.hearit.presentation.home

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.onair.hearit.domain.model.CategorySection

class CategorySectionAdapter : ListAdapter<CategorySection, CategorySectionViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): CategorySectionViewHolder = CategorySectionViewHolder.create(parent)

    override fun onBindViewHolder(
        holder: CategorySectionViewHolder,
        position: Int,
    ) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DiffCallback =
            object : DiffUtil.ItemCallback<CategorySection>() {
                override fun areItemsTheSame(
                    oldItem: CategorySection,
                    newItem: CategorySection,
                ): Boolean = oldItem.categoryName == newItem.categoryName

                override fun areContentsTheSame(
                    oldItem: CategorySection,
                    newItem: CategorySection,
                ): Boolean = oldItem == newItem
            }
    }
}
