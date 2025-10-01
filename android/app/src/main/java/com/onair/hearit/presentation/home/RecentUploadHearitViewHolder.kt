package com.onair.hearit.presentation.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.databinding.ItemRecentUploadHearitBinding
import com.onair.hearit.domain.model.RecentUploadHearit
import com.onair.hearit.presentation.HearitClickListener

class RecentUploadHearitViewHolder private constructor(
    private val binding: ItemRecentUploadHearitBinding,
    hearitClickListener: HearitClickListener,
) : RecyclerView.ViewHolder(binding.root) {
    init {
        binding.clickListener = hearitClickListener
    }

    fun bind(item: RecentUploadHearit) {
        binding.recentUploadHearit = item
    }

    companion object {
        fun create(
            parent: ViewGroup,
            hearitClickListener: HearitClickListener,
        ): RecentUploadHearitViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemRecentUploadHearitBinding.inflate(inflater, parent, false)
            return RecentUploadHearitViewHolder(binding, hearitClickListener)
        }
    }
}
