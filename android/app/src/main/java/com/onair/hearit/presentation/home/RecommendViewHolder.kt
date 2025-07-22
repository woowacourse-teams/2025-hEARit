package com.onair.hearit.presentation.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.databinding.ItemRecommendHearitBinding
import com.onair.hearit.domain.RecommendHearit

class RecommendViewHolder(
    private val binding: ItemRecommendHearitBinding,
    private val recommendClickListener: RecommendClickListener,
) : RecyclerView.ViewHolder(binding.root) {
    init {
        binding.recommendClickListener = recommendClickListener
    }

    fun bind(item: RecommendHearit) {
        binding.item = item
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
