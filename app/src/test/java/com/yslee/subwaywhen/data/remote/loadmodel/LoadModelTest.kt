package com.yslee.subwaywhen.data.remote.loadmodel

import com.yslee.subwaywhen.data.network.NetworkManager
import com.yslee.subwaywhen.data.network.NetworkResult
import com.yslee.subwaywhen.data.network.TokenKey
import com.yslee.subwaywhen.data.network.TokenProvider
import com.yslee.subwaywhen.data.remote.dto.liveArrival.LiveStationModel
import com.yslee.subwaywhen.data.remote.dto.realtimePosition.RealtimeTrainPositionResponse
import com.yslee.subwaywhen.data.remote.dto.scheduleArrival.korail.KorailHeader
import com.yslee.subwaywhen.data.remote.dto.scheduleArrival.seoul.ScheduleStationModel
import com.yslee.subwaywhen.data.remote.dto.stationSearch.SearchStation
import com.yslee.subwaywhen.data.remote.dto.subwayNotice.SubwayNoticeResponse
import com.yslee.subwaywhen.data.remote.dto.vicinityStation.VicinityStationsData
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.serialization.KSerializer

class LoadModelTest : FunSpec({

    lateinit var networkManager: NetworkManager
    lateinit var tokenProvider: TokenProvider
    lateinit var loadModel: LoadModelImpl

    beforeEach {
        networkManager = mockk()
        tokenProvider = mockk()
        loadModel = LoadModelImpl(networkManager, tokenProvider)

        every { tokenProvider.token(TokenKey.LIVE) } returns "live-token"
        every { tokenProvider.token(TokenKey.SEOUL) } returns "seoul-token"
        every { tokenProvider.token(TokenKey.KORAIL) } returns "korail-token"
        every { tokenProvider.token(TokenKey.KAKAO) } returns "kakao-token"
        every { tokenProvider.token(TokenKey.REALTIME) } returns "realtime-token"
    }

    // ── stationArrivalRequest ──────────────────────────────────────────────

    test("stationArrivalRequest — URL에 LIVE 토큰과 역명이 포함된다") {
        val urlSlot = slot<String>()
        val fakeResult = NetworkResult.Success(mockk<LiveStationModel>())

        coEvery {
            networkManager.requestData(capture(urlSlot), any<KSerializer<LiveStationModel>>())
        } returns fakeResult

        val result = loadModel.stationArrivalRequest("홍대입구")

        result shouldBe fakeResult
        urlSlot.captured shouldContain "live-token"
        urlSlot.captured shouldContain "홍대입구"
        urlSlot.captured shouldContain "realtimeStationArrival"
    }

    test("stationArrivalRequest — 부역명 치환: '쌍용' → '쌍용(나사렛대)'") {
        val urlSlot = slot<String>()

        coEvery {
            networkManager.requestData(capture(urlSlot), any<KSerializer<LiveStationModel>>())
        } returns NetworkResult.Success(mockk())

        loadModel.stationArrivalRequest("쌍용")

        urlSlot.captured shouldContain "쌍용(나사렛대)"
    }

    test("stationArrivalRequest — 부역명 치환: '서울' → '서울역'") {
        val urlSlot = slot<String>()

        coEvery {
            networkManager.requestData(capture(urlSlot), any<KSerializer<LiveStationModel>>())
        } returns NetworkResult.Success(mockk())

        loadModel.stationArrivalRequest("서울")

        urlSlot.captured shouldContain "서울역"
    }

    test("stationArrivalRequest — 매핑 없는 역명은 그대로 사용된다") {
        val urlSlot = slot<String>()

        coEvery {
            networkManager.requestData(capture(urlSlot), any<KSerializer<LiveStationModel>>())
        } returns NetworkResult.Success(mockk())

        loadModel.stationArrivalRequest("강남")

        urlSlot.captured shouldContain "강남"
    }

    test("stationArrivalRequest — 공백 있는 역명은 %20으로 인코딩된다 (을지로 3가)") {
        val urlSlot = slot<String>()

        coEvery {
            networkManager.requestData(capture(urlSlot), any<KSerializer<LiveStationModel>>())
        } returns NetworkResult.Success(mockk())

        loadModel.stationArrivalRequest("을지로 3가")

        urlSlot.captured shouldContain "을지로%203가"
    }

    // ── seoulStationScheduleLoad ───────────────────────────────────────────

    test("seoulStationScheduleLoad — SEOUL 토큰 키로 조회된다") {
        val urlSlot = slot<String>()

        coEvery {
            networkManager.requestData(capture(urlSlot), any<KSerializer<ScheduleStationModel>>())
        } returns NetworkResult.Success(mockk())

        loadModel.seoulStationScheduleLoad("1234", "weekday", "상행", "02호선")

        urlSlot.captured shouldContain "seoul-token"
        urlSlot.captured shouldContain "SearchSTNTimeTableByFRCodeService"
    }

    test("seoulStationScheduleLoad — 일반 호선 상행 → inOut=1") {
        val urlSlot = slot<String>()

        coEvery {
            networkManager.requestData(capture(urlSlot), any<KSerializer<ScheduleStationModel>>())
        } returns NetworkResult.Success(mockk())

        loadModel.seoulStationScheduleLoad("1234", "weekday", "상행", "02호선")

        // weekday=1, inOut=1 → .../1234/1/1
        urlSlot.captured shouldContain "/1234/1/1"
    }

    test("seoulStationScheduleLoad — 9호선 상행 → inOut=2 (방향 반전)") {
        val urlSlot = slot<String>()

        coEvery {
            networkManager.requestData(capture(urlSlot), any<KSerializer<ScheduleStationModel>>())
        } returns NetworkResult.Success(mockk())

        loadModel.seoulStationScheduleLoad("1234", "weekday", "상행", "1009")

        // weekday=1, inOut=2 → .../1234/1/2
        urlSlot.captured shouldContain "/1234/1/2"
    }

    test("seoulStationScheduleLoad — 9호선 하행 → inOut=1 (방향 반전)") {
        val urlSlot = slot<String>()

        coEvery {
            networkManager.requestData(capture(urlSlot), any<KSerializer<ScheduleStationModel>>())
        } returns NetworkResult.Success(mockk())

        loadModel.seoulStationScheduleLoad("1234", "weekday", "하행", "1009")

        urlSlot.captured shouldContain "/1234/1/1"
    }

    test("seoulStationScheduleLoad — 'K' 포함 stationCode → BadUrl Failure 반환") {
        val result = loadModel.seoulStationScheduleLoad("K123", "weekday", "상행", "02호선")

        result.shouldBeInstanceOf<NetworkResult.Failure>()
    }

    test("seoulStationScheduleLoad — 'D' 포함 stationCode → BadUrl Failure 반환") {
        val result = loadModel.seoulStationScheduleLoad("D456", "weekday", "하행", "02호선")

        result.shouldBeInstanceOf<NetworkResult.Failure>()
    }

    // ── korailScheduleLoad ─────────────────────────────────────────────────

    test("korailScheduleLoad — KORAIL 토큰 키로 조회된다") {
        val querySlot = slot<Map<String, String>>()

        coEvery {
            networkManager.requestData(
                any(),
                any<Map<String, String>>(),
                capture(querySlot),
                any<KSerializer<KorailHeader>>()
            )
        } returns NetworkResult.Success(mockk())

        loadModel.korailScheduleLoad("1234", "weekday", "01")

        querySlot.captured["serviceKey"] shouldBe "korail-token"
    }

    test("korailScheduleLoad — weekday → dayCd=8, 그 외 → dayCd=9") {
        val weekdayQuery = slot<Map<String, String>>()
        val weekendQuery = slot<Map<String, String>>()

        coEvery {
            networkManager.requestData(
                any(), any<Map<String, String>>(), capture(weekdayQuery), any<KSerializer<KorailHeader>>()
            )
        } returns NetworkResult.Success(mockk())

        loadModel.korailScheduleLoad("1234", "weekday", "01")
        weekdayQuery.captured["dayCd"] shouldBe "8"

        coEvery {
            networkManager.requestData(
                any(), any<Map<String, String>>(), capture(weekendQuery), any<KSerializer<KorailHeader>>()
            )
        } returns NetworkResult.Success(mockk())

        loadModel.korailScheduleLoad("1234", "saturday", "01")
        weekendQuery.captured["dayCd"] shouldBe "9"
    }

    // ── stationSearch ──────────────────────────────────────────────────────

    test("stationSearch — URL에 SEOUL 토큰과 역명이 포함된다") {
        val urlSlot = slot<String>()

        coEvery {
            networkManager.requestData(capture(urlSlot), any<KSerializer<SearchStation>>())
        } returns NetworkResult.Success(mockk())

        loadModel.stationSearch("강남")

        urlSlot.captured shouldContain "seoul-token"
        urlSlot.captured shouldContain "강남"
        urlSlot.captured shouldContain "SearchInfoBySubwayNameService"
    }

    // ── vicinityStationsLoad ───────────────────────────────────────────────

    test("vicinityStationsLoad — KAKAO 토큰이 Authorization 헤더로 전달된다") {
        val headersSlot = slot<Map<String, String>>()

        coEvery {
            networkManager.requestData(
                any(), capture(headersSlot), any<Map<String, String>>(), any<KSerializer<VicinityStationsData>>()
            )
        } returns NetworkResult.Success(mockk())

        loadModel.vicinityStationsLoad("127.0", "37.5")

        headersSlot.captured["Authorization"] shouldBe "KakaoAK kakao-token"
    }

    // ── subwayNoticeRequest ────────────────────────────────────────────────

    test("subwayNoticeRequest — URL에 SEOUL 토큰이 포함된다") {
        val urlSlot = slot<String>()

        coEvery {
            networkManager.requestData(capture(urlSlot), any<KSerializer<SubwayNoticeResponse>>())
        } returns NetworkResult.Success(mockk())

        loadModel.subwayNoticeRequest()

        urlSlot.captured shouldContain "seoul-token"
        urlSlot.captured shouldContain "getNtceList"
    }

    // ── realtimePositionRequest ────────────────────────────────────────────

    test("realtimePositionRequest — URL에 REALTIME 토큰과 호선명이 포함된다") {
        val urlSlot = slot<String>()

        coEvery {
            networkManager.requestData(capture(urlSlot), any<KSerializer<RealtimeTrainPositionResponse>>())
        } returns NetworkResult.Success(mockk())

        loadModel.realtimePositionRequest("1호선")

        urlSlot.captured shouldContain "realtime-token"
        urlSlot.captured shouldContain "1호선"
        urlSlot.captured shouldContain "realtimePosition"
    }
})
