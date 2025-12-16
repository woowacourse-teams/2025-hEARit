package com.onair.hearit.data.datasource.remote

import com.onair.hearit.data.api.RecommendationService
import com.onair.hearit.data.datasource.ErrorResponseHandler
import com.onair.hearit.data.datasource.NetworkResult
import com.onair.hearit.data.datasource.handleApiCall
import com.onair.hearit.data.dto.RecommendationCategoriesResponse
import javax.inject.Inject

class RecommendationRemoteDataSourceImpl @Inject constructor(
    private val recommendationService: RecommendationService,
    private val errorResponseHandler: ErrorResponseHandler,
) : RecommendationRemoteDataSource {
    override suspend fun getRecommendationCategories(): NetworkResult<List<RecommendationCategoriesResponse>> =
        handleApiCall(
            apiCall = { recommendationService.getRecommendationCategories() },
            errorHandler = errorResponseHandler,
        )
}
