package com.onair.hearit.presentation.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.databinding.ItemCategoryHearitBinding
import com.onair.hearit.domain.model.CategoryHearit

class CategoryItemViewHolder private constructor(
    private val binding: ItemCategoryHearitBinding,
    private val hearitClickListener: HearitClickListener,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(
        item: CategoryHearit,
        color: String,
    ) {
        binding.categoryHearit = item
        binding.categoryColor = color
        binding.hearitClickListener = hearitClickListener
    }

    companion object {
        fun create(
            parent: ViewGroup,
            hearitClickListener: HearitClickListener,
        ): CategoryItemViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemCategoryHearitBinding.inflate(inflater, parent, false)
            return CategoryItemViewHolder(binding, hearitClickListener)
        }
    }
}
