package com.yslee.subwaywhen.data.model

data class SaveSetting(
    val mainCongestionLabel: String = "☹️",
    val mainGroupOneTime: Int = 0,
    val mainGroupTwoTime: Int = 0,
    val detailAutoReload: Boolean = true,
    val detailScheduleAutoTime: Boolean = true,
    val liveActivity: Boolean = true,
    val searchOverlapAlert: Boolean = true,
    val alertGroupOneId: String = "",
    val alertGroupTwoId: String = "",
    val tutorialSuccess: Boolean = false,
    val detailVcTrainIcon: String = "🚃",
    val isWeekendNotificationEnabled: Boolean = true,
    val mainCongestionBaseStation: String = "강남",
)
