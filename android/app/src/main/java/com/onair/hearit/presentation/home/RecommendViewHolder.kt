package com.onair.hearit.presentation.home

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.databinding.ItemSlideBinding
import com.onair.hearit.domain.model.SlideItem

class RecommendViewHolder(
    private val binding: ItemSlideBinding,
    private val recommendClickListener: RecommendClickListener,
) : RecyclerView.ViewHolder(binding.root) {
    init {
//        binding.recommendClickListener = recommendClickListener
    }

    fun bind(item: SlideItem) {
        val drawable = binding.root.background?.mutate() as? GradientDrawable
        drawable?.setColor(item.backgroundColor)
        binding.tvSlideCategory.text = item.category
        binding.tvSlideTitle.text = item.title
    }

    companion object {
        fun create(
            parent: ViewGroup,
            recommendClickListener: RecommendClickListener,
        ): RecommendViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemSlideBinding.inflate(inflater, parent, false)
            return RecommendViewHolder(binding, recommendClickListener)
        }
    }
}
