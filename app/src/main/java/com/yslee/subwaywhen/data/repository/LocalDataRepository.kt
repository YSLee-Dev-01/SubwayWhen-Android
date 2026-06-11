package com.yslee.subwaywhen.data.repository

import com.yslee.subwaywhen.data.model.SaveSetting
import com.yslee.subwaywhen.data.model.SaveStation
import kotlinx.coroutines.flow.StateFlow

interface LocalDataRepository {
    val saveSetting: StateFlow<SaveSetting>
    val saveStations: StateFlow<List<SaveStation>>
    val isInitialized: StateFlow<Boolean>
    suspend fun updateSaveSetting(setting: SaveSetting)
    suspend fun updateSaveStations(stations: List<SaveStation>)
    suspend fun deleteShinbundangSchedule(stationName: String)
}
