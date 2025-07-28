package com.onair.hearit.domain.model

import android.os.Bundle
import androidx.core.os.bundleOf

sealed class SearchInput {
    data class Keyword(
        val term: String,
    ) : SearchInput()

    data class Category(
        val id: Long,
        val name: String,
    ) : SearchInput()

    fun toBundle(): Bundle =
        when (this) {
            is Keyword -> bundleOf(KEYWORD_KEY to term)
            is Category ->
                bundleOf(
                    CATEGORY_ID_KEY to id,
                    CATEGORY_NAME_KEY to name,
                )
        }

    companion object {
        private const val KEYWORD_KEY = "keyword"
        private const val CATEGORY_ID_KEY = "categoryId"
        private const val CATEGORY_NAME_KEY = "categoryName"

        fun from(bundle: Bundle): SearchInput =
            when {
                bundle.containsKey(KEYWORD_KEY) ->
                    Keyword(bundle.getString(KEYWORD_KEY) ?: "")

                bundle.containsKey(CATEGORY_ID_KEY) && bundle.containsKey(CATEGORY_NAME_KEY) ->
                    Category(
                        bundle.getLong(CATEGORY_ID_KEY),
                        bundle.getString(CATEGORY_NAME_KEY) ?: "",
                    )

                else -> throw IllegalArgumentException("유효하지 않은 값입니다")
            }
    }
}
