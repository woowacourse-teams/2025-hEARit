package com.onair.hearit.presentation.explore

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.onair.hearit.domain.SubtitleLine

class ScriptAdapter : ListAdapter<SubtitleLine, ScriptViewHolder>((DiffCallback)) {
    private var highlightedId: Long? = null

    fun highlightSubtitle(id: Long?) {
        if (highlightedId != id) {
            val previousId = highlightedId
            highlightedId = id

            val prevIndex = currentList.indexOfFirst { it.id == previousId }
            val newIndex = currentList.indexOfFirst { it.id == id }

            if (prevIndex != -1) notifyItemChanged(prevIndex)
            if (newIndex != -1) notifyItemChanged(newIndex)
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
        val isHighlighted = item.id == highlightedId
        holder.bind(item, isHighlighted)
    }

    override fun getItemCount(): Int = currentList.size

    companion object {
        private val DiffCallback =
            object : DiffUtil.ItemCallback<SubtitleLine>() {
                override fun areItemsTheSame(
                    oldItem: SubtitleLine,
                    newItem: SubtitleLine,
                ): Boolean = oldItem.id == newItem.id

                override fun areContentsTheSame(
                    oldItem: SubtitleLine,
                    newItem: SubtitleLine,
                ): Boolean = oldItem == newItem
            }
    }
}
