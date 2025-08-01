package com.onair.hearit.presentation.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.databinding.ItemKeywordBinding
import com.onair.hearit.domain.model.Keyword

class KeywordViewHolder private constructor(
    private val binding: ItemKeywordBinding,
    private val listener: KeywordClickListener,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(keyword: Keyword) {
        binding.keyword = keyword
        binding.listener = listener
    }

    companion object {
        fun create(
            parent: ViewGroup,
            listener: KeywordClickListener,
        ): KeywordViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemKeywordBinding.inflate(inflater, parent, false)
            return KeywordViewHolder(binding, listener)
        }
    }
}
