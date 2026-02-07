package com.onair.hearit.data.datasource.remote

import com.onair.hearit.data.api.HearitService
import com.onair.hearit.data.datasource.ErrorResponseHandler
import com.onair.hearit.data.datasource.NetworkResult
import com.onair.hearit.data.datasource.handleApiCall
import com.onair.hearit.data.datasource.handleApiCallUnit
import com.onair.hearit.data.dto.ExploreHearitResponse
import com.onair.hearit.data.dto.HearitResponse
import com.onair.hearit.data.dto.HearitsResponse
import com.onair.hearit.data.dto.RecommendHearitResponse
import com.onair.hearit.data.dto.RecommendationCategoriesResponse
import com.onair.hearit.data.dto.SearchHearitsResponse
import javax.inject.Inject

class HearitRemoteDataSourceImpl @Inject constructor(
    private val hearitService: HearitService,
    private val errorResponseHandler: ErrorResponseHandler,
) : HearitRemoteDataSource {
    override suspend fun getHearit(hearitId: Long): NetworkResult<HearitResponse> =
        handleApiCall(
            apiCall = { hearitService.getHearit(hearitId) },
            errorHandler = errorResponseHandler,
        )

    override suspend fun getRecommendHearits(): NetworkResult<List<RecommendHearitResponse>> =
        handleApiCall(
            apiCall = { hearitService.getRecommendHearits() },
            errorHandler = errorResponseHandler,
        )

    override suspend fun getExploreHearits(
        cursorId: Long?,
        size: Int?,
    ): NetworkResult<ExploreHearitResponse> =
        handleApiCall(
            apiCall = { hearitService.getExploreHearits(cursorId, size) },
            errorHandler = errorResponseHandler,
        )

    override suspend fun getSearchHearits(
        searchTerm: String,
        page: Int?,
        size: Int?,
    ): NetworkResult<SearchHearitsResponse> =
        handleApiCall(
            apiCall = { hearitService.getSearchHearits(searchTerm, page, size) },
            errorHandler = errorResponseHandler,
        )

    override suspend fun getHearits(
        categoryId: Long?,
        page: Int?,
        size: Int?,
    ): NetworkResult<HearitsResponse> =
        handleApiCall(
            apiCall = {
                hearitService.getHearits(
                    categoryId = categoryId,
                    page = page,
                    size = size,
                )
            },
            errorHandler = errorResponseHandler,
        )

    override suspend fun getRecommendationCategoryHearits(): NetworkResult<List<RecommendationCategoriesResponse>> =
        handleApiCall(
            apiCall = { hearitService.getCategoryHearits() },
            errorHandler = errorResponseHandler,
        )

    override suspend fun postHearitView(hearitId: Long): NetworkResult<Unit> =
        handleApiCallUnit(
            apiCall = { hearitService.postHearitView(hearitId) },
            errorHandler = errorResponseHandler,
        )
}
