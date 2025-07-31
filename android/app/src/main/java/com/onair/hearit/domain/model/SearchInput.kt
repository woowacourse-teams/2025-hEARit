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
            is Keyword -> bundleOf(TYPE_KEY to "keyword", KEYWORD_KEY to this.term)
            is Category ->
                bundleOf(
                    TYPE_KEY to "category",
                    CATEGORY_ID_KEY to this.id,
                    CATEGORY_NAME_KEY to this.name,
                )
        }

    companion object {
        private const val TYPE_KEY = "type"
        private const val KEYWORD_KEY = "keyword"
        private const val CATEGORY_ID_KEY = "categoryId"
        private const val CATEGORY_NAME_KEY = "categoryName"
        private const val ERROR_INVALID_TERM_MESSAGE = "유효하지 않은 검색어입니다"

        fun from(bundle: Bundle): SearchInput =
            when (bundle.getString(TYPE_KEY)) {
                "keyword" -> Keyword(bundle.getString(KEYWORD_KEY).orEmpty())

                "category" -> {
                    val id = bundle.getLong(CATEGORY_ID_KEY)
                    val name = bundle.getString(CATEGORY_NAME_KEY) ?: ""
                    Category(id, name)
                }

                else -> throw IllegalArgumentException(ERROR_INVALID_TERM_MESSAGE)
            }
    }
}
