package com.onair.hearit.data.repository

import com.onair.hearit.data.datasource.remote.LikeRemoteDataSource
import com.onair.hearit.data.toDomainResult
import com.onair.hearit.domain.repository.LikeRepository
import javax.inject.Inject

class LikeRepositoryImpl @Inject constructor(
    private val likeRemoteDataSource: LikeRemoteDataSource,
) : LikeRepository {
    override suspend fun addLike(hearitId: Long): Result<Unit> = likeRemoteDataSource.addLike(hearitId).toDomainResult()

    override suspend fun deleteLike(hearitId: Long): Result<Unit> = likeRemoteDataSource.deleteLike(hearitId).toDomainResult()
}
