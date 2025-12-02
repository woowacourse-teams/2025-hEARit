package com.onair.hearit.data.datasource.remote

import com.onair.hearit.data.api.HearitService
import com.onair.hearit.data.datasource.ApiErrorMessages.ERROR_RESPONSE_BODY_NULL_MESSAGE
import com.onair.hearit.data.datasource.ErrorResponseHandler
import com.onair.hearit.data.datasource.NetworkResult
import com.onair.hearit.data.datasource.handleApiCall
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
    override suspend fun getHearit(hearitId: Long): Result<NetworkResult<HearitResponse>> =
        handleApiCall(
            apiCall = { hearitService.getHearit(hearitId) },
            transform = { response ->
                response.body() ?: throw IllegalStateException(ERROR_RESPONSE_BODY_NULL_MESSAGE)
            },
            errorHandler = errorResponseHandler,
        )

    override suspend fun getRecommendHearits(): Result<NetworkResult<List<RecommendHearitResponse>>> =
        handleApiCall(
            apiCall = { hearitService.getRecommendHearits() },
            transform = { response ->
                response.body() ?: throw IllegalStateException(ERROR_RESPONSE_BODY_NULL_MESSAGE)
            },
            errorHandler = errorResponseHandler,
        )

    override suspend fun getExploreHearits(
        cursorId: Long?,
        size: Int?,
    ): Result<NetworkResult<ExploreHearitResponse>> =
        handleApiCall(
            apiCall = { hearitService.getExploreHearits(cursorId, size) },
            transform = { response ->
                response.body() ?: throw IllegalStateException(ERROR_RESPONSE_BODY_NULL_MESSAGE)
            },
            errorHandler = errorResponseHandler,
        )

    override suspend fun getSearchHearits(
        searchTerm: String,
        page: Int?,
        size: Int?,
    ): Result<NetworkResult<SearchHearitsResponse>> =
        handleApiCall(
            apiCall = { hearitService.getSearchHearits(searchTerm, page, size) },
            transform = { response ->
                response.body() ?: throw IllegalStateException(ERROR_RESPONSE_BODY_NULL_MESSAGE)
            },
            errorHandler = errorResponseHandler,
        )

    override suspend fun getHearits(
        categoryId: Long?,
        page: Int?,
        size: Int?,
    ): Result<NetworkResult<HearitsResponse>> =
        handleApiCall(
            apiCall = {
                hearitService.getHearits(
                    categoryId = categoryId,
                    page = page,
                    size = size,
                )
            },
            transform = { response ->
                response.body() ?: throw IllegalStateException(ERROR_RESPONSE_BODY_NULL_MESSAGE)
            },
            errorHandler = errorResponseHandler,
        )

    override suspend fun getRecommendationCategoryHearits(): Result<NetworkResult<List<RecommendationCategoriesResponse>>> =
        handleApiCall(
            apiCall = { hearitService.getCategoryHearits() },
            transform = { response ->
                response.body() ?: throw IllegalStateException(ERROR_RESPONSE_BODY_NULL_MESSAGE)
            },
            errorHandler = errorResponseHandler,
        )
}
