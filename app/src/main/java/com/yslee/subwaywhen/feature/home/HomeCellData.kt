package com.yslee.subwaywhen.feature.home

/**
 * iOS MainTableViewCellData 대응 표시 모델.
 * [useTime], [useFast]는 iOS 원본 computed property 동일 규칙으로 구현.
 */
data class HomeCellData(
    val stationIndex: Int,
    val type: HomeCellType,
    val stationName: String,
    val updnLine: String,
    val lastStation: String,
    val exceptionLastStation: String,
    val stateMSG: String,
    val arrivalTime: String,
    val subPrevious: String,
    val code: String,
    val isFast: String,
    val line: String,
    val lineCode: String,
    val korailCode: String,
    val stationCode: String,
) {
    /**
     * 도착 시간 표시 문자열.
     * - Real & arrivalTime==0 & code 0→"곧 도착", 1→"도착", 2→"출발", else→subPrevious에서 '(' 이전
     * - Real & arrivalTime<60 → "${N}초"
     * - Real & arrivalTime>=60 → "${N/60}분"
     * - Schedule → subPrevious 그대로
     * - Loading → ""
     */
    val useTime: String
        get() {
            return when (type) {
                HomeCellType.Real -> {
                    val time = arrivalTime.toIntOrNull() ?: 0
                    val min = time / 60
                    if (min == 0) {
                        if (time == 0) {
                            when (code) {
                                "0" -> "곧 도착"
                                "1" -> "도착"
                                "2" -> "출발"
                                else -> subPrevious.substringBefore("(")
                            }
                        } else {
                            "${time}초"
                        }
                    } else {
                        "${min}분"
                    }
                }
                HomeCellType.Schedule -> subPrevious
                HomeCellType.Loading -> ""
            }
        }

    /**
     * 급행/ITX 여부 표시 문자열.
     * - "급행" → "(급)"
     * - "ITX" → "(ITX)"
     * - else → ""
     */
    val useFast: String
        get() = when (isFast) {
            "급행" -> "(급)"
            "ITX" -> "(ITX)"
            else -> ""
        }
}
