package com.onair.hearit.presentation.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.databinding.ItemRecommendationCategoryBinding
import com.onair.hearit.domain.model.RecommendationCategories
import com.onair.hearit.presentation.HearitClickListener
import com.onair.hearit.presentation.dpToPx
import com.onair.hearit.presentation.home.HorizontalMarginItemDecoration

class RecommendationCategoryViewHolder(
    private val binding: ItemRecommendationCategoryBinding,
    hearitClickListener: HearitClickListener,
    sharedPool: RecyclerView.RecycledViewPool,
) : RecyclerView.ViewHolder(binding.root) {
    private val itemAdapter = CategoryItemAdapter(hearitClickListener, DEFAULT_COLOR)

    init {
        binding.rvCategoryItems.apply {
            setRecycledViewPool(sharedPool)
            adapter = itemAdapter
            addItemDecoration(HorizontalMarginItemDecoration(SIDE_MARGIN.dpToPx(itemView.context)))
        }
    }

    fun bind(
        recommendationCategories: RecommendationCategories,
        clickListener: (Long, String, String) -> Unit,
    ) {
        binding.navigateClickListener = clickListener
        binding.recommendationCategory = recommendationCategories
        itemAdapter.updateColor(recommendationCategories.colorCode)
        itemAdapter.submitList(recommendationCategories.hearits)
    }

    companion object {
        fun create(
            parent: ViewGroup,
            hearitClickListener: HearitClickListener,
            sharedPool: RecyclerView.RecycledViewPool,
        ): RecommendationCategoryViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemRecommendationCategoryBinding.inflate(inflater, parent, false)
            return RecommendationCategoryViewHolder(binding, hearitClickListener, sharedPool)
        }

        private const val DEFAULT_COLOR = "#000000"
        private const val SIDE_MARGIN = 16
    }
}
