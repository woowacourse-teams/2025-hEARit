package com.onair.hearit.presentation.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.databinding.ItemKeywordBinding
import com.onair.hearit.domain.KeywordItem

class KeywordViewHolder(
    val binding: ItemKeywordBinding,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(keywordItem: KeywordItem) {
        binding.keywordItem = keywordItem
    }

    companion object {
        fun create(parent: ViewGroup): KeywordViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemKeywordBinding.inflate(inflater, parent, false)
            return KeywordViewHolder(binding)
        }
    }
}
