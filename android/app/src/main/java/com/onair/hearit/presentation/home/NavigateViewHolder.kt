package com.onair.hearit.presentation.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.databinding.ItemNavigateBinding
import com.onair.hearit.domain.model.Direction
import com.onair.hearit.presentation.dpToPx

class NavigateViewHolder(
    private val binding: ItemNavigateBinding,
    private val navigateClickListener: () -> Unit,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(direction: Direction) {
        binding.navigateClickListener = navigateClickListener
        val layoutParams = binding.root.layoutParams as ViewGroup.MarginLayoutParams
        val sideMargin = 40.dpToPx(itemView.context)

        layoutParams.marginStart = if (direction == Direction.RIGHT) sideMargin else 0
        layoutParams.marginEnd = if (direction == Direction.LEFT) sideMargin else 0

        binding.root.layoutParams = layoutParams
    }

    companion object {
        fun create(
            parent: ViewGroup,
            navigateClickListener: () -> Unit,
        ): NavigateViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemNavigateBinding.inflate(inflater, parent, false)
            return NavigateViewHolder(binding, navigateClickListener)
        }
    }
}
