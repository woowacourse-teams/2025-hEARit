package com.onair.hearit.presentation.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.databinding.ItemLeftNavigateBinding

class LeftNavigateViewHolder(
    private val binding: ItemLeftNavigateBinding,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(clickListener: () -> Unit) {
        binding.navigateClickListener = clickListener
    }

    companion object {
        fun create(
            parent: ViewGroup,
            navigateClickListener: () -> Unit,
        ): LeftNavigateViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemLeftNavigateBinding.inflate(inflater, parent, false)
            return LeftNavigateViewHolder(binding).apply {
                bind(navigateClickListener)
            }
        }
    }
}
