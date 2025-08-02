package com.onair.hearit.presentation.home

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.domain.model.RecommendHearits

class RecommendHearitAdapter(
    private val hearitClickListener: HearitClickListener,
    private val navigateClickListener: () -> Unit,
) : ListAdapter<RecommendHearits, RecyclerView.ViewHolder>(DiffCallback) {
    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is RecommendHearits.Content -> VIEW_TYPE_CONTENT
            is RecommendHearits.LeftNavigateItem -> VIEW_TYPE_LEFT
            is RecommendHearits.RightNavigateItem -> VIEW_TYPE_RIGHT
        }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerView.ViewHolder =
        when (viewType) {
            VIEW_TYPE_CONTENT -> RecommendViewHolder.create(parent, hearitClickListener)
            VIEW_TYPE_LEFT -> LeftNavigateViewHolder.create(parent, navigateClickListener)
            VIEW_TYPE_RIGHT -> RightNavigateViewHolder.create(parent, navigateClickListener)
            else -> throw IllegalArgumentException(ERROR_INVALID_VIEW_TYPE)
        }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
    ) {
        when (val item = getItem(position)) {
            is RecommendHearits.Content -> (holder as RecommendViewHolder).bind(item.hearit)
            is RecommendHearits.LeftNavigateItem -> (holder as LeftNavigateViewHolder)
            is RecommendHearits.RightNavigateItem -> (holder as RightNavigateViewHolder)
        }
    }

    companion object {
        val DiffCallback =
            object : DiffUtil.ItemCallback<RecommendHearits>() {
                override fun areItemsTheSame(
                    oldItem: RecommendHearits,
                    newItem: RecommendHearits,
                ): Boolean =
                    when {
                        oldItem is RecommendHearits.Content && newItem is RecommendHearits.Content ->
                            oldItem.hearit.id == newItem.hearit.id

                        oldItem is RecommendHearits.LeftNavigateItem && newItem is RecommendHearits.LeftNavigateItem ->
                            true

                        oldItem is RecommendHearits.RightNavigateItem && newItem is RecommendHearits.RightNavigateItem ->
                            true

                        else -> false
                    }

                override fun areContentsTheSame(
                    oldItem: RecommendHearits,
                    newItem: RecommendHearits,
                ): Boolean = oldItem == newItem
            }

        private const val VIEW_TYPE_CONTENT = 0
        private const val VIEW_TYPE_LEFT = 1
        private const val VIEW_TYPE_RIGHT = 2
        private const val ERROR_INVALID_VIEW_TYPE = "유효하지 않은 viewType입니다"
    }
}
