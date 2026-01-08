package com.onair.hearit.presentation.search

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

sealed interface SearchRoute {
    @Serializable
    data object SearchMain : SearchRoute

    @Serializable
    data class Category(
        val id: Long,
        val name: String,
        val colorCode: String,
    ) : SearchRoute

    @Serializable
    object SearchDetail : SearchRoute
}

@Immutable
@Serializable
data class SearchStartArgs(
    val initialCategory: SearchRoute.Category? = null,
    val isDirectEntry: Boolean = false,
)
