package com.onair.hearit.presentation.explore

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.databinding.ItemExploreScriptBinding
import com.onair.hearit.domain.model.ScriptLine

class ScriptViewHolder(
    private val binding: ItemExploreScriptBinding,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(
        scriptLine: ScriptLine,
        isHighlighted: Boolean,
    ) {
        binding.subtitleLine = scriptLine
        binding.isHighlighted = isHighlighted
    }

    companion object {
        fun create(parent: ViewGroup): ScriptViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemExploreScriptBinding.inflate(inflater, parent, false)
            return ScriptViewHolder(binding)
        }
    }
}
