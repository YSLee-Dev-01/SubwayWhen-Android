package com.yslee.subwaywhen.core

import com.yslee.subwaywhen.data.local.SettingLocalDataSource
import com.yslee.subwaywhen.data.local.StationLocalDataSource
import com.yslee.subwaywhen.data.model.SaveSetting
import com.yslee.subwaywhen.data.model.SaveStation
import com.yslee.subwaywhen.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FixInfo @Inject constructor(
    private val settingDataSource: SettingLocalDataSource,
    private val stationDataSource: StationLocalDataSource,
    @ApplicationScope private val applicationScope: CoroutineScope,
) {
    private val _saveSetting = MutableStateFlow(SaveSetting())
    val saveSetting: StateFlow<SaveSetting> = _saveSetting.asStateFlow()

    private val _saveStations = MutableStateFlow<List<SaveStation>>(emptyList())
    val saveStations: StateFlow<List<SaveStation>> = _saveStations.asStateFlow()

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    suspend fun initialize() {
        _saveSetting.value = settingDataSource.getSaveSetting().first()
        _saveStations.value = stationDataSource.getSaveStations().first()
        _isInitialized.value = true
    }

    suspend fun updateSaveSetting(new: SaveSetting) {
        _saveSetting.value = new
        settingDataSource.updateSaveSetting(new)
    }

    suspend fun updateSaveStations(list: List<SaveStation>) {
        _saveStations.value = list
        stationDataSource.updateSaveStations(list)
    }
}
