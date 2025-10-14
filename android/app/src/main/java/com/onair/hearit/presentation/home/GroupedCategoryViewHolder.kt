package com.onair.hearit.presentation.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.databinding.ItemRecommendationCategoryBinding
import com.onair.hearit.domain.model.RecommendationCategories
import com.onair.hearit.presentation.HearitClickListener
import com.onair.hearit.presentation.dpToPx

class GroupedCategoryViewHolder(
    private val binding: ItemRecommendationCategoryBinding,
    hearitClickListener: HearitClickListener,
) : RecyclerView.ViewHolder(binding.root) {
    private val itemAdapter = CategoryItemAdapter(hearitClickListener, DEFAULT_COLOR)
    private var decorationAdded = false

    init {
        binding.rvCategoryItems.apply {
            adapter = itemAdapter
            if (!decorationAdded) {
                addItemDecoration(HorizontalMarginItemDecoration(SIDE_MARGIN.dpToPx(itemView.context)))
                decorationAdded = true
            }
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
        ): GroupedCategoryViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemRecommendationCategoryBinding.inflate(inflater, parent, false)
            return GroupedCategoryViewHolder(binding, hearitClickListener)
        }

        private const val DEFAULT_COLOR = "#000000"
        private const val SIDE_MARGIN = 16
    }
}
