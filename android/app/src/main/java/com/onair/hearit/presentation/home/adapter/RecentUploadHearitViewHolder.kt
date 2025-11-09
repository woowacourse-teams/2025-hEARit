package com.onair.hearit.presentation.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.analytics.HearitSource
import com.onair.hearit.databinding.ItemRecentUploadHearitBinding
import com.onair.hearit.domain.model.RecentUploadHearit
import com.onair.hearit.presentation.HearitClickListener

class RecentUploadHearitViewHolder private constructor(
    private val binding: ItemRecentUploadHearitBinding,
    private val source: HearitSource,
    hearitClickListener: HearitClickListener,
) : RecyclerView.ViewHolder(binding.root) {
    init {
        binding.clickListener = hearitClickListener
        binding.source = source
    }

    fun bind(item: RecentUploadHearit) {
        binding.recentUploadHearit = item
    }

    companion object {
        fun create(
            parent: ViewGroup,
            source: HearitSource,
            hearitClickListener: HearitClickListener,
        ): RecentUploadHearitViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemRecentUploadHearitBinding.inflate(inflater, parent, false)
            return RecentUploadHearitViewHolder(binding, source, hearitClickListener)
        }
    }
}
