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
    @SerialName("btrainNo") val trainCode: String,
    // TotalLoadModelImpl.liveArrivalSplit()에서 statnFid 조회 후 채워지는 파생 필드
    // iOS TotalLoadModel.nextAndBackStationSearch() 대응
    @kotlinx.serialization.Transient val backStationName: String = "",
) {
    /**
     * iOS RealtimeStationArrival.useState 대응.
     * code 기반으로 현재 상태 메시지를 반환한다.
     */
    val useState: String
        get() = when (code) {
            "0"  -> "$stationName 진입"
            "1"  -> "$stationName 도착"
            "2"  -> "$stationName 출발"
            "3"  -> "전역 출발"
            "4"  -> "전역 진입"
            "5"  -> "전역 도착"
            "99" -> previousStation?.let { "$it 부근" } ?: subPrevious
            else -> code
        }
}
