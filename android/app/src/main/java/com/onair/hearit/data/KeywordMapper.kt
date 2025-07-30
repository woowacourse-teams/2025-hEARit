package com.onair.hearit.data

import com.onair.hearit.data.database.SearchHistoryEntity
import com.onair.hearit.domain.model.RecentSearch

fun SearchHistoryEntity.toDomain(): RecentSearch =
    RecentSearch(
        term = this.term,
        searchedAt = this.searchedAt,
    )

fun RecentSearch.toData(): SearchHistoryEntity =
    SearchHistoryEntity(
        term = this.term,
        searchedAt = this.searchedAt,
    )
