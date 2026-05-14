package com.yslee.subwaywhen.data.remote.dto.scheduleArrival.seoul

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ScheduleStationModel(
    val SearchSTNTimeTableByFRCodeService: SearchSTNTimeTableByFRCodeService
)

@Serializable
data class SearchSTNTimeTableByFRCodeService(
    val row: List<ScheduleStationArrival>
)

@Serializable
data class ScheduleStationArrival(
    @SerialName("LEFTTIME") val startTime: String,
    @SerialName("FR_CODE") val stationCode: String,
    @SerialName("SUBWAYSNAME") val startStation: String,
    @SerialName("SUBWAYENAME") val lastStation: String,
    @SerialName("INOUT_TAG") val upDown: String,
    @SerialName("LINE_NUM") val line: String,
    @SerialName("WEEK_TAG") val weekDay: String,
    @SerialName("EXPRESS_YN") val isFast: String
)
