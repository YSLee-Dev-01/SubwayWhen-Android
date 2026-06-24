package com.yslee.subwaywhen.feature.detail.resultschedule

import com.yslee.subwaywhen.feature.detail.DetailScheduleItem
import kotlinx.serialization.Serializable

@Serializable
data class DetailResultScheduleSendModel(
    val stationName: String,
    val upDown: String,
    val exceptionLastStation: String,
    val scheduleItems: List<DetailScheduleItem>,
)
