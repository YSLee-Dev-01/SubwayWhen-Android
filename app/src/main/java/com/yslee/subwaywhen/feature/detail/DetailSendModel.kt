package com.yslee.subwaywhen.feature.detail

import com.yslee.subwaywhen.feature.home.HomeCellData
import kotlinx.serialization.Serializable

@Serializable
data class DetailSendModel(
    val upDown: String,
    val stationName: String,
    val lineNumber: String,
    val stationCode: String,
    val lineCode: String,
    val exceptionLastStation: String,
    val korailCode: String,
)

fun HomeCellData.toDetailSendModel() = DetailSendModel(
    upDown = updnLine,
    stationName = stationName,
    lineNumber = line,
    stationCode = stationCode,
    lineCode = lineCode,
    exceptionLastStation = exceptionLastStation,
    korailCode = korailCode,
)
