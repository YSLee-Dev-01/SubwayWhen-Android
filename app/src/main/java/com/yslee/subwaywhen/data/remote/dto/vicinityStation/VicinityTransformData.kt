package com.yslee.subwaywhen.data.remote.dto.vicinityStation

data class VicinityTransformData(
    val id: String,
    val name: String,
    val line: String,
    val distance: String
) {
    val lineColorName: String
        get() {
            if (line == "경의중앙선") return "경의선"
            if (line == "에버라인") return "용인경전철"
            if (line == "김포골드라인") return "김포도시철도"
            if (line == "인천1호선") return "인천선"
            if (line == "우이신설선") return "우이신설경전철"

            if (line.firstOrNull()?.isDigit() != true) return line

            return "0$line"
        }

    val lineName: String
        get() {
            if (line.firstOrNull()?.isDigit() == true) return line

            return when (line) {
                "공항철도" -> "공항"
                "김포골드라인" -> "김포"
                "에버라인" -> "용인"
                "우이신설선" -> "우이"
                "의정부경전철" -> "의정부"
                "인천1호선" -> "인천1"
                "인천2호선" -> "인천2"
                else -> line.replace("선", "")
            }
        }
}
