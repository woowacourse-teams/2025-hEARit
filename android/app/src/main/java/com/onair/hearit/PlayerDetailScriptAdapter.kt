package com.onair.hearit

import android.graphics.Color
import android.graphics.Typeface
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class PlayerDetailScriptAdapter(
    private val items: List<ScriptLine>,
) : RecyclerView.Adapter<PlayerDetailScriptViewHolder>() {
    private var highlightedPosition = -1

    fun highlightPosition(position: Int) {
        if (position == highlightedPosition) return

        val oldPos = highlightedPosition
        highlightedPosition = position

        if (oldPos != -1) notifyItemChanged(oldPos)
        notifyItemChanged(highlightedPosition)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): PlayerDetailScriptViewHolder = PlayerDetailScriptViewHolder.create(parent)

    override fun onBindViewHolder(
        holder: PlayerDetailScriptViewHolder,
        position: Int,
    ) {
        val item = items[position]
        val context = holder.itemView.context
        val binding = holder.binding

        val timePrefix = formatTime(item.startTimeMs)
        binding.tvScriptBody.text = "$timePrefix ${item.text}"

        if (position == highlightedPosition) {
            binding.tvScriptBody.setTextColor(ContextCompat.getColor(context, R.color.hearit_gray4))
            binding.tvScriptBody.setBackgroundColor(Color.TRANSPARENT)
            binding.tvScriptBody.textSize = 18f
            binding.tvScriptBody.setTypeface(null, Typeface.BOLD)
        } else {
            binding.tvScriptBody.setTextColor(ContextCompat.getColor(context, R.color.hearit_gray4))
            binding.tvScriptBody.setBackgroundColor(Color.TRANSPARENT)
            binding.tvScriptBody.textSize = 16f
            binding.tvScriptBody.setTypeface(null, Typeface.NORMAL)
        }
    }

    override fun getItemCount() = items.size

    private fun formatTime(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}
