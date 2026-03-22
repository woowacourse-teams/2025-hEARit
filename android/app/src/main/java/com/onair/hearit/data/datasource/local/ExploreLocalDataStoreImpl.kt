package com.onair.hearit.data.datasource.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import com.onair.hearit.di.ExplorePreferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExploreLocalDataStoreImpl @Inject constructor(
    @ExplorePreferencesDataStore
    private val dataStore: DataStore<Preferences>,
) : ExploreLocalDataStore {
    private val exploreCountKey = intPreferencesKey("explore_count")

    private val exploreCount: Flow<Int> =
        dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    Timber.e(exception, "Error reading explore_prefs")
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }.map { it[exploreCountKey] ?: 0 }

    override suspend fun getExploreCount(): Result<Int> =
        runCatching {
            exploreCount.first()
        }

    override suspend fun updateExploreCount(count: Int): Result<Boolean> =
        runCatching {
            dataStore.edit { it[exploreCountKey] = count }
            true
        }

    override suspend fun clearExploreCount(): Result<Boolean> =
        runCatching {
            dataStore.edit { it.remove(exploreCountKey) }
            true
        }

    override suspend fun shouldShowAnimation(): Result<Boolean> =
        runCatching {
            var result = false
            dataStore.edit { prefs ->
                val current = prefs[exploreCountKey] ?: 0
                if (current < MAX_ANIMATION_COUNT) {
                    prefs[exploreCountKey] = current + 1
                    result = true
                } else {
                    result = false
                }
            }
            result
        }

    companion object {
        private const val MAX_ANIMATION_COUNT = 2
    }
}
