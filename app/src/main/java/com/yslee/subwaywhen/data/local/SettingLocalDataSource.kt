package com.yslee.subwaywhen.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.yslee.subwaywhen.data.model.HolidayData
import com.yslee.subwaywhen.data.model.SaveSetting
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingLocalDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    fun getSaveSetting(): Flow<SaveSetting> = dataStore.data
        .map { prefs ->
            SaveSetting(
                mainCongestionLabel = prefs[PreferencesKeys.MAIN_CONGESTION_LABEL] ?: "☹️",
                mainGroupOneTime = prefs[PreferencesKeys.MAIN_GROUP_ONE_TIME] ?: 0,
                mainGroupTwoTime = prefs[PreferencesKeys.MAIN_GROUP_TWO_TIME] ?: 0,
                detailAutoReload = prefs[PreferencesKeys.DETAIL_AUTO_RELOAD] ?: true,
                detailScheduleAutoTime = prefs[PreferencesKeys.DETAIL_SCHEDULE_AUTO_TIME] ?: true,
                searchOverlapAlert = prefs[PreferencesKeys.SEARCH_OVERLAP_ALERT] ?: true,
                tutorialSuccess = prefs[PreferencesKeys.TUTORIAL_SUCCESS] ?: false,
                detailVcTrainIcon = prefs[PreferencesKeys.DETAIL_VC_TRAIN_ICON] ?: "🚃",
                isWeekendNotificationEnabled = prefs[PreferencesKeys.IS_WEEKEND_NOTIFICATION_ENABLED] ?: true,
                mainCongestionBaseStation = prefs[PreferencesKeys.MAIN_CONGESTION_BASE_STATION] ?: "강남",
                alertGroupOneId = prefs[PreferencesKeys.ALERT_GROUP_ONE_ID] ?: "",
                alertGroupTwoId = prefs[PreferencesKeys.ALERT_GROUP_TWO_ID] ?: "",
            )
        }
        .catch { emit(SaveSetting()) }

    suspend fun updateSaveSetting(setting: SaveSetting) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.MAIN_CONGESTION_LABEL] = setting.mainCongestionLabel
            prefs[PreferencesKeys.MAIN_GROUP_ONE_TIME] = setting.mainGroupOneTime
            prefs[PreferencesKeys.MAIN_GROUP_TWO_TIME] = setting.mainGroupTwoTime
            prefs[PreferencesKeys.DETAIL_AUTO_RELOAD] = setting.detailAutoReload
            prefs[PreferencesKeys.DETAIL_SCHEDULE_AUTO_TIME] = setting.detailScheduleAutoTime
            prefs[PreferencesKeys.SEARCH_OVERLAP_ALERT] = setting.searchOverlapAlert
            prefs[PreferencesKeys.TUTORIAL_SUCCESS] = setting.tutorialSuccess
            prefs[PreferencesKeys.DETAIL_VC_TRAIN_ICON] = setting.detailVcTrainIcon
            prefs[PreferencesKeys.IS_WEEKEND_NOTIFICATION_ENABLED] = setting.isWeekendNotificationEnabled
            prefs[PreferencesKeys.MAIN_CONGESTION_BASE_STATION] = setting.mainCongestionBaseStation
            prefs[PreferencesKeys.ALERT_GROUP_ONE_ID] = setting.alertGroupOneId
            prefs[PreferencesKeys.ALERT_GROUP_TWO_ID] = setting.alertGroupTwoId
        }
    }

    suspend fun updateTutorialSeen(value: Boolean) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.TUTORIAL_SUCCESS] = value
        }
    }

    suspend fun getHolidayData(): HolidayData {
        val prefs = dataStore.data.first()
        val version = prefs[PreferencesKeys.HOLIDAY_VERSION] ?: 0
        val list = prefs[PreferencesKeys.HOLIDAY_LIST]?.toList() ?: emptyList()
        return HolidayData(version = version, list = list)
    }

    suspend fun saveHolidayData(data: HolidayData) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.HOLIDAY_VERSION] = data.version
            prefs[PreferencesKeys.HOLIDAY_LIST] = data.list.toSet()
        }
    }
}
