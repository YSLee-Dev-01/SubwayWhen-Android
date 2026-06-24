package com.yslee.subwaywhen.feature.detail

import com.yslee.subwaywhen.feature.detail.resultschedule.DetailResultScheduleSendModel
import kotlinx.serialization.Serializable

data class DetailUiState(
    val sendModel: DetailSendModel,
    val firstArrival: DetailArrivalItem? = null,
    val secondArrival: DetailArrivalItem? = null,
    val prevStationName: String = "",
    val nextStationName: String = "",
    val scheduleItems: List<DetailScheduleItem> = emptyList(),
    val isArrivalLoading: Boolean = true,
    val isScheduleLoading: Boolean = true,
    val timerCount: Int = 15,
    val isRefreshCooldown: Boolean = false,
    val arrivalError: Boolean = false,
    val scheduleError: Boolean = false,
    val isUnowned: Boolean = false,
    val trainIcon: String = "🚃",
)

data class DetailArrivalItem(
    val useTime: String,
    val statusMessage: String,
    val destination: String,
    val trainNo: String,
    val isFast: Boolean,
    val statusCode: String,
    val prevStationName: String = "",
    val nextStationName: String = "",
)

@Serializable
data class DetailScheduleItem(
    val minutesLater: Int,
    val timeLabel: String,
    val destination: String,
    val isFast: Boolean,
)

sealed interface DetailIntent {
    data object OnAppear : DetailIntent
    data object OnDisappear : DetailIntent
    data object Refresh : DetailIntent
    data object ScheduleMoreTap : DetailIntent
    data object RealtimeTap : DetailIntent
    data object ExceptionRowTap : DetailIntent
    data object ReportTap : DetailIntent
    data object Back : DetailIntent
}

sealed interface DetailEffect {
    data class NavigateToResultSchedule(val sendModel: DetailResultScheduleSendModel) : DetailEffect
    data object NavigateBack : DetailEffect
}
