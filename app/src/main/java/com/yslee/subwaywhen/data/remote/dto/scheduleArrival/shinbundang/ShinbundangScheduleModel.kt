package com.yslee.subwaywhen.data.remote.dto.scheduleArrival.shinbundang

import kotlinx.serialization.Serializable

@Serializable
data class ShinbundangSchedule(
    val endStation: String,
    val startStation: String,
    val startTime: String,
    val stationName: String,
    val updown: String,
    val week: String,
)

data class ProcessedShinbundangSchedule(
    val startTime: String,
    val startStation: String,
    val endStation: String,
)
