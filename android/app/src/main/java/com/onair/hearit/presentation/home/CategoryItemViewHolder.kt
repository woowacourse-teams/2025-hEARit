package com.onair.hearit.presentation.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.databinding.ItemCategoryHearitBinding
import com.onair.hearit.domain.model.CategoryHearit

class CategoryItemViewHolder(
    private val color: String,
    private val binding: ItemCategoryHearitBinding,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: CategoryHearit) {
        binding.recommendCategoryHearit = item
        binding.color = color
    }

    companion object {
        fun create(
            parent: ViewGroup,
            color: String,
        ): CategoryItemViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemCategoryHearitBinding.inflate(inflater, parent, false)
            return CategoryItemViewHolder(color, binding)
        }
    }
}
