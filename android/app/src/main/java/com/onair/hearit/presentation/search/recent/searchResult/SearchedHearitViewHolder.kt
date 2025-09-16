package com.onair.hearit.presentation.search.recent.searchResult

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.databinding.ItemSearchedHearitBinding
import com.onair.hearit.domain.model.SearchedHearit
import com.onair.hearit.presentation.HearitClickListener

class SearchedHearitViewHolder(
    private val binding: ItemSearchedHearitBinding,
    private val hearitClickListener: HearitClickListener,
) : RecyclerView.ViewHolder(binding.root) {
    init {
        binding.clickListener = hearitClickListener
    }

    fun bind(searchedHearit: SearchedHearit) {
        binding.apply {
            this.searchedHearit = searchedHearit
            tvKeywords.text = searchedHearit.keywords.joinToString("  ") { "#${it.name}" }
        }
    }

    companion object {
        fun create(
            parent: ViewGroup,
            clickListener: HearitClickListener,
        ): SearchedHearitViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemSearchedHearitBinding.inflate(inflater, parent, false)
            return SearchedHearitViewHolder(binding, clickListener)
        }
    }
}
