package com.onair.hearit.presentation.main.playlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.databinding.ItemPlaylistBinding
import com.onair.hearit.domain.model.Bookmark

class PlaylistViewHolder(
    private val binding: ItemPlaylistBinding,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(bookmark: Bookmark) {
        binding.item = bookmark
    }

    companion object {
        fun create(parent: ViewGroup): PlaylistViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemPlaylistBinding.inflate(inflater, parent, false)
            return PlaylistViewHolder(binding)
        }
    }
}
