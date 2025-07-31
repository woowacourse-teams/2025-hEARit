package com.onair.hearit.presentation.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.databinding.ItemGroupedCategoryBinding
import com.onair.hearit.domain.model.GroupedCategory

class GroupedCategoryViewHolder(
    private val binding: ItemGroupedCategoryBinding,
    private val hearitClickListener: HearitClickListener,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(groupedCategory: GroupedCategory) {
        val itemAdapter = CategoryItemAdapter(groupedCategory.colorCode, hearitClickListener)
        binding.groupedCategory = groupedCategory
        binding.rvCategoryItems.adapter = itemAdapter
        itemAdapter.submitList(groupedCategory.hearits)
    }

    companion object {
        fun create(
            parent: ViewGroup,
            hearitClickListener: HearitClickListener,
        ): GroupedCategoryViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemGroupedCategoryBinding.inflate(inflater, parent, false)
            return GroupedCategoryViewHolder(binding, hearitClickListener)
        }
    }
}
