package com.onair.hearit.domain.repository

import com.onair.hearit.domain.model.Keyword

interface KeywordRepository {
    suspend fun getRecommendKeywords(size: Int? = null): Result<List<Keyword>>
}
