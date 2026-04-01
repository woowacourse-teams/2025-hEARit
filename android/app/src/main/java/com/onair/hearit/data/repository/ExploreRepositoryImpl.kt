package com.onair.hearit.data.repository

import com.onair.hearit.data.datasource.local.ExploreLocalDataStore
import com.onair.hearit.domain.repository.ExploreRepository
import javax.inject.Inject

class ExploreRepositoryImpl @Inject constructor(
    private val exploreLocalDataSource: ExploreLocalDataStore,
) : ExploreRepository {
    override suspend fun getExploreCount(): Result<Int> = exploreLocalDataSource.getExploreCount()

    override suspend fun updateExploreCount(count: Int): Result<Boolean> = exploreLocalDataSource.updateExploreCount(count)

    override suspend fun clearExploreCount(): Result<Boolean> = exploreLocalDataSource.clearExploreCount()

    override suspend fun shouldShowAnimation(): Result<Boolean> = exploreLocalDataSource.shouldShowAnimation()
}
