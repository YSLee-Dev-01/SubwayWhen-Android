package com.yslee.subwaywhen.feature.home.mapper

import com.yslee.subwaywhen.data.model.SaveStation
import com.yslee.subwaywhen.data.model.SaveStationGroup
import com.yslee.subwaywhen.data.remote.dto.liveArrival.LiveStationModel
import com.yslee.subwaywhen.data.remote.dto.scheduleArrival.korail.ProcessedKorailSchedule
import com.yslee.subwaywhen.data.remote.dto.scheduleArrival.shinbundang.ProcessedShinbundangSchedule
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
    stateMSG = "데이터를 로드하고 있어요.",
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
    // iOS: station.lineCode == x.subWayId && upDown == x.upDown && !(exceptionLastStation.contains(x.lastStation))
    // lineCode 필터로 환승역에서 다른 호선 데이터가 섞이는 버그 방지
    val matched = realtimeArrivalList
        .filter { it.subWayId == base.lineCode }
        .filter { it.upDown == targetUpDown }
        .filter { base.exceptionLastStation.isEmpty() || !base.exceptionLastStation.contains(it.lastStation) }
    // iOS는 첫 번째 매칭만 반환 (한 역 = 셀 1개)
    val arrival = matched.firstOrNull()
        ?: return listOf(
            base.toLoadingCell(index).copy(
                type = HomeCellType.Real,
                stateMSG = "현재 실시간 열차 데이터가 없어요.",
            )
        )
    return listOf(
        HomeCellData(
            stationIndex = index,
            subIndex = 0,
            type = HomeCellType.Real,
            stationName = base.stationName,
            updnLine = base.updnLine,  // 사용자가 저장한 방향 유지 (9호선 반전은 targetUpDown 필터에서 처리됨)
            lastStation = if (arrival.lastStation.isNotEmpty()) "${arrival.lastStation}행" else "",
            exceptionLastStation = base.exceptionLastStation,
            stateMSG = arrival.useState,
            arrivalTime = arrival.arrivalTime,
            subPrevious = arrival.subPrevious,
            code = arrival.code,
            isFast = arrival.isFast ?: "",
            line = base.line,
            lineCode = base.lineCode,
            korailCode = base.korailCode,
            stationCode = base.stationCode,
        )
    )
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
        val h = digits.substring(0, 2).toIntOrNull() ?: 0
        val m = digits.substring(2, 4).toIntOrNull() ?: 0
        val remainingMin = (h * 60 + m) - nowTotal
        prev.copy(
            type = HomeCellType.Schedule,
            lastStation = if (next.lastStation.isNotEmpty()) "${next.lastStation}행" else prev.lastStation,
            stateMSG = "%02d:%02d".format(h, m),
            subPrevious = "${remainingMin}분",
        )
    } else {
        prev.copy(
            type = HomeCellType.Schedule,
            stateMSG = "운행 종료",
            subPrevious = "운행 종료",
        )
    }
}

@JvmName("korailToScheduleCell")
fun List<ProcessedKorailSchedule>.toScheduleCell(prev: HomeCellData): HomeCellData {
    val now = Calendar.getInstance()
    val nowHour = now.get(Calendar.HOUR_OF_DAY)
    val nowMin = now.get(Calendar.MINUTE)
    val nowTotal = nowHour * 60 + nowMin

    val next = firstOrNull { schedule ->
        if (schedule.time.length < 6) return@firstOrNull false
        val h = schedule.time.substring(0, 2).toIntOrNull() ?: return@firstOrNull false
        val m = schedule.time.substring(2, 4).toIntOrNull() ?: return@firstOrNull false
        h * 60 + m >= nowTotal
    }

    return if (next != null) {
        val h = next.time.substring(0, 2).toIntOrNull() ?: 0
        val m = next.time.substring(2, 4).toIntOrNull() ?: 0
        val remainingMin = (h * 60 + m) - nowTotal
        prev.copy(
            type = HomeCellType.Schedule,
            lastStation = if (next.lastStation.isNotEmpty()) "${next.lastStation}행" else prev.lastStation,
            stateMSG = "%02d:%02d".format(h, m),
            subPrevious = "${remainingMin}분",
            isFast = next.isFast,
        )
    } else {
        prev.copy(
            type = HomeCellType.Schedule,
            stateMSG = "운행 종료",
            subPrevious = "운행 종료",
        )
    }
}

@JvmName("shinbundangToScheduleCell")
fun List<ProcessedShinbundangSchedule>.toScheduleCell(prev: HomeCellData): HomeCellData {
    val now = Calendar.getInstance()
    val nowHour = now.get(Calendar.HOUR_OF_DAY)
    val nowMin = now.get(Calendar.MINUTE)
    val nowTotal = nowHour * 60 + nowMin

    // startTime은 "HH:mm:ss" 또는 "HHmmss" 형식
    val next = firstOrNull { schedule ->
        val digits = schedule.startTime.filter { it.isDigit() }
        if (digits.length < 4) return@firstOrNull false
        val h = digits.substring(0, 2).toIntOrNull() ?: return@firstOrNull false
        val m = digits.substring(2, 4).toIntOrNull() ?: return@firstOrNull false
        h * 60 + m >= nowTotal
    }

    return if (next != null) {
        val digits = next.startTime.filter { it.isDigit() }
        val h = digits.substring(0, 2).toIntOrNull() ?: 0
        val m = digits.substring(2, 4).toIntOrNull() ?: 0
        val remainingMin = (h * 60 + m) - nowTotal
        prev.copy(
            type = HomeCellType.Schedule,
            lastStation = if (next.endStation.isNotEmpty()) "${next.endStation}행" else prev.lastStation,
            stateMSG = "%02d:%02d".format(h, m),
            subPrevious = "${remainingMin}분",
            isFast = "",
        )
    } else {
        prev.copy(
            type = HomeCellType.Schedule,
            stateMSG = "운행 종료",
            subPrevious = "운행 종료",
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
