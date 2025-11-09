package com.onair.hearit.presentation.home.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.onair.hearit.analytics.HearitSource
import com.onair.hearit.domain.model.RecentUploadHearit
import com.onair.hearit.presentation.HearitClickListener

class RecentUploadHearitAdapter(
    private val hearitClickListener: HearitClickListener,
) : ListAdapter<RecentUploadHearit, RecentUploadHearitViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecentUploadHearitViewHolder = RecentUploadHearitViewHolder.create(parent, HearitSource.RECENT_UPLOAD, hearitClickListener)

    override fun onBindViewHolder(
        holder: RecentUploadHearitViewHolder,
        position: Int,
    ) = holder.bind(getItem(position))

    companion object {
        private val DiffCallback =
            object : DiffUtil.ItemCallback<RecentUploadHearit>() {
                override fun areItemsTheSame(
                    oldItem: RecentUploadHearit,
                    newItem: RecentUploadHearit,
                ): Boolean = oldItem.id == newItem.id

                override fun areContentsTheSame(
                    oldItem: RecentUploadHearit,
                    newItem: RecentUploadHearit,
                ): Boolean = oldItem == newItem
            }
    }
}
