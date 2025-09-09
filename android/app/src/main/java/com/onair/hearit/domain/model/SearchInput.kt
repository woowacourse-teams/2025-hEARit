package com.onair.hearit.domain.model

import android.os.Bundle
import androidx.core.os.bundleOf
import com.onair.hearit.presentation.IntentKeys.CATEGORY_ID_KEY
import com.onair.hearit.presentation.IntentKeys.CATEGORY_KEY
import com.onair.hearit.presentation.IntentKeys.CATEGORY_NAME_KEY
import com.onair.hearit.presentation.IntentKeys.KEYWORD_KEY
import com.onair.hearit.presentation.IntentKeys.TYPE_KEY
import com.onair.hearit.presentation.IntentValues.CATEGORY_VALUE
import com.onair.hearit.presentation.IntentValues.KEYWORD_VALUE

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
            is Keyword -> bundleOf(TYPE_KEY to KEYWORD_VALUE, KEYWORD_KEY to this.term)
            is Category ->
                bundleOf(
                    TYPE_KEY to CATEGORY_VALUE,
                    CATEGORY_ID_KEY to this.id,
                    CATEGORY_NAME_KEY to this.name,
                )
        }

    companion object {
        private const val ERROR_INVALID_TERM_MESSAGE = "유효하지 않은 검색어입니다"

        fun from(bundle: Bundle): SearchInput =
            when (bundle.getString(TYPE_KEY)) {
                KEYWORD_KEY -> Keyword(bundle.getString(KEYWORD_KEY).orEmpty())

                CATEGORY_KEY -> {
                    val id = bundle.getLong(CATEGORY_ID_KEY)
                    val name = bundle.getString(CATEGORY_NAME_KEY) ?: ""
                    Category(id, name)
                }

                else -> throw IllegalArgumentException(ERROR_INVALID_TERM_MESSAGE)
            }
    }
}
