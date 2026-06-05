package com.yslee.subwaywhen.feature.edit

import app.cash.turbine.test
import com.yslee.subwaywhen.data.model.SaveSetting
import com.yslee.subwaywhen.data.model.SaveStation
import com.yslee.subwaywhen.data.model.SaveStationGroup
import com.yslee.subwaywhen.data.repository.LocalDataRepository
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class EditViewModelTest : FunSpec({

    val testDispatcher = UnconfinedTestDispatcher()
    beforeEach { Dispatchers.setMain(testDispatcher) }
    afterEach { Dispatchers.resetMain() }

    // ── 헬퍼 ──────────────────────────────────────────────────────────────────

    fun fakeStation(id: String, group: SaveStationGroup = SaveStationGroup.ONE) = SaveStation(
        id = id,
        stationName = "역$id",
        stationCode = "code$id",
        updnLine = "상행",
        line = "2호선",
        lineCode = "1002",
        group = group,
        exceptionLastStation = "",
        korailCode = "",
    )

    fun createViewModel(stations: List<SaveStation> = emptyList()): Pair<EditViewModel, LocalDataRepository> {
        val localDataRepository = mockk<LocalDataRepository>()
        coEvery { localDataRepository.saveStations } returns MutableStateFlow(stations)
        coEvery { localDataRepository.saveSetting } returns MutableStateFlow(SaveSetting())
        coEvery { localDataRepository.isInitialized } returns MutableStateFlow(true)
        coEvery { localDataRepository.updateSaveStations(any()) } returns Unit
        return EditViewModel(localDataRepository) to localDataRepository
    }

    // ── 1. 초기 로드 ──────────────────────────────────────────────────────────
    test("초기 로드 시 groupOne에는 ONE, groupTwo에는 TWO 그룹만 있고 isSaveEnabled = false") {
        runTest(testDispatcher) {
            val stationOne = fakeStation("1", SaveStationGroup.ONE)
            val stationTwo = fakeStation("2", SaveStationGroup.TWO)
            val (vm, _) = createViewModel(stations = listOf(stationOne, stationTwo))

            vm.uiState.value.groupOne shouldBe listOf(stationOne)
            vm.uiState.value.groupTwo shouldBe listOf(stationTwo)
            vm.uiState.value.isSaveEnabled shouldBe false
        }
    }

    // ── 2. DeleteStation - groupOne 삭제 ──────────────────────────────────────
    test("DeleteStation - groupOne의 항목 삭제 시 groupOne에서 제거되고 isSaveEnabled = true") {
        runTest(testDispatcher) {
            val station1 = fakeStation("1", SaveStationGroup.ONE)
            val station2 = fakeStation("2", SaveStationGroup.ONE)
            val (vm, _) = createViewModel(stations = listOf(station1, station2))

            vm.onIntent(EditIntent.DeleteStation(station1))

            vm.uiState.value.groupOne shouldBe listOf(station2)
            vm.uiState.value.isSaveEnabled shouldBe true
        }
    }

    // ── 3. DeleteStation - groupTwo 삭제 ──────────────────────────────────────
    test("DeleteStation - groupTwo의 항목 삭제 시 groupTwo에서 제거되고 isSaveEnabled = true") {
        runTest(testDispatcher) {
            val station1 = fakeStation("1", SaveStationGroup.TWO)
            val station2 = fakeStation("2", SaveStationGroup.TWO)
            val (vm, _) = createViewModel(stations = listOf(station1, station2))

            vm.onIntent(EditIntent.DeleteStation(station2))

            vm.uiState.value.groupTwo shouldBe listOf(station1)
            vm.uiState.value.isSaveEnabled shouldBe true
        }
    }

    // ── 4. MoveStation - 동일 섹션 순서 변경 ──────────────────────────────────
    test("MoveStation - 동일 섹션 내 groupOne[0]을 groupOne[1]로 이동하면 순서가 바뀌고 isSaveEnabled = true") {
        runTest(testDispatcher) {
            val station1 = fakeStation("1", SaveStationGroup.ONE)
            val station2 = fakeStation("2", SaveStationGroup.ONE)
            val (vm, _) = createViewModel(stations = listOf(station1, station2))

            vm.onIntent(EditIntent.MoveStation(fromSection = 0, fromIndex = 0, toSection = 0, toIndex = 1))

            vm.uiState.value.groupOne shouldBe listOf(station2, station1)
            vm.uiState.value.isSaveEnabled shouldBe true
        }
    }

    // ── 5. MoveStation - cross-section ────────────────────────────────────────
    test("MoveStation - groupOne[0]을 groupTwo로 이동 시 group 필드가 TWO로 변경되고 groupOne에서 제거된다") {
        runTest(testDispatcher) {
            val station1 = fakeStation("1", SaveStationGroup.ONE)
            val station2 = fakeStation("2", SaveStationGroup.TWO)
            val (vm, _) = createViewModel(stations = listOf(station1, station2))

            vm.onIntent(EditIntent.MoveStation(fromSection = 0, fromIndex = 0, toSection = 1, toIndex = 0))

            val state = vm.uiState.value
            state.groupOne shouldBe emptyList()
            state.groupTwo.any { it.id == "1" && it.group == SaveStationGroup.TWO } shouldBe true
        }
    }

    // ── 6. SaveTap ────────────────────────────────────────────────────────────
    test("SaveTap 시 updateSaveStations 호출되고 NavigateBack Effect 방출, isSaveEnabled = false") {
        runTest(testDispatcher) {
            val station1 = fakeStation("1", SaveStationGroup.ONE)
            val station2 = fakeStation("2", SaveStationGroup.ONE)
            val (vm, repo) = createViewModel(stations = listOf(station1, station2))

            // dirty 상태로 만들기
            vm.onIntent(EditIntent.DeleteStation(station1))

            vm.effect.test {
                vm.onIntent(EditIntent.SaveTap)
                awaitItem() shouldBe EditEffect.NavigateBack
            }

            coVerify { repo.updateSaveStations(any()) }
            vm.uiState.value.isSaveEnabled shouldBe false
        }
    }

    // ── 7. BackTap (dirty) ────────────────────────────────────────────────────
    test("변경 후 BackTap 시 showNotSaveDialog = true, Effect 없음") {
        runTest(testDispatcher) {
            val station1 = fakeStation("1", SaveStationGroup.ONE)
            val station2 = fakeStation("2", SaveStationGroup.ONE)
            val (vm, _) = createViewModel(stations = listOf(station1, station2))

            // dirty 상태로 만들기
            vm.onIntent(EditIntent.DeleteStation(station1))

            vm.effect.test {
                vm.onIntent(EditIntent.BackTap)
                expectNoEvents()
            }

            vm.uiState.value.showNotSaveDialog shouldBe true
        }
    }

    // ── 8. BackTap (clean) ────────────────────────────────────────────────────
    test("변경 없이 BackTap 시 NavigateBack Effect 방출") {
        runTest(testDispatcher) {
            val (vm, _) = createViewModel(stations = listOf(fakeStation("1", SaveStationGroup.ONE)))

            vm.effect.test {
                vm.onIntent(EditIntent.BackTap)
                awaitItem() shouldBe EditEffect.NavigateBack
            }
        }
    }

    // ── 9. DialogDiscard ──────────────────────────────────────────────────────
    test("DialogDiscard 시 NavigateBack Effect 방출") {
        runTest(testDispatcher) {
            val (vm, _) = createViewModel()

            vm.effect.test {
                vm.onIntent(EditIntent.DialogDiscard)
                awaitItem() shouldBe EditEffect.NavigateBack
            }
        }
    }

    // ── 10. DialogCancel ──────────────────────────────────────────────────────
    test("DialogCancel 시 showNotSaveDialog = false, Effect 없음") {
        runTest(testDispatcher) {
            val station1 = fakeStation("1", SaveStationGroup.ONE)
            val station2 = fakeStation("2", SaveStationGroup.ONE)
            val (vm, _) = createViewModel(stations = listOf(station1, station2))

            // showNotSaveDialog = true 상태로 만들기
            vm.onIntent(EditIntent.DeleteStation(station1))
            vm.onIntent(EditIntent.BackTap)
            vm.uiState.value.showNotSaveDialog shouldBe true

            vm.effect.test {
                vm.onIntent(EditIntent.DialogCancel)
                expectNoEvents()
            }

            vm.uiState.value.showNotSaveDialog shouldBe false
        }
    }
})
