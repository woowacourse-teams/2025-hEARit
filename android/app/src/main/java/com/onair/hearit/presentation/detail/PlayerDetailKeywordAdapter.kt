package com.onair.hearit.presentation.detail

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.onair.hearit.domain.model.Keyword

class PlayerDetailKeywordAdapter : ListAdapter<Keyword, PlayerDetailKeywordViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): PlayerDetailKeywordViewHolder = PlayerDetailKeywordViewHolder.create(parent)

    override fun onBindViewHolder(
        holder: PlayerDetailKeywordViewHolder,
        position: Int,
    ) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DiffCallback =
            object : DiffUtil.ItemCallback<Keyword>() {
                override fun areItemsTheSame(
                    oldItem: Keyword,
                    newItem: Keyword,
                ): Boolean = oldItem.id == newItem.id

                override fun areContentsTheSame(
                    oldItem: Keyword,
                    newItem: Keyword,
                ): Boolean = oldItem == newItem
            }
    }
}
