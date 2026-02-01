package com.onair.hearit.domain.repository

interface ExploreDataStoreRepository {
    suspend fun getExploreCount(): Result<Int>

    suspend fun updateExploreCount(count: Int): Result<Boolean>

    suspend fun clearExploreCount(): Result<Boolean>

    suspend fun shouldShowAnimation(): Result<Boolean>

    suspend fun getLastPosition(): Result<Long>

    suspend fun saveLastPosition(position: Long): Result<Boolean>

    suspend fun saveLastCursorId(cursorId: Long): Result<Boolean>

    suspend fun getLastCursorId(): Result<Long?>
}
