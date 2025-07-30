package com.onair.hearit.presentation.search.recent

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.databinding.ItemRecentKeywordBinding
import com.onair.hearit.domain.model.RecentKeyword

class RecentKeywordViewHolder private constructor(
    private val binding: ItemRecentKeywordBinding,
    private val listener: KeywordClickListener,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: RecentKeyword) {
        binding.item = item
        binding.listener = listener
    }

    companion object {
        fun create(
            parent: ViewGroup,
            listener: KeywordClickListener,
        ): RecentKeywordViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemRecentKeywordBinding.inflate(inflater, parent, false)
            return RecentKeywordViewHolder(binding, listener)
        }
    }
}
