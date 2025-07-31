package com.onair.hearit.presentation.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.databinding.ItemKeywordBinding
import com.onair.hearit.domain.model.Keyword

class PlayerDetailKeywordViewHolder private constructor(
    private val binding: ItemKeywordBinding,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(keyword: Keyword) {
        binding.keyword = keyword
    }

    companion object {
        fun create(parent: ViewGroup): PlayerDetailKeywordViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemKeywordBinding.inflate(inflater, parent, false)
            return PlayerDetailKeywordViewHolder(binding)
        }
    }
}
