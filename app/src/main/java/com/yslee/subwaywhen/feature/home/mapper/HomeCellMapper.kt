package com.yslee.subwaywhen.feature.home.mapper

import com.yslee.subwaywhen.data.model.SaveStation
import com.yslee.subwaywhen.data.model.SaveStationGroup
import com.yslee.subwaywhen.data.remote.dto.liveArrival.LiveStationModel
import com.yslee.subwaywhen.data.remote.dto.scheduleArrival.korail.KorailHeader
import com.yslee.subwaywhen.data.remote.dto.scheduleArrival.seoul.ScheduleStationModel
import com.yslee.subwaywhen.feature.home.HomeCellData
import com.yslee.subwaywhen.feature.home.HomeCellType
import java.util.Calendar

fun SaveStation.toLoadingCell(index: Int): HomeCellData = HomeCellData(
    stationIndex = index,
    type = HomeCellType.Loading,
    stationName = stationName,
    updnLine = updnLine,
    lastStation = "",
    exceptionLastStation = exceptionLastStation,
    stateMSG = "",
    arrivalTime = "",
    subPrevious = "",
    code = "",
    isFast = "",
    line = line,
    lineCode = lineCode,
    korailCode = korailCode,
    stationCode = stationCode,
)

fun LiveStationModel.toRealCells(index: Int, base: SaveStation): List<HomeCellData> {
    // 9호선은 API 응답의 상하행 방향이 역전되어 있어 반대로 필터링
    val targetUpDown = if (base.line == "09호선") {
        if (base.updnLine == "상행") "하행" else "상행"
    } else {
        base.updnLine
    }
    val matched = realtimeArrivalList
        .filter { it.upDown == targetUpDown }
        .filter { base.exceptionLastStation.isEmpty() || !base.exceptionLastStation.contains(it.lastStation) }
    if (matched.isEmpty()) {
        return listOf(
            base.toLoadingCell(index).copy(
                type = HomeCellType.Real,
                stateMSG = "정보 없음",
            )
        )
    }
    return matched.map { arrival ->
        HomeCellData(
            stationIndex = index,
            type = HomeCellType.Real,
            stationName = base.stationName,
            updnLine = arrival.upDown,
            lastStation = arrival.lastStation,
            exceptionLastStation = base.exceptionLastStation,
            stateMSG = arrival.previousStation ?: "",
            arrivalTime = arrival.arrivalTime,
            subPrevious = arrival.subPrevious,
            code = arrival.code,
            isFast = arrival.isFast ?: "",
            line = base.line,
            lineCode = base.lineCode,
            korailCode = base.korailCode,
            stationCode = base.stationCode,
        )
    }
}

fun ScheduleStationModel.toScheduleCell(prev: HomeCellData): HomeCellData {
    val now = Calendar.getInstance()
    val nowHour = now.get(Calendar.HOUR_OF_DAY)
    val nowMin = now.get(Calendar.MINUTE)
    val nowTotal = nowHour * 60 + nowMin

    val next = SearchSTNTimeTableByFRCodeService.row.firstOrNull { arrival ->
        val digits = arrival.startTime.filter { it.isDigit() }
        // digits is at least "HHmm" (4 digits) or "HHmmss" (6 digits)
        if (digits.length < 4) return@firstOrNull false
        val h = digits.substring(0, 2).toIntOrNull() ?: return@firstOrNull false
        val m = digits.substring(2, 4).toIntOrNull() ?: return@firstOrNull false
        h * 60 + m >= nowTotal
    }

    return if (next != null) {
        val digits = next.startTime.filter { it.isDigit() }
        val h = digits.substring(0, 2)
        val m = digits.substring(2, 4)
        prev.copy(
            type = HomeCellType.Schedule,
            subPrevious = "${next.startStation}→${next.lastStation} $h:$m",
            stateMSG = "",
        )
    } else {
        prev.copy(
            type = HomeCellType.Schedule,
            subPrevious = "운행 종료",
            stateMSG = "",
        )
    }
}

fun KorailHeader.toScheduleCell(prev: HomeCellData): HomeCellData {
    val now = Calendar.getInstance()
    val nowHour = now.get(Calendar.HOUR_OF_DAY)
    val nowMin = now.get(Calendar.MINUTE)
    val nowTotal = nowHour * 60 + nowMin

    val next = body.firstOrNull { schedule ->
        val time = schedule.time ?: return@firstOrNull false
        if (time.length < 6) return@firstOrNull false
        val h = time.substring(0, 2).toIntOrNull() ?: return@firstOrNull false
        val m = time.substring(2, 4).toIntOrNull() ?: return@firstOrNull false
        h * 60 + m >= nowTotal
    }

    return if (next != null) {
        val time = next.time!! // non-null guaranteed by firstOrNull filter
        val h = time.substring(0, 2)
        val m = time.substring(2, 4)
        prev.copy(
            type = HomeCellType.Schedule,
            subPrevious = "$h:$m",
            stateMSG = "",
        )
    } else {
        prev.copy(
            type = HomeCellType.Schedule,
            subPrevious = "운행 종료",
            stateMSG = "",
        )
    }
}

/**
 * iOS MainModel.timeGroup 포팅.
 * oneTime/twoTime이 모두 0이면 null 반환.
 * oneValid = oneTime > 0 && oneTime <= nowHour
 * twoValid = twoTime > 0 && twoTime <= nowHour
 * 둘 다 유효하면 oneTime >= twoTime이면 ONE, 아니면 TWO
 * oneValid만 → ONE, twoValid만 → TWO, 모두 아니면 null
 */
fun timeGroup(oneTime: Int, twoTime: Int, nowHour: Int): SaveStationGroup? {
    if (oneTime == 0 && twoTime == 0) return null

    val oneValid = oneTime > 0 && oneTime <= nowHour
    val twoValid = twoTime > 0 && twoTime <= nowHour

    return when {
        oneValid && twoValid -> if (oneTime >= twoTime) SaveStationGroup.ONE else SaveStationGroup.TWO
        oneValid -> SaveStationGroup.ONE
        twoValid -> SaveStationGroup.TWO
        else -> null
    }
}
