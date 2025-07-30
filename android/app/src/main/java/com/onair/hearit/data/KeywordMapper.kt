package com.onair.hearit.data

import com.onair.hearit.data.database.RecentKeywordEntity
import com.onair.hearit.domain.model.RecentKeyword

fun RecentKeywordEntity.toDomain(): RecentKeyword =
    RecentKeyword(
        term = this.term,
        searchedAt = this.searchedAt,
    )

fun RecentKeyword.toData(): RecentKeywordEntity =
    RecentKeywordEntity(
        term = this.term,
        searchedAt = this.searchedAt,
    )
