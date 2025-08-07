package com.onair.hearit.presentation.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.databinding.ItemRecommendHearitBinding
import com.onair.hearit.domain.model.RecommendHearit

class RecommendViewHolder(
    private val binding: ItemRecommendHearitBinding,
    hearitClickListener: HearitClickListener,
) : RecyclerView.ViewHolder(binding.root) {
    init {
        binding.recommendClickListener = hearitClickListener
    }

    fun bind(item: RecommendHearit) {
        binding.item = item
    }

    companion object {
        fun create(
            parent: ViewGroup,
            hearitClickListener: HearitClickListener,
        ): RecommendViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemRecommendHearitBinding.inflate(inflater, parent, false)
            return RecommendViewHolder(binding, hearitClickListener)
        }
    }
}
