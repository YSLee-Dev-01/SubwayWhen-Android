package com.yslee.subwaywhen.feature.home

import com.yslee.subwaywhen.data.model.SaveStationGroup
import com.yslee.subwaywhen.data.remote.dto.scheduleArrival.korail.ProcessedKorailSchedule
import com.yslee.subwaywhen.feature.home.mapper.timeGroup
import com.yslee.subwaywhen.feature.home.mapper.toScheduleCell
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class HomeCellMapperTest : FunSpec({

    // ── timeGroup ─────────────────────────────────────────────────────────────

    test("timeGroup: oneTime=0, twoTime=0 → null") {
        timeGroup(0, 0, 10) shouldBe null
    }

    test("timeGroup: oneTime만 유효 → ONE") {
        timeGroup(8, 0, 9) shouldBe SaveStationGroup.ONE
    }

    test("timeGroup: twoTime만 유효 → TWO") {
        timeGroup(0, 18, 19) shouldBe SaveStationGroup.TWO
    }

    test("timeGroup: 둘 다 유효, oneTime >= twoTime → ONE") {
        timeGroup(18, 8, 19) shouldBe SaveStationGroup.ONE
    }

    test("timeGroup: 둘 다 유효, oneTime < twoTime → TWO") {
        timeGroup(8, 18, 19) shouldBe SaveStationGroup.TWO
    }

    test("timeGroup: oneTime이 nowHour보다 크면 유효하지 않음 → null") {
        timeGroup(9, 0, 8) shouldBe null
    }

    test("timeGroup: 경계값 — oneTime == nowHour는 유효 (≤ 조건)") {
        timeGroup(9, 0, 9) shouldBe SaveStationGroup.ONE
    }

    test("timeGroup: oneTime=0이면 twoTime도 0이 아니고 유효해야 TWO 반환") {
        timeGroup(0, 18, 18) shouldBe SaveStationGroup.TWO
    }

    // ── toScheduleCell (Korail) ───────────────────────────────────────────────

    fun fakeCell() = HomeCellData(
        stationIndex = 0, type = HomeCellType.Real,
        stationName = "강남", updnLine = "상행",
        lastStation = "성수행", exceptionLastStation = "",
        stateMSG = "", arrivalTime = "120",
        subPrevious = "", code = "0",
        isFast = "", line = "수도권전철 1호선", lineCode = "1",
        korailCode = "", stationCode = "100",
    )

    test("Korail toScheduleCell - 목록 비어있으면 운행 종료") {
        val result = emptyList<ProcessedKorailSchedule>().toScheduleCell(fakeCell())
        result.type shouldBe HomeCellType.Schedule
        result.stateMSG shouldBe "운행 종료"
        result.subPrevious shouldBe "운행 종료"
    }

    test("Korail toScheduleCell - time 길이 6 미만이면 건너뜀 → 다음 유효 열차 선택") {
        val schedules = listOf(
            ProcessedKorailSchedule(time = "23", lastStation = "무효", startStation = "", isFast = ""),
            ProcessedKorailSchedule(time = "235900", lastStation = "천안", startStation = "", isFast = ""),
        )
        val result = schedules.toScheduleCell(fakeCell())
        result.lastStation shouldBe "천안행"
        result.stateMSG shouldBe "23:59"
    }

    test("Korail toScheduleCell - lastStation 비어있으면 기존 lastStation 유지") {
        val schedules = listOf(
            ProcessedKorailSchedule(time = "235900", lastStation = "", startStation = "", isFast = ""),
        )
        val result = schedules.toScheduleCell(fakeCell())
        result.lastStation shouldBe "성수행"
    }

    test("Korail toScheduleCell - isFast 값이 결과에 전달됨") {
        val schedules = listOf(
            ProcessedKorailSchedule(time = "235900", lastStation = "천안", startStation = "", isFast = "급행"),
        )
        val result = schedules.toScheduleCell(fakeCell())
        result.isFast shouldBe "급행"
    }

    test("Korail toScheduleCell - 다음 열차 있으면 type=Schedule") {
        val schedules = listOf(
            ProcessedKorailSchedule(time = "235900", lastStation = "천안", startStation = "", isFast = ""),
        )
        val result = schedules.toScheduleCell(fakeCell())
        result.type shouldBe HomeCellType.Schedule
    }

    test("Korail toScheduleCell - stateMSG 형식이 HH:mm 이다") {
        val schedules = listOf(
            ProcessedKorailSchedule(time = "235900", lastStation = "천안", startStation = "", isFast = ""),
        )
        val result = schedules.toScheduleCell(fakeCell())
        result.stateMSG shouldBe "23:59"
    }
})
