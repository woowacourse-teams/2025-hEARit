package com.onair.hearit.presentation.explore

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.databinding.ItemExploreScriptBinding
import com.onair.hearit.domain.SubtitleLine

class ScriptViewHolder(
    private val binding: ItemExploreScriptBinding,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(
        subtitleLine: SubtitleLine,
        isHighlighted: Boolean,
    ) {
        binding.subtitleLine = subtitleLine
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
