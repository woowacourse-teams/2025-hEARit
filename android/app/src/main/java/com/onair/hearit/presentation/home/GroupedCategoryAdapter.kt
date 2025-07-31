package com.onair.hearit.presentation.home

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.onair.hearit.domain.model.GroupedCategory

class GroupedCategoryAdapter : ListAdapter<GroupedCategory, GroupedCategoryViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): GroupedCategoryViewHolder = GroupedCategoryViewHolder.create(parent)

    override fun onBindViewHolder(
        holder: GroupedCategoryViewHolder,
        position: Int,
    ) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DiffCallback =
            object : DiffUtil.ItemCallback<GroupedCategory>() {
                override fun areItemsTheSame(
                    oldItem: GroupedCategory,
                    newItem: GroupedCategory,
                ): Boolean = oldItem.categoryName == newItem.categoryName

                override fun areContentsTheSame(
                    oldItem: GroupedCategory,
                    newItem: GroupedCategory,
                ): Boolean = oldItem == newItem
            }
    }
}
