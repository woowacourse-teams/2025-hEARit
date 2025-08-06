package com.onair.hearit.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.onair.hearit.data.exploreDataStore
import com.onair.hearit.domain.repository.ExploreDataStoreRepository
import kotlinx.coroutines.flow.first

class ExploreDataStoreRepositoryImpl(
    context: Context,
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
                preferences.clear()
            }
            true
        }

    companion object {
        private val EXPLORE_COUNT = intPreferencesKey("explore_count")
    }
}
