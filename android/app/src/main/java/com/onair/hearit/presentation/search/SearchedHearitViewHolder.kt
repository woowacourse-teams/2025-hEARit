package com.onair.hearit.presentation.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.databinding.ItemSearchedHearitBinding
import com.onair.hearit.domain.SearchedHearitItem

class SearchedHearitViewHolder(
    private val binding: ItemSearchedHearitBinding,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(searchedHearit: SearchedHearitItem) {
        binding.item = searchedHearit
    }

    companion object {
        fun create(parent: ViewGroup): SearchedHearitViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemSearchedHearitBinding.inflate(inflater, parent, false)
            return SearchedHearitViewHolder(binding)
        }
    }
}
