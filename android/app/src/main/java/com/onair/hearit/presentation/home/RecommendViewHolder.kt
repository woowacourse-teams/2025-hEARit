package com.onair.hearit.presentation.home

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.databinding.ItemRecommendHearitBinding
import com.onair.hearit.domain.model.RecommendHearit

class RecommendViewHolder(
    private val binding: ItemRecommendHearitBinding,
    private val recommendClickListener: RecommendClickListener,
) : RecyclerView.ViewHolder(binding.root) {
    init {
        binding.recommendClickListener = recommendClickListener
    }

    fun bind(item: RecommendHearit) {
        binding.item = item
        val drawable = binding.root.background?.mutate() as? GradientDrawable
        drawable?.setColor(item.categoryColor.toColorInt())
    }

    companion object {
        fun create(
            parent: ViewGroup,
            recommendClickListener: RecommendClickListener,
        ): RecommendViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemRecommendHearitBinding.inflate(inflater, parent, false)
            return RecommendViewHolder(binding, recommendClickListener)
        }
    }
}
