package com.onair.hearit.presentation.detail.script

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.onair.hearit.domain.model.ScriptLine

class ScriptAdapter(
    private val onItemClick: (ScriptLine) -> Unit,
) : ListAdapter<ScriptLine, ScriptViewHolder>(DiffCallback) {
    private var highlightedId: Long? = null
    private var highlightedIndex: Int = -1

    fun highlightScriptLine(id: Long?) {
        if (highlightedId == id) return

        val prevIndex = highlightedIndex
        val newIndex = currentList.indexOfFirst { it.id == id }.takeIf { it >= 0 } ?: -1

        highlightedId = id
        highlightedIndex = newIndex

        when {
            prevIndex >= 0 && newIndex >= 0 -> {
                val from = minOf(prevIndex, newIndex)
                val count = kotlin.math.abs(newIndex - prevIndex) + 1
                notifyItemRangeChanged(from, count)
            }

            prevIndex >= 0 -> {
                notifyItemRangeChanged(0, prevIndex + 1)
            }

            prevIndex == -1 && newIndex >= 0 -> {
                notifyItemRangeChanged(0, newIndex + 1)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ScriptViewHolder = ScriptViewHolder.create(parent)

    override fun onBindViewHolder(
        holder: ScriptViewHolder,
        position: Int,
    ) {
        val item = getItem(position)
        val isHighlighted = (item.id == highlightedId)
        val isPast = highlightedIndex >= 0 && position <= highlightedIndex

        holder.bind(item, isHighlighted, isPast, onItemClick)
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
