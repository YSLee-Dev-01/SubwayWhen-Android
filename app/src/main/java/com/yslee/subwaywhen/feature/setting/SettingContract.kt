package com.yslee.subwaywhen.feature.setting

import com.yslee.subwaywhen.data.model.SaveSetting

enum class TimeGroup { Work, Leave }

enum class SettingModalType { TrainIcon, License, Contents }

enum class SettingToggleField { AutoReload, ScheduleAutoTime, SearchOverlap }

data class SettingUiState(
    val saveSetting: SaveSetting = SaveSetting(),
    val expandedTimeGroup: TimeGroup? = null,
    val activeModal: SettingModalType? = null,
    val modalLicenses: List<String> = emptyList(),
    val modalContents: String = "",
    val isModalLoading: Boolean = false,
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
    data object WorkAlarmTapped : SettingIntent
}
