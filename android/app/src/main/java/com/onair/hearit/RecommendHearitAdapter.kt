package com.onair.hearit

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.databinding.ItemRecommendHearitBinding

class RecommendHearitAdapter : ListAdapter<RecommendHearitItem, RecommendHearitAdapter.RecommendViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecommendViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemRecommendHearitBinding.inflate(inflater, parent, false)
        return RecommendViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: RecommendViewHolder,
        position: Int,
    ) {
        holder.bind(getItem(position))
    }

    class RecommendViewHolder(
        private val binding: ItemRecommendHearitBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: RecommendHearitItem) {
            binding.item = item
            binding.executePendingBindings()
        }
    }

    companion object {
        val DiffCallback =
            object : DiffUtil.ItemCallback<RecommendHearitItem>() {
                override fun areItemsTheSame(
                    oldItem: RecommendHearitItem,
                    newItem: RecommendHearitItem,
                ): Boolean = oldItem.id == newItem.id

                override fun areContentsTheSame(
                    oldItem: RecommendHearitItem,
                    newItem: RecommendHearitItem,
                ): Boolean = oldItem == newItem
            }
    }
}
