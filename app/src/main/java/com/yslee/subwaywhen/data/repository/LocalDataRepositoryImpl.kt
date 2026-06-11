package com.yslee.subwaywhen.data.repository

import com.yslee.subwaywhen.core.FixInfo
import com.yslee.subwaywhen.data.local.room.ShinbundangScheduleDao
import com.yslee.subwaywhen.data.model.SaveSetting
import com.yslee.subwaywhen.data.model.SaveStation
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalDataRepositoryImpl @Inject constructor(
    private val fixInfo: FixInfo,
    private val shinbundangScheduleDao: ShinbundangScheduleDao,
) : LocalDataRepository {

    override val saveSetting: StateFlow<SaveSetting>
        get() = fixInfo.saveSetting

    override val saveStations: StateFlow<List<SaveStation>>
        get() = fixInfo.saveStations

    override val isInitialized: StateFlow<Boolean>
        get() = fixInfo.isInitialized

    override suspend fun updateSaveSetting(setting: SaveSetting) {
        fixInfo.updateSaveSetting(setting)
    }

    override suspend fun updateSaveStations(stations: List<SaveStation>) {
        fixInfo.updateSaveStations(stations)
    }

    override suspend fun deleteShinbundangSchedule(stationName: String) {
        shinbundangScheduleDao.delete(stationName)
    }
}
