package com.onair.hearit.presentation.explore

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.R
import com.onair.hearit.databinding.ItemExploreScriptBinding
import com.onair.hearit.domain.SubtitleLine

class ScriptViewHolder(
    parent: ViewGroup,
) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_explore_script, parent, false),
    ) {
    private val binding = ItemExploreScriptBinding.bind(itemView)

    fun bind(
        subtitleLine: SubtitleLine,
        isHighlighted: Boolean,
    ) {
        binding.tvExploreScriptSentence.text = subtitleLine.sentence
        val colorRes = if (isHighlighted) R.color.white else R.color.hearit_gray2
        val textSizeSp = if (isHighlighted) 16f else 14f
        binding.tvExploreScriptSentence.setTextColor(
            ContextCompat.getColor(
                itemView.context,
                colorRes,
            ),
        )
        binding.tvExploreScriptSentence.textSize = textSizeSp
    }
}
