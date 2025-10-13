package com.onair.hearit.presentation.main.playlist

import android.util.LongSparseArray
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.onair.hearit.domain.model.Bookmark

class PlaylistAdapter(
    private val playlistClickListener: PlaylistClickListener,
) : ListAdapter<Bookmark, PlaylistViewHolder>(DiffCallback) {
    init {
        setHasStableIds(true)
    }

    private var currentPlayingId: Long? = null
    private var currentPlayMode: String? = null
    private var isPlayerPlaying: Boolean = false
    private val idToPosition = LongSparseArray<Int>()

    fun updatePlaying(
        newId: Long?,
        newMode: String?,
    ) {
        if ((currentPlayingId == newId) && currentPlayMode.equals(newMode)) return

        val oldPosition = currentPlayingId?.let { findPositionById(it) }
        val newPosition = newId?.let { findPositionById(it) }
        currentPlayingId = newId
        currentPlayMode = newMode

        oldPosition?.let { notifyItemChanged(it, PAYLOAD_PLAY_STATE) }
        newPosition?.let { notifyItemChanged(it, PAYLOAD_PLAY_STATE) }
    }

    fun updateIsPlaying(isPlaying: Boolean) {
        if (isPlayerPlaying == isPlaying) return
        isPlayerPlaying = isPlaying
        val pos = currentPlayingId?.let { findPositionById(it) } ?: return
        notifyItemChanged(pos, PAYLOAD_PLAY_STATE)
    }

    private fun findPositionById(id: Long): Int? = idToPosition.get(id)?.takeIf { it >= 0 }

    private fun isActiveForBackground(itemId: Long): Boolean {
        val isLibrary = currentPlayMode?.equals("LIBRARY", ignoreCase = true) == true
        return isLibrary && (itemId == currentPlayingId)
    }

    override fun getItemId(position: Int): Long = getItem(position).bookmarkId

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): PlaylistViewHolder = PlaylistViewHolder.create(parent, playlistClickListener)

    override fun onBindViewHolder(
        holder: PlaylistViewHolder,
        position: Int,
    ) {
        val item: Bookmark = getItem(position)
        val active = isActiveForBackground(item.bookmarkId)
        holder.bind(item, isActive = active, isPlaying = (active && isPlayerPlaying))
    }

    override fun onBindViewHolder(
        holder: PlaylistViewHolder,
        position: Int,
        payloads: MutableList<Any>,
    ) {
        if (payloads.contains(PAYLOAD_PLAY_STATE)) {
            val item = getItem(position)
            val active = isActiveForBackground(item.bookmarkId)
            holder.updatePlayState(isActive = active, isPlaying = (active && isPlayerPlaying))
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
