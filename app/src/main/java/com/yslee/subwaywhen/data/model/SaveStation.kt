package com.yslee.subwaywhen.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SaveStation(
    val id: String = "",
    val stationName: String = "",
    val stationCode: String = "",
    val updnLine: String = "",
    val line: String = "",
    val lineCode: String = "",
    val group: SaveStationGroup = SaveStationGroup.ONE,
    val exceptionLastStation: String = "",
    val korailCode: String = "",
)
