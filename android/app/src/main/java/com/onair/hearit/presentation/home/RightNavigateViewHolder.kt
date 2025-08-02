package com.onair.hearit.presentation.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.databinding.ItemRightNavigateBinding

class RightNavigateViewHolder(
    private val binding: ItemRightNavigateBinding,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(clickListener: () -> Unit) {
        binding.navigateClickListener = clickListener
    }

    companion object {
        fun create(
            parent: ViewGroup,
            navigateClickListener: () -> Unit,
        ): RightNavigateViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemRightNavigateBinding.inflate(inflater, parent, false)
            return RightNavigateViewHolder(binding).apply {
                bind(navigateClickListener)
            }
        }
    }
}
