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

    /** flatItems에서 특정 그룹 헤더 뒤에 오는 Station 목록 추출 */
    fun List<EditFlatItem>.stationsInGroup(group: SaveStationGroup): List<SaveStation> {
        var inGroup = false
        val result = mutableListOf<SaveStation>()
        for (item in this) {
            when {
                item is EditFlatItem.Header -> inGroup = (item.group == group)
                item is EditFlatItem.Station && inGroup -> result.add(item.station)
            }
        }
        return result
    }

    // ── 1. 초기 로드 ──────────────────────────────────────────────────────────
    test("초기 로드 시 flatItems에 Header와 Station이 순서대로 배치되고 isSaveEnabled = false") {
        runTest(testDispatcher) {
            val stationOne = fakeStation("1", SaveStationGroup.ONE)
            val stationTwo = fakeStation("2", SaveStationGroup.TWO)
            val (vm, _) = createViewModel(stations = listOf(stationOne, stationTwo))

            val items = vm.uiState.value.flatItems
            items.stationsInGroup(SaveStationGroup.ONE) shouldBe listOf(stationOne)
            items.stationsInGroup(SaveStationGroup.TWO) shouldBe listOf(stationTwo)
            vm.uiState.value.isSaveEnabled shouldBe false
        }
    }

    // ── 2. DeleteStation - ONE 그룹 삭제 ──────────────────────────────────────
    test("DeleteStation - ONE 그룹 항목 삭제 시 flatItems에서 제거되고 isSaveEnabled = true") {
        runTest(testDispatcher) {
            val station1 = fakeStation("1", SaveStationGroup.ONE)
            val station2 = fakeStation("2", SaveStationGroup.ONE)
            val (vm, _) = createViewModel(stations = listOf(station1, station2))

            vm.onIntent(EditIntent.DeleteStation(station1))

            val items = vm.uiState.value.flatItems
            items.stationsInGroup(SaveStationGroup.ONE) shouldBe listOf(station2)
            vm.uiState.value.isSaveEnabled shouldBe true
        }
    }

    // ── 3. DeleteStation - TWO 그룹 삭제 ──────────────────────────────────────
    test("DeleteStation - TWO 그룹 항목 삭제 시 flatItems에서 제거되고 isSaveEnabled = true") {
        runTest(testDispatcher) {
            val station1 = fakeStation("1", SaveStationGroup.TWO)
            val station2 = fakeStation("2", SaveStationGroup.TWO)
            val (vm, _) = createViewModel(stations = listOf(station1, station2))

            vm.onIntent(EditIntent.DeleteStation(station2))

            val items = vm.uiState.value.flatItems
            items.stationsInGroup(SaveStationGroup.TWO) shouldBe listOf(station1)
            vm.uiState.value.isSaveEnabled shouldBe true
        }
    }

    // ── 4. DeleteStation 후 빈 섹션에 DropTarget 삽입 ─────────────────────────
    test("DeleteStation으로 섹션이 비어지면 DropTarget이 삽입된다") {
        runTest(testDispatcher) {
            val station1 = fakeStation("1", SaveStationGroup.ONE)
            val (vm, _) = createViewModel(stations = listOf(station1))

            vm.onIntent(EditIntent.DeleteStation(station1))

            val hasDropTarget = vm.uiState.value.flatItems
                .any { it is EditFlatItem.DropTarget && it.group == SaveStationGroup.ONE }
            hasDropTarget shouldBe true
        }
    }

    // ── 5. Reorder - 동일 섹션 순서 변경 ──────────────────────────────────────
    test("Reorder - 동일 섹션 내 순서 변경 시 flatItems 갱신되고 isSaveEnabled = true") {
        runTest(testDispatcher) {
            val station1 = fakeStation("1", SaveStationGroup.ONE)
            val station2 = fakeStation("2", SaveStationGroup.ONE)
            val (vm, _) = createViewModel(stations = listOf(station1, station2))

            // station1 과 station2 순서 교환
            val newItems = listOf(
                EditFlatItem.Header("출근", SaveStationGroup.ONE),
                EditFlatItem.Station(station2),
                EditFlatItem.Station(station1),
                EditFlatItem.Header("퇴근", SaveStationGroup.TWO),
                EditFlatItem.DropTarget(SaveStationGroup.TWO),
            )
            vm.onIntent(EditIntent.Reorder(newItems))

            vm.uiState.value.flatItems.stationsInGroup(SaveStationGroup.ONE) shouldBe listOf(station2, station1)
            vm.uiState.value.isSaveEnabled shouldBe true
        }
    }

    // ── 6. Reorder - 섹션 간 이동 ─────────────────────────────────────────────
    test("Reorder - station1을 TWO 섹션으로 이동 시 SaveTap하면 group=TWO로 저장된다") {
        runTest(testDispatcher) {
            val station1 = fakeStation("1", SaveStationGroup.ONE)
            val station2 = fakeStation("2", SaveStationGroup.TWO)
            val (vm, repo) = createViewModel(stations = listOf(station1, station2))

            // station1을 TWO 섹션으로 이동한 플랫 리스트
            val newItems = listOf(
                EditFlatItem.Header("출근", SaveStationGroup.ONE),
                EditFlatItem.DropTarget(SaveStationGroup.ONE),
                EditFlatItem.Header("퇴근", SaveStationGroup.TWO),
                EditFlatItem.Station(station1),  // TWO 섹션에 위치
                EditFlatItem.Station(station2),
            )
            vm.onIntent(EditIntent.Reorder(newItems))
            vm.onIntent(EditIntent.SaveTap)

            coVerify {
                repo.updateSaveStations(match { saved ->
                    saved.any { it.id == "1" && it.group == SaveStationGroup.TWO }
                })
            }
        }
    }

    // ── 7. SaveTap ────────────────────────────────────────────────────────────
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

    // ── 8. BackTap (dirty) ────────────────────────────────────────────────────
    test("변경 후 BackTap 시 showNotSaveDialog = true, Effect 없음") {
        runTest(testDispatcher) {
            val station1 = fakeStation("1", SaveStationGroup.ONE)
            val station2 = fakeStation("2", SaveStationGroup.ONE)
            val (vm, _) = createViewModel(stations = listOf(station1, station2))

            vm.onIntent(EditIntent.DeleteStation(station1))

            vm.effect.test {
                vm.onIntent(EditIntent.BackTap)
                expectNoEvents()
            }

            vm.uiState.value.showNotSaveDialog shouldBe true
        }
    }

    // ── 9. BackTap (clean) ────────────────────────────────────────────────────
    test("변경 없이 BackTap 시 NavigateBack Effect 방출") {
        runTest(testDispatcher) {
            val (vm, _) = createViewModel(stations = listOf(fakeStation("1", SaveStationGroup.ONE)))

            vm.effect.test {
                vm.onIntent(EditIntent.BackTap)
                awaitItem() shouldBe EditEffect.NavigateBack
            }
        }
    }

    // ── 10. DialogDiscard ─────────────────────────────────────────────────────
    test("DialogDiscard 시 NavigateBack Effect 방출") {
        runTest(testDispatcher) {
            val (vm, _) = createViewModel()

            vm.effect.test {
                vm.onIntent(EditIntent.DialogDiscard)
                awaitItem() shouldBe EditEffect.NavigateBack
            }
        }
    }

    // ── 11. DialogCancel ──────────────────────────────────────────────────────
    test("DialogCancel 시 showNotSaveDialog = false, Effect 없음") {
        runTest(testDispatcher) {
            val station1 = fakeStation("1", SaveStationGroup.ONE)
            val station2 = fakeStation("2", SaveStationGroup.ONE)
            val (vm, _) = createViewModel(stations = listOf(station1, station2))

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
