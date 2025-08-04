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
            is RecommendHearits.NavigateItem -> VIEW_TYPE_NAVIGATE
        }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerView.ViewHolder =
        when (viewType) {
            VIEW_TYPE_CONTENT -> RecommendViewHolder.create(parent, hearitClickListener)
            VIEW_TYPE_NAVIGATE -> NavigateViewHolder.create(parent, navigateClickListener)
            else -> throw IllegalArgumentException(ERROR_INVALID_VIEW_TYPE)
        }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
    ) {
        when (val item = getItem(position)) {
            is RecommendHearits.Content -> (holder as RecommendViewHolder).bind(item.hearit)
            is RecommendHearits.NavigateItem -> (holder as NavigateViewHolder).bind(item.direction)
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

                        oldItem is RecommendHearits.NavigateItem && newItem is RecommendHearits.NavigateItem ->
                            true

                        else -> false
                    }

                override fun areContentsTheSame(
                    oldItem: RecommendHearits,
                    newItem: RecommendHearits,
                ): Boolean = oldItem == newItem
            }

        private const val VIEW_TYPE_CONTENT = 0
        private const val VIEW_TYPE_NAVIGATE = 1
        private const val ERROR_INVALID_VIEW_TYPE = "유효하지 않은 viewType입니다"
    }
}
