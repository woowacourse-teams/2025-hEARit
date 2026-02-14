package com.onair.hearit.domain.repository

interface ExploreDataStoreRepository {
    suspend fun getExploreCount(): Result<Int>

    suspend fun updateExploreCount(count: Int): Result<Boolean>

    suspend fun clearExploreCount(): Result<Boolean>

    suspend fun shouldShowAnimation(): Result<Boolean>
}
