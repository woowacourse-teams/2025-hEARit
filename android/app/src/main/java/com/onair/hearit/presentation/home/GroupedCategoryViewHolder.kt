package com.onair.hearit.presentation.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.databinding.ItemGroupedCategoryBinding
import com.onair.hearit.domain.model.GroupedCategory

class GroupedCategoryViewHolder(
    private val binding: ItemGroupedCategoryBinding,
    hearitClickListener: HearitClickListener,
) : RecyclerView.ViewHolder(binding.root) {
    private val itemAdapter = CategoryItemAdapter(hearitClickListener, DEFAULT_COLOR)

    init {
        binding.rvCategoryItems.adapter = itemAdapter
    }

    fun bind(groupedCategory: GroupedCategory) {
        binding.groupedCategory = groupedCategory
        itemAdapter.updateColor(groupedCategory.colorCode)
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

        private const val DEFAULT_COLOR = "#000000"
    }
}
