package com.yslee.subwaywhen.feature.detail.mapper

import com.yslee.subwaywhen.data.remote.dto.liveArrival.RealtimeStationArrival
import com.yslee.subwaywhen.data.remote.dto.scheduleArrival.korail.ProcessedKorailSchedule
import com.yslee.subwaywhen.data.remote.dto.scheduleArrival.seoul.ScheduleStationArrival
import com.yslee.subwaywhen.data.remote.dto.scheduleArrival.shinbundang.ProcessedShinbundangSchedule
import com.yslee.subwaywhen.feature.detail.DetailArrivalItem
import com.yslee.subwaywhen.feature.detail.DetailScheduleItem
import java.util.Calendar

private val UNOWNED_LINES = setOf("공항철도", "우이신설경전철", "경강선", "서해선", "GTX-A")

fun isUnownedLine(lineNumber: String): Boolean =
    UNOWNED_LINES.any { lineNumber.contains(it) }

fun RealtimeStationArrival.toDetailArrivalItem() = DetailArrivalItem(
    useTime = resolveUseTime(),
    statusMessage = useState,
    destination = lastStation,
    trainNo = trainCode,
    isFast = isFast == "급행" || isFast == "ITX",
    statusCode = code,
    prevStationName = backStationName,
    nextStationName = "",
)

private fun RealtimeStationArrival.resolveUseTime(): String {
    val time = arrivalTime.toIntOrNull() ?: 0
    val min = time / 60
    return when {
        min == 0 && time == 0 -> when (code) {
            "0" -> "곧 도착"
            "1" -> "도착"
            "2" -> "출발"
            else -> subPrevious.substringBefore("(")
        }
        min == 0 -> "${time}초"
        else -> "${min}분"
    }
}

fun ScheduleStationArrival.toDetailScheduleItem(): DetailScheduleItem {
    val timeLabel = startTime.take(5) // "HH:MM:SS" → "HH:MM"
    return DetailScheduleItem(
        minutesLater = minutesLater(timeLabel),
        timeLabel = timeLabel,
        destination = lastStation,
        isFast = isFast == "Y",
    )
}

fun ProcessedKorailSchedule.toDetailScheduleItem(): DetailScheduleItem {
    // time 형식: "0HMMSS" → HH:MM
    val timeLabel = parseKorailTime(time)
    return DetailScheduleItem(
        minutesLater = minutesLater(timeLabel),
        timeLabel = timeLabel,
        destination = lastStation,
        isFast = isFast == "급행" || isFast == "ITX",
    )
}

fun ProcessedShinbundangSchedule.toDetailScheduleItem(): DetailScheduleItem {
    // startTime: "HH:MM" 형식, 0시 → 24시 처리
    val timeLabel = parseShinbundangTime(startTime)
    return DetailScheduleItem(
        minutesLater = minutesLater(timeLabel),
        timeLabel = timeLabel,
        destination = endStation,
        isFast = false,
    )
}

private fun parseKorailTime(raw: String): String {
    // "HHMMSS" → "HH:MM" (ProcessedKorailSchedule.time은 dptTm 필드 그대로)
    if (raw.length < 6) return raw
    val hour = raw.substring(0, raw.length - 4).trimStart('0').ifEmpty { "0" }
    val minute = raw.substring(raw.length - 4, raw.length - 2)
    return "%02d:%s".format(hour.toIntOrNull() ?: 0, minute)
}

private fun parseShinbundangTime(raw: String): String {
    // "HH:MM" 그대로, 단 "00:" 로 시작하면 "24:"로 변환
    return if (raw.startsWith("00:")) "24:" + raw.substring(3) else raw
}

fun minutesLater(timeLabel: String): Int {
    val parts = timeLabel.split(":")
    val hour = parts.getOrNull(0)?.toIntOrNull() ?: return 0
    val minute = parts.getOrNull(1)?.toIntOrNull() ?: return 0
    val cal = Calendar.getInstance()
    val nowMinutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
    val targetMinutes = hour * 60 + minute
    return (targetMinutes - nowMinutes).coerceAtLeast(0)
}

fun List<DetailScheduleItem>.filterFromNow(): List<DetailScheduleItem> {
    val cal = Calendar.getInstance()
    val nowMinutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
    val filtered = filter { item ->
        val parts = item.timeLabel.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: return@filter false
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: return@filter false
        hour * 60 + minute >= nowMinutes
    }
    return filtered.ifEmpty { this }
}
