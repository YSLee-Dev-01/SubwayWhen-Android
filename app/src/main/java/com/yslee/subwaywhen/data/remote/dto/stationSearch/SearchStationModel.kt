package com.yslee.subwaywhen.data.remote.dto.stationSearch

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("SearchStaion")
data class SearchStation(
    val SearchInfoBySubwayNameService: SearchInfoBySubwayNameService
)

@Serializable
data class SearchInfoBySubwayNameService(
    val row: List<SearchStationInfo>
)

@Serializable
data class SearchStationInfo(
    @SerialName("STATION_NM") val stationName: String,
    @SerialName("LINE_NUM") val line: String,
    @SerialName("FR_CODE") val stationCode: String
)
