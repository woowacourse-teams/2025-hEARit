package com.onair.hearit.presentation.main.playlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.R
import com.onair.hearit.databinding.ItemPlaylistBinding
import com.onair.hearit.domain.model.Bookmark

class PlaylistViewHolder(
    private val binding: ItemPlaylistBinding,
    private val playlistClickListener: PlaylistClickListener,
) : RecyclerView.ViewHolder(binding.root) {
    init {
        binding.playlistClickListener = playlistClickListener
    }

    fun bind(
        bookmark: Bookmark,
        isPlaying: Boolean,
    ) {
        binding.item = bookmark
        binding.root.isActivated = isPlaying
        val resId = if (isPlaying) R.drawable.ic_bottom_pause else R.drawable.ic_bottom_play
        binding.btnPlaylistPlayPause.setImageResource(resId)
        binding.executePendingBindings()
    }

    fun updatePlayState(isPlaying: Boolean) {
        binding.root.isActivated = isPlaying
        val resId = if (isPlaying) R.drawable.ic_bottom_pause else R.drawable.ic_bottom_play
        binding.btnPlaylistPlayPause.setImageResource(resId)
    }

    companion object {
        fun create(
            parent: ViewGroup,
            playlistClickListener: PlaylistClickListener,
        ): PlaylistViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemPlaylistBinding.inflate(inflater, parent, false)
            return PlaylistViewHolder(binding, playlistClickListener)
        }
    }
}
