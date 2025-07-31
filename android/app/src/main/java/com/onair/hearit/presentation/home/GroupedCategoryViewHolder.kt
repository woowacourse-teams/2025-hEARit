package com.onair.hearit.presentation.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.databinding.ItemGroupedCategoryBinding
import com.onair.hearit.domain.model.GroupedCategory

class GroupedCategoryViewHolder(
    private val binding: ItemGroupedCategoryBinding,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(groupedCategory: GroupedCategory) {
        val itemAdapter = CategoryItemAdapter(groupedCategory.colorCode)
        binding.groupedCategory = groupedCategory
        binding.rvCategoryItems.apply {
            layoutManager = LinearLayoutManager(itemView.context, RecyclerView.HORIZONTAL, false)
            adapter = itemAdapter
        }
        itemAdapter.submitList(groupedCategory.hearits)
    }

    companion object {
        fun create(parent: ViewGroup): GroupedCategoryViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemGroupedCategoryBinding.inflate(inflater, parent, false)
            return GroupedCategoryViewHolder(binding)
        }
    }
}
