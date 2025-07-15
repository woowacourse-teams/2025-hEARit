package com.onair.hearit.presentation.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.R
import com.onair.hearit.databinding.ItemKeywordBinding
import com.onair.hearit.domain.KeywordItem

class KeywordViewHolder(
    parent: ViewGroup,
) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_keyword, parent, false),
    ) {
    private val binding = ItemKeywordBinding.bind(itemView)

    fun bind(keywordItem: KeywordItem) {
        binding.tvKeyword.text = keywordItem.keyword
    }
}
