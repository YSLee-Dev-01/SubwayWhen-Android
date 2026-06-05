package com.yslee.subwaywhen.feature.edit

import com.yslee.subwaywhen.data.model.SaveStation
import com.yslee.subwaywhen.data.model.SaveStationGroup
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class EditMapperTest : FunSpec({

    fun station(id: String, group: SaveStationGroup) = SaveStation(
        id = id,
        stationName = "역$id",
        group = group,
    )

    test("출근/퇴근 혼합 리스트 - 분리 후 병합하면 원본과 동일") {
        val stations = listOf(
            station("1", SaveStationGroup.ONE),
            station("2", SaveStationGroup.TWO),
            station("3", SaveStationGroup.ONE),
            station("4", SaveStationGroup.TWO),
        )

        val split = stations.splitByGroup()
        val merged = split.mergeGroups()

        split.first shouldBe listOf(station("1", SaveStationGroup.ONE), station("3", SaveStationGroup.ONE))
        split.second shouldBe listOf(station("2", SaveStationGroup.TWO), station("4", SaveStationGroup.TWO))
        merged shouldBe split.first + split.second
    }

    test("출근(ONE)만 있는 경우 - groupTwo는 빈 리스트") {
        val stations = listOf(
            station("1", SaveStationGroup.ONE),
            station("2", SaveStationGroup.ONE),
        )

        val (groupOne, groupTwo) = stations.splitByGroup()

        groupOne.size shouldBe 2
        groupTwo shouldBe emptyList()
    }

    test("퇴근(TWO)만 있는 경우 - groupOne은 빈 리스트") {
        val stations = listOf(
            station("1", SaveStationGroup.TWO),
            station("2", SaveStationGroup.TWO),
        )

        val (groupOne, groupTwo) = stations.splitByGroup()

        groupOne shouldBe emptyList()
        groupTwo.size shouldBe 2
    }

    test("빈 리스트 - 분리/병합 모두 빈 리스트 반환") {
        val stations = emptyList<SaveStation>()

        val split = stations.splitByGroup()
        val merged = split.mergeGroups()

        split.first shouldBe emptyList()
        split.second shouldBe emptyList()
        merged shouldBe emptyList()
    }

    test("병합 순서 - groupOne(출근)이 항상 먼저") {
        val groupOne = listOf(station("A", SaveStationGroup.ONE))
        val groupTwo = listOf(station("B", SaveStationGroup.TWO))

        val merged = Pair(groupOne, groupTwo).mergeGroups()

        merged.first() shouldBe station("A", SaveStationGroup.ONE)
        merged.last() shouldBe station("B", SaveStationGroup.TWO)
    }
})
