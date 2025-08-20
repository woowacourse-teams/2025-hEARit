package com.onair.hearit.presentation

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import java.util.UUID

object UserIdManager {
    private val Context.dataStore by preferencesDataStore("app_prefs")
    private val USER_ID_KEY = stringPreferencesKey("user_id")

    suspend fun getOrCreateUserId(context: Context): String {
        val prefs = context.dataStore.data.first()
        val existing = prefs[USER_ID_KEY]

        return if (existing != null) {
            existing
        } else {
            val newId = UUID.randomUUID().toString()
            context.dataStore.edit { it[USER_ID_KEY] = newId }
            newId
        }
    }
}
