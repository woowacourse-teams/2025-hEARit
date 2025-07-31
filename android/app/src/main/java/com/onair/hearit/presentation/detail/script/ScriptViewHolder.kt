package com.onair.hearit.presentation.detail.script

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.databinding.ItemScriptBinding
import com.onair.hearit.domain.model.ScriptLine

class ScriptViewHolder(
    private val binding: ItemScriptBinding,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(
        item: ScriptLine,
        isHighlighted: Boolean,
    ) {
        binding.scriptLine = item
        binding.isHighlighted = isHighlighted
    }

    companion object {
        fun create(parent: ViewGroup): ScriptViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemScriptBinding.inflate(inflater, parent, false)
            return ScriptViewHolder(binding)
        }
    }
}
