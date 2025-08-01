package com.onair.hearit.presentation.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.databinding.ItemSearchedHearitBinding
import com.onair.hearit.domain.model.SearchedHearit

class SearchedHearitViewHolder(
    private val binding: ItemSearchedHearitBinding,
    private val listener: SearchResultClickListener,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(searchedHearit: SearchedHearit) {
        binding.item = searchedHearit
        binding.listener = listener
    }

    companion object {
        fun create(
            parent: ViewGroup,
            listener: SearchResultClickListener,
        ): SearchedHearitViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemSearchedHearitBinding.inflate(inflater, parent, false)
            return SearchedHearitViewHolder(binding, listener)
        }
    }
}
