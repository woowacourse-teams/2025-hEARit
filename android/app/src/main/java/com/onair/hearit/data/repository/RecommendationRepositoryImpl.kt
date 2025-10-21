package com.onair.hearit.data.repository

import com.onair.hearit.data.datasource.remote.RecommendationRemoteDataSource
import com.onair.hearit.data.mapper.toDomain
import com.onair.hearit.domain.model.RecommendationCategories
import com.onair.hearit.domain.repository.RecommendationRepository
import javax.inject.Inject

class RecommendationRepositoryImpl @Inject constructor(
    private val recommendationDataSource: RecommendationRemoteDataSource,
) : RecommendationRepository {
    override suspend fun getRecommendationCategories(): Result<List<RecommendationCategories>> =
        recommendationDataSource
            .getRecommendationCategories()
            .mapListOrThrowDomain { it.toDomain() }
}
