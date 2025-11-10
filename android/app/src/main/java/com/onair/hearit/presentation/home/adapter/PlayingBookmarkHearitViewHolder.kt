package com.onair.hearit.presentation.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.analytics.HearitSource
import com.onair.hearit.databinding.ItemPlayingBookmarkHearitBinding
import com.onair.hearit.domain.model.Bookmark
import com.onair.hearit.presentation.HearitClickListener

class PlayingBookmarkHearitViewHolder private constructor(
    private val binding: ItemPlayingBookmarkHearitBinding,
    private val source: HearitSource,
    hearitClickListener: HearitClickListener,
) : RecyclerView.ViewHolder(binding.root) {
    init {
        binding.clickListener = hearitClickListener
        binding.source = source
    }

    fun bind(item: Bookmark) {
        binding.playingBookmarkHearit = item
    }

    companion object {
        fun create(
            parent: ViewGroup,
            source: HearitSource,
            hearitClickListener: HearitClickListener,
        ): PlayingBookmarkHearitViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemPlayingBookmarkHearitBinding.inflate(inflater, parent, false)
            return PlayingBookmarkHearitViewHolder(binding, source, hearitClickListener)
        }
    }
}
