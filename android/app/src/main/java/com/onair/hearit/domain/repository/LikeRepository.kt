package com.onair.hearit.domain.repository

interface LikeRepository {
    suspend fun addLike(hearitId: Long): Result<Unit>

    suspend fun deleteLike(hearitId: Long): Result<Unit>
}
