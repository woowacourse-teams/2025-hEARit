package com.onair.hearit.presentation.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.databinding.ItemCategoryBinding
import com.onair.hearit.domain.model.Category
import com.onair.hearit.presentation.CategoryClickListener

class CategoryViewHolder(
    val binding: ItemCategoryBinding,
    val listener: CategoryClickListener,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(category: Category) {
        binding.item = category
        binding.listener = listener
    }

    companion object {
        fun create(
            parent: ViewGroup,
            listener: CategoryClickListener,
        ): CategoryViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemCategoryBinding.inflate(inflater, parent, false)
            return CategoryViewHolder(binding, listener)
        }
    }
}
