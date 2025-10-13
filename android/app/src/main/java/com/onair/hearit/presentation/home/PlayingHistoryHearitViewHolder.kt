package com.onair.hearit.presentation.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.databinding.ItemPlayingHistoryHearitBinding
import com.onair.hearit.domain.model.PlayingHistoryHearit
import com.onair.hearit.presentation.HearitClickListener

class PlayingHistoryHearitViewHolder private constructor(
    private val binding: ItemPlayingHistoryHearitBinding,
    hearitClickListener: HearitClickListener,
) : RecyclerView.ViewHolder(binding.root) {
    init {
        binding.clickListener = hearitClickListener
    }

    fun bind(item: PlayingHistoryHearit) {
        binding.playingHistoryHearit = item
    }

    companion object {
        fun create(
            parent: ViewGroup,
            hearitClickListener: HearitClickListener,
        ): PlayingHistoryHearitViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemPlayingHistoryHearitBinding.inflate(inflater, parent, false)
            return PlayingHistoryHearitViewHolder(binding, hearitClickListener)
        }
    }
}
