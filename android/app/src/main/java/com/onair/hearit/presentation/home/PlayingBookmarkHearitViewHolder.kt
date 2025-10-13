package com.onair.hearit.presentation.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.databinding.ItemPlayingBookmarkHearitBinding
import com.onair.hearit.domain.model.Bookmark
import com.onair.hearit.presentation.HearitClickListener

class PlayingBookmarkHearitViewHolder private constructor(
    private val binding: ItemPlayingBookmarkHearitBinding,
    hearitClickListener: HearitClickListener,
) : RecyclerView.ViewHolder(binding.root) {
    init {
        binding.clickListener = hearitClickListener
    }

    fun bind(item: Bookmark) {
        binding.playingBookmarkHearit = item
    }

    companion object {
        fun create(
            parent: ViewGroup,
            hearitClickListener: HearitClickListener,
        ): PlayingBookmarkHearitViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemPlayingBookmarkHearitBinding.inflate(inflater, parent, false)
            return PlayingBookmarkHearitViewHolder(binding, hearitClickListener)
        }
    }
}
