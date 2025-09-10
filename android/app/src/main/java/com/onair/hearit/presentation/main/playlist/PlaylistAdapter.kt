package com.onair.hearit.presentation.main.playlist

import android.util.LongSparseArray
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.onair.hearit.domain.model.Bookmark

class PlaylistAdapter : ListAdapter<Bookmark, PlaylistViewHolder>(DiffCallback) {
    init {
        setHasStableIds(true)
    }

    private var currentPlayingId: Long? = null
    private var currentPlayMode: String? = null
    private val idToPosition = LongSparseArray<Int>()

    fun updatePlaying(
        newId: Long?,
        newMode: String?,
    ) {
        if (currentPlayingId == newId && currentPlayMode.equals(newMode, ignoreCase = true)) return

        val oldPos = currentPlayingId?.let { findPositionById(it) }
        val newPos = newId?.let { findPositionById(it) }
        currentPlayingId = newId
        currentPlayMode = newMode

        oldPos?.let { notifyItemChanged(it, PAYLOAD_PLAY_STATE) }
        newPos?.let { notifyItemChanged(it, PAYLOAD_PLAY_STATE) }
    }

    private fun findPositionById(id: Long): Int? = idToPosition.get(id)?.takeIf { it >= 0 }

    private fun isActiveForBg(itemId: Long): Boolean {
        val isLibrary = currentPlayMode?.equals("LIBRARY", ignoreCase = true) == true
        return isLibrary && (itemId == currentPlayingId)
    }

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
        idToPosition.put(item.bookmarkId, position)
        holder.bind(item, isPlaying = isActiveForBg(item.bookmarkId))
    }

    override fun onBindViewHolder(
        holder: PlaylistViewHolder,
        position: Int,
        payloads: MutableList<Any>,
    ) {
        if (payloads.contains(PAYLOAD_PLAY_STATE)) {
            val item = getItem(position)
            holder.updatePlayState(isPlaying = isActiveForBg(item.bookmarkId))
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    override fun onCurrentListChanged(
        previousList: MutableList<Bookmark>,
        currentList: MutableList<Bookmark>,
    ) {
        super.onCurrentListChanged(previousList, currentList)
        rebuildIndex(currentList)
    }

    private fun rebuildIndex(list: List<Bookmark>) {
        idToPosition.clear()
        list.forEachIndexed { index, item ->
            idToPosition.put(item.bookmarkId, index)
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
