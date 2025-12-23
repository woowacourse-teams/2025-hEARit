package com.onair.hearit.data.repository

import com.onair.hearit.data.datasource.remote.RecommendationRemoteDataSource
import com.onair.hearit.data.mapper.toDomain
import com.onair.hearit.data.toDomainResultList
import com.onair.hearit.domain.model.RecommendationCategories
import com.onair.hearit.domain.repository.RecommendationRepository
import javax.inject.Inject

class RecommendationRepositoryImpl @Inject constructor(
    private val recommendationRemoteDataSource: RecommendationRemoteDataSource,
) : RecommendationRepository {
    override suspend fun getRecommendationCategories(): Result<List<RecommendationCategories>> =
        recommendationRemoteDataSource
            .getRecommendationCategories()
            .toDomainResultList { it.toDomain() }
}
