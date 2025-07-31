package com.onair.hearit.presentation.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.databinding.ItemCategorySectionBinding
import com.onair.hearit.domain.model.CategorySection

class CategorySectionViewHolder(
    private val binding: ItemCategorySectionBinding,
) : RecyclerView.ViewHolder(binding.root) {
    private val itemAdapter = CategoryItemAdapter()

    fun bind(categorySection: CategorySection) {
        binding.categorySection = categorySection
        binding.rvCategoryItems.apply {
            layoutManager = LinearLayoutManager(itemView.context, RecyclerView.HORIZONTAL, false)
            adapter = itemAdapter
        }
        itemAdapter.submitList(categorySection.items)
    }

    companion object {
        fun create(parent: ViewGroup): CategorySectionViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemCategorySectionBinding.inflate(inflater, parent, false)
            return CategorySectionViewHolder(binding)
        }
    }
}
