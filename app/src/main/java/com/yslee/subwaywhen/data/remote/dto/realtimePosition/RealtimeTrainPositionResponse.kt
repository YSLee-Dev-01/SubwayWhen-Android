package com.yslee.subwaywhen.data.remote.dto.realtimePosition

import kotlinx.serialization.Serializable

@Serializable
data class RealtimeTrainPositionResponse(
    val realtimePositionList: List<RealtimeTrainPosition> = emptyList()
)

@Serializable
data class RealtimeTrainPosition(
    val subwayId: String,
    val subwayNm: String,
    val statnId: String,
    val statnNm: String,
    val trainNo: String,
    val lastRecptnDt: String,
    val recptnDt: String,
    val updnLine: String,
    val statnTid: String,
    val statnTnm: String,
    val trainSttus: String,
    val directAt: String,
    val lstcarAt: String
)
