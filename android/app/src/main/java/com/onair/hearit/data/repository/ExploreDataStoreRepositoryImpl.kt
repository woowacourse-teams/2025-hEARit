package com.onair.hearit.data.repository

import com.onair.hearit.domain.repository.ExploreDataStoreRepository
import com.onair.hearit.presentation.explore.ExploreDataStore
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ExploreDataStoreRepositoryImpl @Inject constructor(
    private val exploreDataStore: ExploreDataStore,
) : ExploreDataStoreRepository {
    override suspend fun getExploreCount(): Result<Int> = runCatching { exploreDataStore.exploreCount.first() }

    override suspend fun updateExploreCount(count: Int): Result<Boolean> =
        runCatching {
            exploreDataStore.updateExploreCount(count)
            true
        }

    override suspend fun clearExploreCount(): Result<Boolean> =
        runCatching {
            exploreDataStore.clearExploreCount()
            true
        }

    override suspend fun shouldShowAnimation(): Result<Boolean> =
        runCatching {
            exploreDataStore.incrementCountIfUnder(MAX_ANIMATION_COUNT)
        }

    override suspend fun getLastCursorId(): Result<Long?> = runCatching { exploreDataStore.lastCursorId.first() }

    override suspend fun saveLastCursorId(cursorId: Long): Result<Boolean> =
        runCatching {
            exploreDataStore.saveLastCursorId(cursorId)
            true
        }

    override suspend fun getLastPosition(): Result<Long> = runCatching { exploreDataStore.lastPosition.first() }

    override suspend fun saveLastPosition(position: Long): Result<Boolean> =
        runCatching {
            exploreDataStore.saveLastPosition(position)
            true
        }

    companion object {
        private const val MAX_ANIMATION_COUNT = 2
    }
}
