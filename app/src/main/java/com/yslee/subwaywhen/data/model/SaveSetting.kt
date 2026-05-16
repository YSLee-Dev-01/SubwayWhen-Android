package com.yslee.subwaywhen.data.model

data class SaveSetting(
    val mainCongestionLabel: String = "☹️",
    val mainGroupOneTime: Int = 0,
    val mainGroupTwoTime: Int = 0,
    val detailAutoReload: Boolean = true,
    val detailScheduleAutoTime: Boolean = true,
    val searchOverlapAlert: Boolean = true,
    val tutorialSuccess: Boolean = false,
    val detailVcTrainIcon: String = "🚃",
    val isWeekendNotificationEnabled: Boolean = true,
    val mainCongestionBaseStation: String = "강남",
)
