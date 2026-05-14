package com.yslee.subwaywhen.data.remote.dto.subwayNotice

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubwayNoticeResponse(
    val response: ResponseData
) {
    @Serializable
    data class ResponseData(
        val body: Body
    )

    @Serializable
    data class Body(
        val items: Items
    ) {
        @Serializable
        data class Items(
            val item: List<SubwayNotice>
        )
    }
}

@Serializable
data class SubwayNotice(
    @SerialName("noftTtl") val title: String,
    @SerialName("noftCn") val content: String,
    @SerialName("noftOcrnDt") val occurredAt: String,
    @SerialName("lineNmLst") val lineNames: String? = null,
    @SerialName("crtrYmd") val createdDate: String,
    @SerialName("nonstopYn") val isNonstop: String,
    @SerialName("upbdnbSe") val direction: String? = null,
    @SerialName("xcseSitnEndDt") val exceptionEndedAt: String? = null
)
