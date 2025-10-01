package com.onair.hearit.data.datasource.remote

import com.onair.hearit.data.api.HearitService
import com.onair.hearit.data.datasource.ApiErrorMessages.ERROR_RESPONSE_BODY_NULL_MESSAGE
import com.onair.hearit.data.datasource.ErrorResponseHandler
import com.onair.hearit.data.datasource.NetworkResult
import com.onair.hearit.data.datasource.handleApiCall
import com.onair.hearit.data.dto.GroupedCategoryHearitResponse
import com.onair.hearit.data.dto.HearitResponse
import com.onair.hearit.data.dto.PlayingBookmarkResponse
import com.onair.hearit.data.dto.RandomHearitResponse
import com.onair.hearit.data.dto.RecentUploadResponse
import com.onair.hearit.data.dto.RecommendHearitResponse
import com.onair.hearit.data.dto.SearchHearitResponse

class HearitRemoteDataSourceImpl(
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

    override suspend fun getRecentUploadHearits(): Result<NetworkResult<List<RecentUploadResponse>>> =
        handleApiCall(
            apiCall = { hearitService.getRecentUploadHearits() },
            transform = { response ->
                response.body() ?: throw IllegalStateException(ERROR_RESPONSE_BODY_NULL_MESSAGE)
            },
            errorHandler = errorResponseHandler,
        )

    override suspend fun getPlayingBookmarkHearits(): Result<NetworkResult<List<PlayingBookmarkResponse>>> =
        handleApiCall(
            apiCall = { hearitService.getPlayingBookmarkHearits() },
            transform = { response ->
                response.body() ?: throw IllegalStateException(ERROR_RESPONSE_BODY_NULL_MESSAGE)
            },
            errorHandler = errorResponseHandler,
        )

    override suspend fun getRandomHearits(
        cursorId: Long?,
        size: Int?,
    ): Result<NetworkResult<RandomHearitResponse>> =
        handleApiCall(
            apiCall = { hearitService.getRandomHearits(cursorId, size) },
            transform = { response ->
                response.body() ?: throw IllegalStateException(ERROR_RESPONSE_BODY_NULL_MESSAGE)
            },
            errorHandler = errorResponseHandler,
        )

    override suspend fun getSearchHearits(
        searchTerm: String,
        page: Int?,
        size: Int?,
    ): Result<NetworkResult<SearchHearitResponse>> =
        handleApiCall(
            apiCall = { hearitService.getSearchHearits(searchTerm, page, size) },
            transform = { response ->
                response.body() ?: throw IllegalStateException(ERROR_RESPONSE_BODY_NULL_MESSAGE)
            },
            errorHandler = errorResponseHandler,
        )

    override suspend fun getCategoryHearits(): Result<NetworkResult<List<GroupedCategoryHearitResponse>>> =
        handleApiCall(
            apiCall = { hearitService.getCategoryHearits() },
            transform = { response ->
                response.body() ?: throw IllegalStateException(ERROR_RESPONSE_BODY_NULL_MESSAGE)
            },
            errorHandler = errorResponseHandler,
        )
}
