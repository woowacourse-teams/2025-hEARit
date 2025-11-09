package com.onair.hearit.presentation.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.analytics.HearitSource
import com.onair.hearit.databinding.ItemPlayingHistoryHearitBinding
import com.onair.hearit.domain.model.PlayingHistoryHearit
import com.onair.hearit.presentation.HearitClickListener

class PlayingHistoryHearitViewHolder private constructor(
    private val binding: ItemPlayingHistoryHearitBinding,
    private val source: HearitSource,
    hearitClickListener: HearitClickListener,
) : RecyclerView.ViewHolder(binding.root) {
    init {
        binding.clickListener = hearitClickListener
        binding.source = source
    }

    fun bind(item: PlayingHistoryHearit) {
        binding.playingHistoryHearit = item
    }

    companion object {
        fun create(
            parent: ViewGroup,
            source: HearitSource,
            hearitClickListener: HearitClickListener,
        ): PlayingHistoryHearitViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemPlayingHistoryHearitBinding.inflate(inflater, parent, false)
            return PlayingHistoryHearitViewHolder(binding, source, hearitClickListener)
        }
    }
}
