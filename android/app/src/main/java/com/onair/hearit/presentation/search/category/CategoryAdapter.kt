package com.onair.hearit.presentation.search.category

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.onair.hearit.domain.model.Category
import com.onair.hearit.presentation.CategoryClickListener

class CategoryAdapter(
    private val listener: CategoryClickListener,
) : ListAdapter<Category, CategoryViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): CategoryViewHolder = CategoryViewHolder.create(parent, listener)

    override fun onBindViewHolder(
        holder: CategoryViewHolder,
        position: Int,
    ) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DiffCallback =
            object : DiffUtil.ItemCallback<Category>() {
                override fun areItemsTheSame(
                    oldItem: Category,
                    newItem: Category,
                ): Boolean = oldItem.id == newItem.id

                override fun areContentsTheSame(
                    oldItem: Category,
                    newItem: Category,
                ): Boolean = oldItem == newItem
            }
    }
}
