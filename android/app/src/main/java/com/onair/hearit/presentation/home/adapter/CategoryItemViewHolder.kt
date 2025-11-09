package com.onair.hearit.presentation.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.analytics.HearitSource
import com.onair.hearit.databinding.ItemCategoryHearitBinding
import com.onair.hearit.domain.model.CategoryHearit
import com.onair.hearit.presentation.HearitClickListener

class CategoryItemViewHolder private constructor(
    private val binding: ItemCategoryHearitBinding,
    private val source: HearitSource,
    hearitClickListener: HearitClickListener,
) : RecyclerView.ViewHolder(binding.root) {
    init {
        binding.hearitClickListener = hearitClickListener
    }

    fun bind(
        item: CategoryHearit,
        color: String,
    ) {
        binding.categoryHearit = item
        binding.categoryColor = color
        binding.source = source
    }

    companion object {
        fun create(
            parent: ViewGroup,
            source: HearitSource,
            hearitClickListener: HearitClickListener,
        ): CategoryItemViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemCategoryHearitBinding.inflate(inflater, parent, false)
            return CategoryItemViewHolder(binding, source, hearitClickListener)
        }
    }
}
