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

    companion object {
        private const val MAX_ANIMATION_COUNT = 2
    }
}
