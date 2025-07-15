package com.onair.hearit.presentation.home

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.R
import com.onair.hearit.databinding.ItemCategoryBinding
import com.onair.hearit.domain.CategoryItem

class CategoryAdapter : ListAdapter<CategoryItem, CategoryAdapter.CategoryViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): CategoryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemCategoryBinding.inflate(inflater, parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: CategoryViewHolder,
        position: Int,
    ) {
        holder.bind(getItem(position))
    }

    class CategoryViewHolder(
        private val binding: ItemCategoryBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CategoryItem) {
            binding.item = item
            binding.clCategory.setBackgroundResource(R.drawable.bg_purple3_radius_8dp)
            val background = binding.clCategory.background.mutate()
            if (background is GradientDrawable) {
                try {
                    val colorInt = item.color.toColorInt()
                    background.setColor(colorInt)
                } catch (e: IllegalArgumentException) {
                    // 파싱 실패 시 기본 색상 지정하거나 로그 출력
                    val context = binding.root.context
                    val defaultColor = ContextCompat.getColor(context, R.color.hearit_gray2)
                    background.setColor(defaultColor)
                }
            }
            binding.executePendingBindings()
        }
    }

    companion object {
        val DiffCallback =
            object : DiffUtil.ItemCallback<CategoryItem>() {
                override fun areItemsTheSame(
                    oldItem: CategoryItem,
                    newItem: CategoryItem,
                ): Boolean = oldItem.id == newItem.id

                override fun areContentsTheSame(
                    oldItem: CategoryItem,
                    newItem: CategoryItem,
                ): Boolean = oldItem == newItem
            }
    }
}
