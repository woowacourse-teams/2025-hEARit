package com.onair.hearit.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.onair.hearit.data.exploreDataStore
import com.onair.hearit.domain.repository.ExploreDataStoreRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ExploreDataStoreRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context,
) : ExploreDataStoreRepository {
    private val exploreDataStore: DataStore<Preferences> = context.exploreDataStore

    override suspend fun getExploreCount(): Result<Int> =
        runCatching {
            val preferences = exploreDataStore.data.first()
            preferences[EXPLORE_COUNT] ?: 0
        }

    override suspend fun updateExploreCount(count: Int): Result<Boolean> =
        runCatching {
            exploreDataStore.edit { preferences ->
                preferences[EXPLORE_COUNT] = count
            }
            true
        }

    override suspend fun clearExploreCount(): Result<Boolean> =
        runCatching {
            exploreDataStore.edit { preferences ->
                preferences.remove(EXPLORE_COUNT)
            }
            true
        }

    override suspend fun shouldShowAnimation(): Result<Boolean> =
        runCatching {
            var shouldShow = false

            exploreDataStore.edit { preferences ->
                val current = preferences[EXPLORE_COUNT] ?: 0
                shouldShow = current < MAX_ANIMATION_COUNT
                if (shouldShow) {
                    preferences[EXPLORE_COUNT] = current + 1
                }
            }
            shouldShow
        }

    companion object {
        private val EXPLORE_COUNT = intPreferencesKey("explore_count")
        private const val MAX_ANIMATION_COUNT = 2
    }
}
