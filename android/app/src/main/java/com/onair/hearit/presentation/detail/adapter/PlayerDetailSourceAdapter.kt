package com.onair.hearit.presentation.detail.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.onair.hearit.domain.model.Source
import com.onair.hearit.presentation.detail.PlayerDetailClickListener

class PlayerDetailSourceAdapter(
    private val clickListener: PlayerDetailClickListener,
) : ListAdapter<Source, PlayerDetailSourceViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): PlayerDetailSourceViewHolder = PlayerDetailSourceViewHolder.create(parent, clickListener)

    override fun onBindViewHolder(
        holder: PlayerDetailSourceViewHolder,
        position: Int,
    ) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DiffCallback =
            object : DiffUtil.ItemCallback<Source>() {
                override fun areItemsTheSame(
                    oldItem: Source,
                    newItem: Source,
                ): Boolean = oldItem.url == newItem.url

                override fun areContentsTheSame(
                    oldItem: Source,
                    newItem: Source,
                ): Boolean = oldItem == newItem
            }
    }
}
