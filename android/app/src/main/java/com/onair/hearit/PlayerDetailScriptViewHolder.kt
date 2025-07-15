package com.onair.hearit

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.databinding.ItemScriptBinding

class PlayerDetailScriptViewHolder(
    val binding: ItemScriptBinding,
) : RecyclerView.ViewHolder(binding.root) {
    companion object {
        fun create(parent: ViewGroup): PlayerDetailScriptViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemScriptBinding.inflate(inflater, parent, false)
            return PlayerDetailScriptViewHolder(binding)
        }
    }
}
