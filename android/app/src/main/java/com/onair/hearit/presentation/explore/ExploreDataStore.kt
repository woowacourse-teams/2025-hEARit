package com.onair.hearit.presentation.explore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "explore_prefs")

@Singleton
class ExploreDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val exploreCountKey = intPreferencesKey("explore_count")

    // 애니메이션 카운트
    val exploreCount: Flow<Int> =
        context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    Timber.e(exception, "Error reading explore_prefs")
                } else {
                    Timber.e(exception, "Unexpected error in explore_prefs")
                }
                emit(emptyPreferences())
            }.map { it[exploreCountKey] ?: 0 }

    suspend fun updateExploreCount(count: Int) {
        safeEdit { it[exploreCountKey] = count }
    }

    suspend fun clearExploreCount() {
        safeEdit { it.remove(exploreCountKey) }
    }

    // 애니메이션 로직 처리용
    suspend fun incrementCountIfUnder(max: Int): Boolean {
        var result = false
        safeEdit { prefs ->
            val current = prefs[exploreCountKey] ?: 0
            if (current < max) {
                prefs[exploreCountKey] = current + 1
                result = true
            } else {
                result = false
            }
        }
        return result
    }

    private suspend fun safeEdit(action: (MutablePreferences) -> Unit) {
        try {
            context.dataStore.edit { action(it) }
        } catch (e: Exception) {
            Timber.e(e, "Error saving to DataStore")
        }
    }

    suspend fun clearAll() {
        safeEdit { it.clear() }
    }
}
