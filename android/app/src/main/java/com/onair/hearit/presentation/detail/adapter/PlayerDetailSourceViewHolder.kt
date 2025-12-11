package com.onair.hearit.presentation.detail.adapter

import android.text.SpannableString
import android.text.Spanned
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.databinding.ItemSourceBinding
import com.onair.hearit.domain.model.Source
import com.onair.hearit.presentation.detail.PlayerDetailClickListener

class PlayerDetailSourceViewHolder(
    val binding: ItemSourceBinding,
    clickListener: PlayerDetailClickListener,
) : RecyclerView.ViewHolder(binding.root) {
    init {
        binding.clickListener = clickListener
    }

    fun bind(source: Source) {
        binding.source = source
        val underlined =
            SpannableString(source.name).apply {
                setSpan(
                    UnderlineSpan(),
                    0,
                    this.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
                )
            }
        binding.tvDetailSource.text = underlined
    }

    companion object {
        fun create(
            parent: ViewGroup,
            clickListener: PlayerDetailClickListener,
        ): PlayerDetailSourceViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemSourceBinding.inflate(inflater, parent, false)
            return PlayerDetailSourceViewHolder(binding, clickListener)
        }
    }
}
