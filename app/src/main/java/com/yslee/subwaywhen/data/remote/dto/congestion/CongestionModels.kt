package com.yslee.subwaywhen.data.remote.dto.congestion

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CongestionDataSet(val stations: Map<String, StationCongestion>)

@Serializable
data class StationCongestion(
    @SerialName("hourly_congestion") val hourlyCongestion: DayCongestion,
)

@Serializable
data class DayCongestion(
    val weekday: Map<String, CongestionLevel>,
    val saturday: Map<String, CongestionLevel>,
    val sunday: Map<String, CongestionLevel>,
)

@Serializable
data class CongestionLevel(val percent: Int, val level: Int)
