package com.yslee.subwaywhen.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.yslee.subwaywhen.data.model.SaveSetting
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingLocalDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        val MAIN_CONGESTION_LABEL = stringPreferencesKey("main_congestion_label")
        val MAIN_GROUP_ONE_TIME = intPreferencesKey("main_group_one_time")
        val MAIN_GROUP_TWO_TIME = intPreferencesKey("main_group_two_time")
        val DETAIL_AUTO_RELOAD = booleanPreferencesKey("detail_auto_reload")
        val DETAIL_SCHEDULE_AUTO_TIME = booleanPreferencesKey("detail_schedule_auto_time")
        val LIVE_ACTIVITY = booleanPreferencesKey("live_activity")
        val SEARCH_OVERLAP_ALERT = booleanPreferencesKey("search_overlap_alert")
        val ALERT_GROUP_ONE_ID = stringPreferencesKey("alert_group_one_id")
        val ALERT_GROUP_TWO_ID = stringPreferencesKey("alert_group_two_id")
        val TUTORIAL_SUCCESS = booleanPreferencesKey("tutorial_success")
        val DETAIL_VC_TRAIN_ICON = stringPreferencesKey("detail_vc_train_icon")
        val IS_WEEKEND_NOTIFICATION_ENABLED = booleanPreferencesKey("is_weekend_notification_enabled")
        val MAIN_CONGESTION_BASE_STATION = stringPreferencesKey("main_congestion_base_station")
    }

    fun getSaveSetting(): Flow<SaveSetting> = dataStore.data
        .map { prefs ->
            SaveSetting(
                mainCongestionLabel = prefs[MAIN_CONGESTION_LABEL] ?: "☹️",
                mainGroupOneTime = prefs[MAIN_GROUP_ONE_TIME] ?: 0,
                mainGroupTwoTime = prefs[MAIN_GROUP_TWO_TIME] ?: 0,
                detailAutoReload = prefs[DETAIL_AUTO_RELOAD] ?: true,
                detailScheduleAutoTime = prefs[DETAIL_SCHEDULE_AUTO_TIME] ?: true,
                liveActivity = prefs[LIVE_ACTIVITY] ?: true,
                searchOverlapAlert = prefs[SEARCH_OVERLAP_ALERT] ?: true,
                alertGroupOneId = prefs[ALERT_GROUP_ONE_ID] ?: "",
                alertGroupTwoId = prefs[ALERT_GROUP_TWO_ID] ?: "",
                tutorialSuccess = prefs[TUTORIAL_SUCCESS] ?: false,
                detailVcTrainIcon = prefs[DETAIL_VC_TRAIN_ICON] ?: "🚃",
                isWeekendNotificationEnabled = prefs[IS_WEEKEND_NOTIFICATION_ENABLED] ?: true,
                mainCongestionBaseStation = prefs[MAIN_CONGESTION_BASE_STATION] ?: "강남",
            )
        }
        .catch { emit(SaveSetting()) }

    suspend fun updateTutorialSeen(value: Boolean) {
        dataStore.edit { prefs ->
            prefs[TUTORIAL_SUCCESS] = value
        }
    }
}
