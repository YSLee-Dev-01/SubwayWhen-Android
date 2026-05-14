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
