package com.yslee.subwaywhen.feature.setting

import com.yslee.subwaywhen.data.model.SaveSetting
import com.yslee.subwaywhen.data.model.SaveStation

enum class TimeGroup { Work, Leave }

enum class SettingModalType { TrainIcon, License, Contents, WorkAlarm }

enum class SettingToggleField { AutoReload, ScheduleAutoTime, SearchOverlap }

data class SettingUiState(
    val saveSetting: SaveSetting = SaveSetting(),
    val expandedTimeGroup: TimeGroup? = null,
    val activeModal: SettingModalType? = null,
    val modalLicenses: List<String> = emptyList(),
    val modalContents: String = "",
    val isModalLoading: Boolean = false,
    val hasNotificationPermission: Boolean = false,
    val isWeekendIncluded: Boolean = true,
    val workAlarmGroupOneStation: SaveStation? = null,
    val workAlarmGroupTwoStation: SaveStation? = null,
    val workAlarmSelectGroup: TimeGroup? = null,
    val groupOneStations: List<SaveStation> = emptyList(),
    val groupTwoStations: List<SaveStation> = emptyList(),
)

sealed interface SettingIntent {
    data object OnAppear : SettingIntent
    data class TimeGroupTapped(val group: TimeGroup) : SettingIntent
    data class TimeSaved(val group: TimeGroup, val time: Int) : SettingIntent
    data class ToggleChanged(val field: SettingToggleField) : SettingIntent
    data class CongestionLabelChanged(val text: String) : SettingIntent
    data object CongestionLabelFocusLost : SettingIntent
    data object TrainIconTapped : SettingIntent
    data class TrainIconSelected(val icon: String) : SettingIntent
    data object LicenseTapped : SettingIntent
    data object ContentsTapped : SettingIntent
    data object ModalDismissed : SettingIntent
    data class WorkAlarmOpened(val hasPermission: Boolean) : SettingIntent
    data object WeekendToggled : SettingIntent
    data class WorkAlarmStationTapped(val group: TimeGroup) : SettingIntent
    data class WorkAlarmStationSelected(val station: SaveStation) : SettingIntent
    data class WorkAlarmStationReset(val group: TimeGroup) : SettingIntent
    data object WorkAlarmSelectPopped : SettingIntent
    data object WorkAlarmSaved : SettingIntent
}
