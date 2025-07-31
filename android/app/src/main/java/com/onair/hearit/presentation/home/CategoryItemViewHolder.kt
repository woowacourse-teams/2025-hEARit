package com.onair.hearit.presentation.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.databinding.ItemRecommendCategoryBinding
import com.onair.hearit.domain.model.RecommendCategoryHearit

class CategoryItemViewHolder(
    private val binding: ItemRecommendCategoryBinding,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: RecommendCategoryHearit) {
        binding.recommendCategoryHearit = item
    }

    companion object {
        fun create(parent: ViewGroup): CategoryItemViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemRecommendCategoryBinding.inflate(inflater, parent, false)
            return CategoryItemViewHolder(binding)
        }
    }
}
