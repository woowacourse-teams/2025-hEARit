package com.onair.hearit.data.datasource.remote

import com.onair.hearit.data.api.LikeService
import com.onair.hearit.data.datasource.ErrorResponseHandler
import com.onair.hearit.data.datasource.NetworkResult
import com.onair.hearit.data.datasource.handleApiCall
import javax.inject.Inject

class LikeRemoteDataSourceImpl @Inject constructor(
    private val likeService: LikeService,
    private val errorResponseHandler: ErrorResponseHandler,
) : LikeRemoteDataSource {
    override suspend fun addLike(hearitId: Long): NetworkResult<Unit> =
        handleApiCall(
            apiCall = { likeService.postLike(hearitId) },
            errorHandler = errorResponseHandler,
        )

    override suspend fun deleteLike(hearitId: Long): NetworkResult<Unit> =
        handleApiCall(
            apiCall = { likeService.deleteLike(hearitId) },
            errorHandler = errorResponseHandler,
        )
}
