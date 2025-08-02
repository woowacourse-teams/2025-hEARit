package com.onair.hearit.presentation.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.databinding.ItemGroupedCategoryBinding
import com.onair.hearit.domain.model.GroupedCategory
import com.onair.hearit.presentation.dpToPx

class GroupedCategoryViewHolder(
    private val binding: ItemGroupedCategoryBinding,
    hearitClickListener: HearitClickListener,
) : RecyclerView.ViewHolder(binding.root) {
    private val itemAdapter = CategoryItemAdapter(hearitClickListener, DEFAULT_COLOR)

    init {
        binding.rvCategoryItems.apply {
            adapter = itemAdapter
            addItemDecoration(HorizontalMarginItemDecoration(SIDE_MARGIN.dpToPx(itemView.context)))
        }
    }

    fun bind(
        groupedCategory: GroupedCategory,
        clickListener: (Long, String) -> Unit,
    ) {
        binding.navigateClickListener = clickListener
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
        private const val SIDE_MARGIN = 20
    }
}
