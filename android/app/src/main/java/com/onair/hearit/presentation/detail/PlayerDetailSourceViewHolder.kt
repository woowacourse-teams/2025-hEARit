package com.onair.hearit.presentation.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.databinding.ItemSourceBinding
import com.onair.hearit.domain.model.Source

class PlayerDetailSourceViewHolder(
    val binding: ItemSourceBinding,
    clickListener: PlayerDetailClickListener,
) : RecyclerView.ViewHolder(binding.root) {
    init {
        binding.clickListener = clickListener
    }

    fun bind(source: Source) {
        binding.source = source
    }

    companion object {
        fun create(
            parent: ViewGroup,
            clickListener: PlayerDetailClickListener,
        ): PlayerDetailSourceViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemSourceBinding.inflate(inflater, parent, false)
            return PlayerDetailSourceViewHolder(binding, clickListener)
        }
    }
}
