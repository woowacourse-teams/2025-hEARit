package com.onair.hearit.data.datasource.remote

import com.onair.hearit.data.datasource.NetworkResult

interface LikeRemoteDataSource {
    suspend fun addLike(hearitId: Long): NetworkResult<Unit>

    suspend fun deleteLike(hearitId: Long): NetworkResult<Unit>
}
