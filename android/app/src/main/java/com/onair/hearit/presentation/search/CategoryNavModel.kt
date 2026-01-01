package com.onair.hearit.presentation.search

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CategoryNavModel(
    val id: Long,
    val name: String,
    val colorCode: String,
) : Parcelable

@Parcelize
data class SearchStartArgs(
    val initialCategory: CategoryNavModel? = null,
    val isDirectEntry: Boolean = false,
) : Parcelable
