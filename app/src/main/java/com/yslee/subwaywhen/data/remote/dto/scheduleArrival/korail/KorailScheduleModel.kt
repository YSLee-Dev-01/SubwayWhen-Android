package com.yslee.subwaywhen.data.remote.dto.scheduleArrival.korail

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class KorailHeader(
    val body: List<KorailSchedule>
)

@Serializable
@SerialName("KorailScdule")
data class KorailSchedule(
    @SerialName("dptTm") val time: String? = null,
    @SerialName("trnNo") val trainCode: String,
    @SerialName("lnCd") val lineCode: String,
    @SerialName("dayCd") val weekDay: String,
    @SerialName("stinCd") val stationId: String
)

@Serializable
data class KorailTrainNumber(
    val endStation: String,
    val isFast: String,
    val line: String,
    val startStation: String,
    val trainNumber: String,
    val week: String,
)

data class ProcessedKorailSchedule(
    val time: String,
    val lastStation: String,
    val startStation: String,
    val isFast: String,
)
