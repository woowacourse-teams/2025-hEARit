package com.onair.hearit.presentation.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.databinding.ItemRecentHearitBinding
import com.onair.hearit.domain.model.PlayingHistoryHearit
import com.onair.hearit.presentation.HearitClickListener

class RecentHearitViewHolder private constructor(
    private val binding: ItemRecentHearitBinding,
    hearitClickListener: HearitClickListener,
) : RecyclerView.ViewHolder(binding.root) {
    init {
        binding.clickListener = hearitClickListener
    }

    fun bind(item: PlayingHistoryHearit) {
        binding.recentHearit = item
    }

    companion object {
        fun create(
            parent: ViewGroup,
            hearitClickListener: HearitClickListener,
        ): RecentHearitViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemRecentHearitBinding.inflate(inflater, parent, false)
            return RecentHearitViewHolder(binding, hearitClickListener)
        }
    }
}
