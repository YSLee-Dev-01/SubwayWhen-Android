package com.yslee.subwaywhen.data.repository

import com.yslee.subwaywhen.data.network.NetworkResult
import com.yslee.subwaywhen.data.remote.dto.vicinityStation.VicinityDocumentData
import com.yslee.subwaywhen.data.remote.dto.vicinityStation.VicinityStationsData
import com.yslee.subwaywhen.data.remote.dto.vicinityStation.VicinityTransformData
import com.yslee.subwaywhen.data.remote.loadmodel.LoadModel
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk

class VicinityRepositoryImplTest : FunSpec({

    lateinit var loadModel: LoadModel
    lateinit var repository: VicinityRepositoryImpl

    beforeEach {
        loadModel = mockk()
        repository = VicinityRepositoryImpl(loadModel)
    }

    // ── category 필터링 ──────────────────────────────────────────────────────

    test("category != SW8 항목은 결과에 포함되지 않음") {
        val documents = listOf(
            VicinityDocumentData(name = "강남역 2호선", distance = "200", category = "SW8"),
            VicinityDocumentData(name = "스타벅스", distance = "100", category = "CE7")
        )
        coEvery { loadModel.vicinityStationsLoad(any(), any()) } returns
            NetworkResult.Success(VicinityStationsData(documents))

        val result = repository.loadVicinityStations(37.0, 127.0)

        result.size shouldBe 1
        result[0].name shouldBe "강남"
    }

    // ── 거리 정렬 ────────────────────────────────────────────────────────────

    test("거리 기준 오름차순 정렬") {
        val documents = listOf(
            VicinityDocumentData(name = "선릉역 2호선", distance = "800", category = "SW8"),
            VicinityDocumentData(name = "강남역 2호선", distance = "200", category = "SW8"),
            VicinityDocumentData(name = "역삼역 2호선", distance = "500", category = "SW8")
        )
        coEvery { loadModel.vicinityStationsLoad(any(), any()) } returns
            NetworkResult.Success(VicinityStationsData(documents))

        val result = repository.loadVicinityStations(37.0, 127.0)

        result.map { it.name } shouldBe listOf("강남", "역삼", "선릉")
    }

    // ── place_name 파싱 ──────────────────────────────────────────────────────

    test("place_name에서 역명과 호선 분리") {
        val documents = listOf(
            VicinityDocumentData(name = "홍대입구역 2호선", distance = "300", category = "SW8")
        )
        coEvery { loadModel.vicinityStationsLoad(any(), any()) } returns
            NetworkResult.Success(VicinityStationsData(documents))

        val result = repository.loadVicinityStations(37.0, 127.0)

        result[0].name shouldBe "홍대입구"
        result[0].line shouldBe "2호선"
    }

    test("place_name에 역이 없으면 name·line 모두 정보없음") {
        val documents = listOf(
            VicinityDocumentData(name = "홍대입구 2호선", distance = "300", category = "SW8")
        )
        coEvery { loadModel.vicinityStationsLoad(any(), any()) } returns
            NetworkResult.Success(VicinityStationsData(documents))

        val result = repository.loadVicinityStations(37.0, 127.0)

        result[0].name shouldBe "정보없음"
        result[0].line shouldBe "정보없음"
    }

    // ── distance 변환 ────────────────────────────────────────────────────────

    test("distance 1500 → 1.5km 변환") {
        val documents = listOf(
            VicinityDocumentData(name = "강남역 2호선", distance = "1500", category = "SW8")
        )
        coEvery { loadModel.vicinityStationsLoad(any(), any()) } returns
            NetworkResult.Success(VicinityStationsData(documents))

        val result = repository.loadVicinityStations(37.0, 127.0)

        result[0].distance shouldBe "1.5km"
    }

    test("distance 200 → 0.2km 변환") {
        val documents = listOf(
            VicinityDocumentData(name = "강남역 2호선", distance = "200", category = "SW8")
        )
        coEvery { loadModel.vicinityStationsLoad(any(), any()) } returns
            NetworkResult.Success(VicinityStationsData(documents))

        val result = repository.loadVicinityStations(37.0, 127.0)

        result[0].distance shouldBe "0.2km"
    }

    // ── 빈 응답 ──────────────────────────────────────────────────────────────

    test("빈 응답 시 emptyList() 반환") {
        coEvery { loadModel.vicinityStationsLoad(any(), any()) } returns
            NetworkResult.Success(VicinityStationsData(emptyList()))

        val result = repository.loadVicinityStations(37.0, 127.0)

        result shouldBe emptyList()
    }

    // ── 네트워크 에러 ────────────────────────────────────────────────────────

    test("네트워크 에러 시 emptyList() 반환") {
        coEvery { loadModel.vicinityStationsLoad(any(), any()) } returns
            NetworkResult.Failure(mockk())

        val result = repository.loadVicinityStations(37.0, 127.0)

        result shouldBe emptyList()
    }
})
