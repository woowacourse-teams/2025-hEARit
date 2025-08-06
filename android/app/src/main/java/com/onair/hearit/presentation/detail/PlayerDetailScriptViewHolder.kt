package com.onair.hearit.presentation.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.databinding.ItemDetailScriptBinding

class PlayerDetailScriptViewHolder(
    val binding: ItemDetailScriptBinding,
) : RecyclerView.ViewHolder(binding.root) {
    companion object {
        fun create(parent: ViewGroup): PlayerDetailScriptViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemDetailScriptBinding.inflate(inflater, parent, false)
            return PlayerDetailScriptViewHolder(binding)
        }
    }
}
