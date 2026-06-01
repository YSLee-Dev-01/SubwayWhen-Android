package com.yslee.subwaywhen.data.repository

import com.yslee.subwaywhen.data.remote.dto.liveArrival.RealtimeStationArrival
import com.yslee.subwaywhen.data.remote.dto.vicinityStation.VicinityTransformData
import com.yslee.subwaywhen.data.remote.totalload.TotalLoadModel
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

class VicinityRepositoryImplTest : FunSpec({

    lateinit var totalLoadModel: TotalLoadModel
    lateinit var repository: VicinityRepositoryImpl

    beforeEach {
        totalLoadModel = mockk()
        repository = VicinityRepositoryImpl(totalLoadModel)
    }

    // ── loadVicinityStations ──────────────────────────────────────────────

    test("loadVicinityStations — TotalLoadModel.vicinityStations 결과 그대로 반환") {
        val expected = listOf(
            VicinityTransformData(id = "1", name = "강남", line = "2호선", distance = "0.2km"),
            VicinityTransformData(id = "2", name = "역삼", line = "2호선", distance = "0.5km"),
        )
        coEvery { totalLoadModel.vicinityStations(37.0, 127.0) } returns expected

        val result = repository.loadVicinityStations(37.0, 127.0)

        result shouldBe expected
        coVerify(exactly = 1) { totalLoadModel.vicinityStations(37.0, 127.0) }
    }

    test("loadVicinityStations — 빈 응답 시 emptyList() 반환") {
        coEvery { totalLoadModel.vicinityStations(any(), any()) } returns emptyList()

        val result = repository.loadVicinityStations(37.0, 127.0)

        result shouldBe emptyList()
    }

    // ── loadLiveArrival ───────────────────────────────────────────────────

    test("loadLiveArrival — TotalLoadModel.liveArrivalSplit 결과 그대로 반환") {
        val upArrival = RealtimeStationArrival(
            upDown = "상행", arrivalTime = "60", subPrevious = "강남 방면",
            code = "0", subWayId = "1002", stationName = "강남", lastStation = "성수",
            backStationId = "221", nextStationId = "223", trainCode = "1234"
        )
        val downArrival = RealtimeStationArrival(
            upDown = "하행", arrivalTime = "120", subPrevious = "잠실 방면",
            code = "0", subWayId = "1002", stationName = "강남", lastStation = "잠실",
            backStationId = "221", nextStationId = "223", trainCode = "5678"
        )
        val expected = Pair(listOf(upArrival), listOf(downArrival))
        coEvery { totalLoadModel.liveArrivalSplit("강남", "2호선") } returns expected

        val result = repository.loadLiveArrival("강남", "2호선")

        result shouldBe expected
        coVerify(exactly = 1) { totalLoadModel.liveArrivalSplit("강남", "2호선") }
    }
})
