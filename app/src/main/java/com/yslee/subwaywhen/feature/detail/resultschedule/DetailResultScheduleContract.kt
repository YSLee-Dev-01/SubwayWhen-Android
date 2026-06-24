package com.yslee.subwaywhen.feature.detail.resultschedule

import com.yslee.subwaywhen.feature.detail.DetailScheduleItem

data class DetailResultScheduleUiState(
    val stationName: String = "",
    val upDown: String = "",
    val exceptionLastStation: String = "",
    val hourSections: List<HourSection> = emptyList(),
    val currentHourIndex: Int = 0,
    val isExceptionModalVisible: Boolean = false,
    val selectedDestination: String? = null,
)

data class HourSection(
    val hour: Int,
    val label: String,
    val items: List<DetailScheduleItem>,
)

sealed interface DetailResultScheduleIntent {
    data object OnAppear : DetailResultScheduleIntent
    data class ExceptionButtonTap(val destination: String) : DetailResultScheduleIntent
    data object ExceptionConfirm : DetailResultScheduleIntent
    data object ExceptionDismiss : DetailResultScheduleIntent
    data object Back : DetailResultScheduleIntent
}

sealed interface DetailResultScheduleEffect {
    data class NavigateBackWithException(val exceptionLastStation: String) : DetailResultScheduleEffect
    data object NavigateBack : DetailResultScheduleEffect
}
