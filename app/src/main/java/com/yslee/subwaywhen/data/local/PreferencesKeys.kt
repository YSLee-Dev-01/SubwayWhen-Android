package com.yslee.subwaywhen.data.local

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey

object PreferencesKeys {
    val MAIN_CONGESTION_LABEL = stringPreferencesKey("main_congestion_label")
    val MAIN_GROUP_ONE_TIME = intPreferencesKey("main_group_one_time")
    val MAIN_GROUP_TWO_TIME = intPreferencesKey("main_group_two_time")
    val DETAIL_AUTO_RELOAD = booleanPreferencesKey("detail_auto_reload")
    val DETAIL_SCHEDULE_AUTO_TIME = booleanPreferencesKey("detail_schedule_auto_time")
    val SEARCH_OVERLAP_ALERT = booleanPreferencesKey("search_overlap_alert")
    val TUTORIAL_SUCCESS = booleanPreferencesKey("tutorial_success")
    val DETAIL_VC_TRAIN_ICON = stringPreferencesKey("detail_vc_train_icon")
    val IS_WEEKEND_NOTIFICATION_ENABLED = booleanPreferencesKey("is_weekend_notification_enabled")
    val MAIN_CONGESTION_BASE_STATION = stringPreferencesKey("main_congestion_base_station")
    val SAVE_STATIONS_JSON = stringPreferencesKey("save_stations_json")
    val HOLIDAY_VERSION = intPreferencesKey("holiday_version")
    val HOLIDAY_LIST = stringSetPreferencesKey("holiday_list")
}
