package com.onair.hearit.presentation.explore

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.onair.hearit.domain.model.ScriptLine

class ExploreScriptAdapter : ListAdapter<ScriptLine, ExploreScriptViewHolder>((DiffCallback)) {
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
    ): ExploreScriptViewHolder = ExploreScriptViewHolder.create(parent)

    override fun onBindViewHolder(
        holder: ExploreScriptViewHolder,
        position: Int,
    ) {
        val item = getItem(position)
        val isHighlighted = item.id == highlightedId
        holder.bind(item, isHighlighted)
    }

    override fun getItemCount(): Int = currentList.size

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
