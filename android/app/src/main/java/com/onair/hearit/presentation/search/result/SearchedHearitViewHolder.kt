package com.onair.hearit.presentation.search.result

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.databinding.ItemSearchedHearitBinding
import com.onair.hearit.domain.model.SearchedHearit
import com.onair.hearit.presentation.home.HearitClickListener

class SearchedHearitViewHolder(
    private val binding: ItemSearchedHearitBinding,
    private val clickListener: HearitClickListener,
) : RecyclerView.ViewHolder(binding.root) {
    private val keywordAdapter = SearchKeywordAdapter()

    init {
        binding.rvKeyword.adapter = keywordAdapter
    }

    fun bind(searchedHearit: SearchedHearit) {
        binding.item = searchedHearit
        keywordAdapter.submitList(searchedHearit.keywords)
        binding.clickListener = clickListener
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
