package com.onair.hearit.presentation.explore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
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
    // 저장할 키값 정의
    private val KEY_LAST_CURSOR_ID = longPreferencesKey("last_cursor_id")
    private val KEY_LAST_POSITION = longPreferencesKey("last_position")
    private val EXPLORE_COUNT = intPreferencesKey("explore_count")

    // 마지막으로 본 인덱스
    val lastCursorId: Flow<Long?> =
        context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    Timber.e(exception, "Error reading explore_prefs")
                } else {
                    Timber.e(exception, "Unexpected error in explore_prefs")
                }
                emit(emptyPreferences())
            }.map { it[KEY_LAST_CURSOR_ID] ?: 0 }

    // 마지막 재생 위치
    val lastPosition: Flow<Long> =
        context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    Timber.e(exception, "Error reading explore_prefs")
                } else {
                    Timber.e(exception, "Unexpected error in explore_prefs")
                }
                emit(emptyPreferences())
            }.map { it[KEY_LAST_POSITION] ?: 0L }

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
            }.map { it[EXPLORE_COUNT] ?: 0 }

    suspend fun saveLastCursorId(cursorId: Long) {
        safeEdit { it[KEY_LAST_CURSOR_ID] = cursorId }
    }

    suspend fun saveLastPosition(position: Long) {
        safeEdit { it[KEY_LAST_POSITION] = position }
    }

    suspend fun updateExploreCount(count: Int) {
        safeEdit { it[EXPLORE_COUNT] = count }
    }

    suspend fun clearExploreCount() {
        safeEdit { it.remove(EXPLORE_COUNT) }
    }

    // 애니메이션 로직 처리용
    suspend fun incrementCountIfUnder(max: Int): Boolean {
        var result = false
        safeEdit { prefs ->
            val current = prefs[EXPLORE_COUNT] ?: 0
            if (current < max) {
                prefs[EXPLORE_COUNT] = current + 1
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
