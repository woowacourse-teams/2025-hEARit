package com.onair.hearit.presentation.search.recent.searchResult

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.analytics.HearitSource
import com.onair.hearit.databinding.ItemSearchedHearitBinding
import com.onair.hearit.domain.model.SearchedHearit
import com.onair.hearit.presentation.HearitClickListener

class SearchedHearitViewHolder(
    private val binding: ItemSearchedHearitBinding,
    private val source: HearitSource,
    hearitClickListener: HearitClickListener,
) : RecyclerView.ViewHolder(binding.root) {
    init {
        binding.clickListener = hearitClickListener
    }

    fun bind(searchedHearit: SearchedHearit) {
        binding.searchedHearit = searchedHearit
        binding.tvKeywords.text = searchedHearit.keywords.joinToString("  ") { "#${it.name}" }
        binding.source = source
    }

    companion object {
        fun create(
            parent: ViewGroup,
            source: HearitSource,
            hearitClickListener: HearitClickListener,
        ): SearchedHearitViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemSearchedHearitBinding.inflate(inflater, parent, false)
            return SearchedHearitViewHolder(binding, source, hearitClickListener)
        }
    }
}
