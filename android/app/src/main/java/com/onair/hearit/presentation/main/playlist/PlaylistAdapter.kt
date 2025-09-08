package com.onair.hearit.presentation.main.playlist

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.onair.hearit.domain.model.Bookmark

class PlaylistAdapter : ListAdapter<Bookmark, PlaylistViewHolder>(DiffCallback) {
    init {
        setHasStableIds(true)
    }

    private var currentPlayingId: Long? = null

    fun updatePlaying(newId: Long?) {
        if (currentPlayingId == newId) return

        val oldPos = currentPlayingId?.let { findPositionById(it) }
        val newPos = newId?.let { findPositionById(it) }
        currentPlayingId = newId

        oldPos?.let { notifyItemChanged(it, PAYLOAD_PLAY_STATE) }
        newPos?.let { notifyItemChanged(it, PAYLOAD_PLAY_STATE) }
    }

    private fun findPositionById(id: Long): Int? = currentList.indexOfFirst { it.bookmarkId == id }.takeIf { it >= 0 }

    override fun getItemId(position: Int): Long = getItem(position).bookmarkId

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): PlaylistViewHolder = PlaylistViewHolder.create(parent)

    override fun onBindViewHolder(
        holder: PlaylistViewHolder,
        position: Int,
    ) {
        val item: Bookmark = getItem(position)
        holder.bind(item, isPlaying = (item.bookmarkId == currentPlayingId))
    }

    override fun onBindViewHolder(
        holder: PlaylistViewHolder,
        position: Int,
        payloads: MutableList<Any>,
    ) {
        if (payloads.contains(PAYLOAD_PLAY_STATE)) {
            val item = getItem(position)
            holder.updatePlayState(isPlaying = (item.bookmarkId == currentPlayingId))
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    companion object {
        private const val PAYLOAD_PLAY_STATE = "payload_play_state"

        private val DiffCallback =
            object : DiffUtil.ItemCallback<Bookmark>() {
                override fun areItemsTheSame(
                    oldItem: Bookmark,
                    newItem: Bookmark,
                ): Boolean = oldItem.bookmarkId == newItem.bookmarkId

                override fun areContentsTheSame(
                    oldItem: Bookmark,
                    newItem: Bookmark,
                ): Boolean = oldItem == newItem
            }
    }
}
