package com.yslee.subwaywhen.feature.home

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class HomeCellDataTest : FunSpec({

    fun realCell(
        arrivalTime: String = "0",
        code: String = "0",
        subPrevious: String = "",
    ) = HomeCellData(
        stationIndex = 0, type = HomeCellType.Real,
        stationName = "테스트역", updnLine = "상행",
        lastStation = "종착역", exceptionLastStation = "",
        stateMSG = "", arrivalTime = arrivalTime,
        subPrevious = subPrevious, code = code,
        isFast = "", line = "2호선", lineCode = "1002",
        korailCode = "", stationCode = "222",
    )

    // ── useTime ──────────────────────────────────────────────

    test("useTime: Real, arrivalTime=0, code=0 → 곧 도착") {
        realCell(arrivalTime = "0", code = "0").useTime shouldBe "곧 도착"
    }

    test("useTime: Real, arrivalTime=0, code=1 → 도착") {
        realCell(arrivalTime = "0", code = "1").useTime shouldBe "도착"
    }

    test("useTime: Real, arrivalTime=0, code=2 → 출발") {
        realCell(arrivalTime = "0", code = "2").useTime shouldBe "출발"
    }

    test("useTime: Real, arrivalTime=0, code=99, subPrevious=잠실(서울) → 잠실") {
        realCell(arrivalTime = "0", code = "99", subPrevious = "잠실(서울)").useTime shouldBe "잠실"
    }

    test("useTime: Real, arrivalTime=30 → 30초") {
        realCell(arrivalTime = "30").useTime shouldBe "30초"
    }

    test("useTime: Real, arrivalTime=59 → 59초") {
        realCell(arrivalTime = "59").useTime shouldBe "59초"
    }

    test("useTime: Real, arrivalTime=60 → 1분") {
        realCell(arrivalTime = "60").useTime shouldBe "1분"
    }

    test("useTime: Real, arrivalTime=120 → 2분") {
        realCell(arrivalTime = "120").useTime shouldBe "2분"
    }

    test("useTime: Real, arrivalTime=180 → 3분") {
        realCell(arrivalTime = "180").useTime shouldBe "3분"
    }

    test("useTime: Schedule, subPrevious=10:30 → 10:30") {
        HomeCellData(
            stationIndex = 0, type = HomeCellType.Schedule,
            stationName = "테스트역", updnLine = "상행",
            lastStation = "종착역", exceptionLastStation = "",
            stateMSG = "", arrivalTime = "0",
            subPrevious = "10:30", code = "0",
            isFast = "", line = "2호선", lineCode = "1002",
            korailCode = "", stationCode = "222",
        ).useTime shouldBe "10:30"
    }

    test("useTime: Loading → 빈 문자열") {
        HomeCellData(
            stationIndex = 0, type = HomeCellType.Loading,
            stationName = "테스트역", updnLine = "상행",
            lastStation = "종착역", exceptionLastStation = "",
            stateMSG = "", arrivalTime = "0",
            subPrevious = "", code = "0",
            isFast = "", line = "2호선", lineCode = "1002",
            korailCode = "", stationCode = "222",
        ).useTime shouldBe ""
    }

    // ── useFast ───────────────────────────────────────────────

    test("useFast: isFast=급행 → (급)") {
        realCell().copy(isFast = "급행").useFast shouldBe "(급)"
    }

    test("useFast: isFast=ITX → (ITX)") {
        realCell().copy(isFast = "ITX").useFast shouldBe "(ITX)"
    }

    test("useFast: isFast=빈 문자열 → 빈 문자열") {
        realCell().copy(isFast = "").useFast shouldBe ""
    }

    test("useFast: isFast=일반 → 빈 문자열") {
        realCell().copy(isFast = "일반").useFast shouldBe ""
    }
})
