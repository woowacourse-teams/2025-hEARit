package com.onair.hearit.presentation.search.recent

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.databinding.ItemRecentKeywordBinding
import com.onair.hearit.domain.model.RecentSearch

class RecentSearchViewHolder private constructor(
    private val binding: ItemRecentKeywordBinding,
    private val listener: RecentSearchClickListener,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: RecentSearch) {
        binding.item = item
        binding.listener = listener
    }

    companion object {
        fun create(
            parent: ViewGroup,
            listener: RecentSearchClickListener,
        ): RecentSearchViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemRecentKeywordBinding.inflate(inflater, parent, false)
            return RecentSearchViewHolder(binding, listener)
        }
    }
}
