package com.onair.hearit.presentation.detail

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.onair.hearit.domain.model.ScriptLine

class PlayerDetailScriptAdapter : ListAdapter<ScriptLine, PlayerDetailScriptViewHolder>(DiffCallback) {
    private var highlightedId: Long? = null

    fun highlightScriptLine(id: Long?) {
        if (highlightedId == id) return

        val previousId = highlightedId
        highlightedId = id

        val prevIndex = currentList.indexOfFirst { it.id == previousId }
        val newIndex = currentList.indexOfFirst { it.id == id }

        if (prevIndex != -1) notifyItemChanged(prevIndex)
        if (newIndex != -1) notifyItemChanged(newIndex)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): PlayerDetailScriptViewHolder = PlayerDetailScriptViewHolder.create(parent)

    override fun onBindViewHolder(
        holder: PlayerDetailScriptViewHolder,
        position: Int,
    ) {
        val item = getItem(position)
        val binding = holder.binding

        binding.scriptLine = item
        binding.isHighlighted = item.id == highlightedId
    }

    companion object {
        private val DiffCallback =
            object : DiffUtil.ItemCallback<ScriptLine>() {
                override fun areItemsTheSame(
                    oldItem: ScriptLine,
                    newItem: ScriptLine,
                ): Boolean = oldItem.id == newItem.id

                override fun areContentsTheSame(
                    oldItem: ScriptLine,
                    newItem: ScriptLine,
                ): Boolean = oldItem == newItem
            }
    }
}
