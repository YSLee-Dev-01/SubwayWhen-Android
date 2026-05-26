package com.yslee.subwaywhen.ui.common

import androidx.compose.ui.graphics.Color

/**
 * iOS SubwayLineData.useLine / Assets.xcassets 기준 호선 매핑.
 * - subwayLineDisplayName: 원형 내부 표시 텍스트 (긴 이름 약어 처리)
 * - subwayLineColor: 호선 컬러 (null이면 StationLineCircle이 Gray로 폴백)
 */
fun subwayLineDisplayName(line: String): String = when (line) {
    "01호선" -> "1호선"
    "02호선" -> "2호선"
    "03호선" -> "3호선"
    "04호선" -> "4호선"
    "05호선" -> "5호선"
    "06호선" -> "6호선"
    "07호선" -> "7호선"
    "08호선" -> "8호선"
    "09호선" -> "9호선"
    "경의선" -> "경의중앙"
    "서해선" -> "서해"
    "수인분당선" -> "수인분당"
    "신분당선" -> "신분당"
    "경강선" -> "경강"
    "경춘선" -> "경춘"
    "공항철도" -> "공항"
    "김포도시철도" -> "김포"
    "신림선" -> "신림"
    "용인경전철" -> "용인"
    "우이신설경전철" -> "우이"
    "의정부경전철" -> "의정부"
    "인천선" -> "인천1"
    "인천2호선" -> "인천2"
    "GTX-A" -> "GTX-A"
    else -> line.trimStart('0')
}

fun subwayLineColor(line: String): Color? = when (line) {
    "01호선" -> Color(0xFF0052A4)
    "02호선" -> Color(0xFF00A84D)
    "03호선" -> Color(0xFFEF7C1C)
    "04호선" -> Color(0xFF00A4E3)
    "05호선" -> Color(0xFF996CAC)
    "06호선" -> Color(0xFFCD7C2F)
    "07호선" -> Color(0xFF747F00)
    "08호선" -> Color(0xFFE6186C)
    "09호선" -> Color(0xFFBDB092)
    "수인분당선" -> Color(0xFFFABE00)
    "신분당선" -> Color(0xFFD31145)
    "경의선" -> Color(0xFF77C4A3)
    "공항철도" -> Color(0xFF0090D2)
    "경춘선" -> Color(0xFF178C72)
    "경강선" -> Color(0xFF0054A6)
    "서해선" -> Color(0xFF8FC31F)
    "우이신설경전철" -> Color(0xFFB7C450)
    "인천선" -> Color(0xFF759CCE)
    "인천2호선" -> Color(0xFFF5A251)
    "신림선" -> Color(0xFF6789CA)
    "김포도시철도" -> Color(0xFFAD8605)
    "용인경전철" -> Color(0xFF56AD2D)
    "의정부경전철" -> Color(0xFFFD8100)
    "GTX-A" -> Color(0xFF9A6292)
    else -> null
}
