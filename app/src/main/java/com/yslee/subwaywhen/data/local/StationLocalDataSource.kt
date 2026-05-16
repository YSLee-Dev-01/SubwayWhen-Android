package com.yslee.subwaywhen.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.yslee.subwaywhen.data.model.SaveStation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StationLocalDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    fun getSaveStations(): Flow<List<SaveStation>> = dataStore.data
        .map { prefs ->
            val json = prefs[PreferencesKeys.SAVE_STATIONS_JSON] ?: return@map emptyList()
            try {
                Json.decodeFromString<List<SaveStation>>(json)
            } catch (e: Exception) {
                emptyList()
            }
        }
        .catch { emit(emptyList()) }

    suspend fun updateSaveStations(list: List<SaveStation>) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.SAVE_STATIONS_JSON] = Json.encodeToString(list)
        }
    }
}
