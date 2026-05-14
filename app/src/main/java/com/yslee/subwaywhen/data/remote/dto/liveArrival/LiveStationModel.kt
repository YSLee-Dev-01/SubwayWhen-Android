package com.yslee.subwaywhen.data.remote.dto.liveArrival

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LiveStationModel(
    val realtimeArrivalList: List<RealtimeStationArrival>
)

@Serializable
data class RealtimeStationArrival(
    @SerialName("updnLine") val upDown: String,
    @SerialName("barvlDt") val arrivalTime: String,
    @SerialName("arvlMsg3") val previousStation: String? = null,
    @SerialName("arvlMsg2") val subPrevious: String,
    @SerialName("arvlCd") val code: String,
    @SerialName("subwayId") val subWayId: String,
    @SerialName("statnNm") val stationName: String,
    @SerialName("bstatnNm") val lastStation: String,
    @SerialName("lineNumber") val lineNumber: String? = null,
    @SerialName("btrainSttus") val isFast: String? = null,
    @SerialName("statnFid") val backStationId: String,
    @SerialName("statnTid") val nextStationId: String,
    @SerialName("btrainNo") val trainCode: String
)
