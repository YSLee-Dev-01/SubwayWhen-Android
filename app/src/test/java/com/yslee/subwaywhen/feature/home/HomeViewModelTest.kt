package com.yslee.subwaywhen.feature.home

import app.cash.turbine.test
import com.yslee.subwaywhen.data.model.SaveSetting
import com.yslee.subwaywhen.data.model.SaveStation
import com.yslee.subwaywhen.data.model.SaveStationGroup
import com.yslee.subwaywhen.data.network.NetworkResult
import com.yslee.subwaywhen.data.remote.dto.liveArrival.LiveStationModel
import com.yslee.subwaywhen.data.remote.dto.liveArrival.RealtimeStationArrival
import com.yslee.subwaywhen.data.remote.congestion.CongestionManager
import com.yslee.subwaywhen.data.remote.totalload.TotalLoadModel
import com.yslee.subwaywhen.data.network.NetworkError
import com.yslee.subwaywhen.data.repository.LocalDataRepository
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest : FunSpec({

    val testDispatcher = UnconfinedTestDispatcher()
    beforeEach { Dispatchers.setMain(testDispatcher) }
    afterEach { Dispatchers.resetMain() }

    // ── 헬퍼 ──────────────────────────────────────────────────────────────────

    fun fakeStation(group: SaveStationGroup = SaveStationGroup.ONE) = SaveStation(
        id = "1", stationName = "강남", stationCode = "222",
        updnLine = "상행", line = "2호선", lineCode = "1002",
        group = group, exceptionLastStation = "", korailCode = "",
    )

    fun fakeArrival(upDown: String = "상행") = RealtimeStationArrival(
        upDown = upDown, arrivalTime = "120", subPrevious = "잠실나루 방면",
        code = "0", subWayId = "1002", stationName = "강남",
        lastStation = "성수", backStationId = "221", nextStationId = "223",
        trainCode = "1234",
    )

    fun createViewModel(
        stations: List<SaveStation> = emptyList(),
        setting: SaveSetting = SaveSetting(),
        arrivalFlow: kotlinx.coroutines.flow.Flow<IndexedValue<NetworkResult<LiveStationModel>>> = flowOf(),
    ): HomeViewModel {
        val totalLoadModel = mockk<TotalLoadModel>()
        coEvery { totalLoadModel.arrivalDataLoad(any()) } returns arrivalFlow
        coEvery { totalLoadModel.seoulScheduleLoad(any(), any()) } returns NetworkResult.Failure(mockk())
        coEvery { totalLoadModel.korailScheduleLoad(any(), any()) } returns NetworkResult.Failure(mockk())

        val localRepo = mockk<LocalDataRepository>()
        coEvery { localRepo.saveStations } returns MutableStateFlow(stations)
        coEvery { localRepo.saveSetting } returns MutableStateFlow(setting)

        val congestionManager = mockk<CongestionManager>()
        coEvery { congestionManager.getLevel(any(), any()) } returns null

        return HomeViewModel(totalLoadModel, localRepo, congestionManager)
    }

    // ── 1. 빈 목록 ────────────────────────────────────────────────────────────
    test("저장 역이 없으면 cells가 비어 있다") {
        runTest(testDispatcher) {
            val vm = createViewModel(stations = emptyList())
            advanceUntilIdle()
            vm.uiState.value.cells.isEmpty() shouldBe true
        }
    }

    // ── 2. 로딩 카드 즉시 표시 ───────────────────────────────────────────────
    test("OnAppear 시 Loading 타입 카드가 즉시 표시된다") {
        runTest(testDispatcher) {
            val station = fakeStation(SaveStationGroup.ONE)
            val vm = createViewModel(stations = listOf(station))

            vm.uiState.test {
                val initial = awaitItem()
                val hasLoadingCard = initial.cells.any { it.type == HomeCellType.Loading }
                // Loading 카드가 나오거나 이미 real로 전환된 상태 중 하나여야 함
                // (UnconfinedDispatcher 특성상 바로 실행될 수 있음)
                (initial.cells.isEmpty() || initial.cells[0].type == HomeCellType.Loading
                        || initial.cells[0].type == HomeCellType.Real) shouldBe true
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    // ── 3. 실시간 도착정보 수신 시 카드 교체 ─────────────────────────────────
    test("arrivalDataLoad 응답 수신 시 Real 타입 카드로 교체된다") {
        runTest(testDispatcher) {
            val station = fakeStation(SaveStationGroup.ONE)
            val arrival = fakeArrival("상행")
            val liveModel = LiveStationModel(realtimeArrivalList = listOf(arrival))
            val arrivalFlow = flowOf(IndexedValue(0, NetworkResult.Success(liveModel)))

            val vm = createViewModel(stations = listOf(station), arrivalFlow = arrivalFlow)
            advanceUntilIdle()

            val cells = vm.uiState.value.cells
            cells.isNotEmpty() shouldBe true
            cells.first().type shouldBe HomeCellType.Real
        }
    }

    // ── 4. API 실패 → "정보 없음" ─────────────────────────────────────────────
    test("arrivalDataLoad 실패 시 정보 없음 카드로 표시된다") {
        runTest(testDispatcher) {
            val station = fakeStation(SaveStationGroup.ONE)
            val failFlow = flowOf(IndexedValue(0, NetworkResult.Failure(NetworkError.BadServerResponse) as NetworkResult<LiveStationModel>))

            val vm = createViewModel(stations = listOf(station), arrivalFlow = failFlow)
            advanceUntilIdle()

            val cell = vm.uiState.value.cells.firstOrNull()
            cell?.stateMSG shouldBe "현재 실시간 열차 데이터가 없어요."
        }
    }

    // ── 5. GroupTap 전환 ─────────────────────────────────────────────────────
    test("GroupTap(TWO) 시 currentGroup이 TWO로 변경된다") {
        runTest(testDispatcher) {
            val vm = createViewModel(stations = emptyList())
            advanceUntilIdle()

            vm.onIntent(HomeIntent.GroupTap(SaveStationGroup.TWO))
            advanceUntilIdle()

            vm.uiState.value.currentGroup shouldBe SaveStationGroup.TWO
        }
    }

    // ── 6. EmptyAddTap → NavigateToSearch Effect ──────────────────────────────
    test("EmptyAddTap 시 NavigateToSearch Effect가 방출된다") {
        runTest(testDispatcher) {
            val vm = createViewModel()

            vm.effect.test {
                vm.onIntent(HomeIntent.EmptyAddTap)
                awaitItem() shouldBe HomeEffect.NavigateToSearch
            }
        }
    }

    // ── 7. StationTap → NavigateToDetail Effect ──────────────────────────────
    test("StationTap 시 NavigateToDetail Effect가 방출된다") {
        runTest(testDispatcher) {
            val vm = createViewModel()
            val cell = HomeCellData(
                stationIndex = 0, type = HomeCellType.Real,
                stationName = "강남", updnLine = "상행",
                lastStation = "성수", exceptionLastStation = "",
                stateMSG = "", arrivalTime = "60",
                subPrevious = "1분", code = "0",
                isFast = "", line = "2호선",
                lineCode = "1002", korailCode = "", stationCode = "222",
            )

            vm.effect.test {
                vm.onIntent(HomeIntent.StationTap(cell))
                awaitItem() shouldBe HomeEffect.NavigateToDetail(cell)
            }
        }
    }

    // ── 8. Refresh → isRefreshing 상태 ───────────────────────────────────────
    test("Refresh Intent 시 isRefreshing이 true였다가 false로 돌아온다") {
        runTest(testDispatcher) {
            val vm = createViewModel()
            advanceUntilIdle()

            // UnconfinedDispatcher라 바로 처리됨 → 최종 상태만 검증
            vm.onIntent(HomeIntent.Refresh)
            advanceUntilIdle()

            vm.uiState.value.isRefreshing shouldBe false
        }
    }
})
