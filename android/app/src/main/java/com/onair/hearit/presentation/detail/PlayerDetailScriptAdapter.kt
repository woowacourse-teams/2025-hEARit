package com.onair.hearit.presentation.detail

import android.graphics.Color
import android.graphics.Typeface
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.onair.hearit.R
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
        val context = holder.itemView.context
        val binding = holder.binding

        val timePrefix = formatTime(item.start)
        binding.tvScriptBody.text = item.text
        binding.tvScriptTime.text = timePrefix

        binding.tvScriptBody.setTextColor(ContextCompat.getColor(context, R.color.hearit_gray4))
        binding.tvScriptBody.setBackgroundColor(Color.TRANSPARENT)
        binding.tvScriptTime.setTextColor(ContextCompat.getColor(context, R.color.hearit_gray4))
        binding.tvScriptTime.setBackgroundColor(Color.TRANSPARENT)

        if (item.id == highlightedId) {
            binding.tvScriptBody.textSize = 18f
            binding.tvScriptBody.setTypeface(null, Typeface.BOLD)
            binding.tvScriptTime.textSize = 18f
            binding.tvScriptTime.setTypeface(null, Typeface.BOLD)
        } else {
            binding.tvScriptBody.textSize = 16f
            binding.tvScriptBody.setTypeface(null, Typeface.NORMAL)
            binding.tvScriptTime.textSize = 16f
            binding.tvScriptTime.setTypeface(null, Typeface.NORMAL)
        }
    }

    private fun formatTime(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
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
