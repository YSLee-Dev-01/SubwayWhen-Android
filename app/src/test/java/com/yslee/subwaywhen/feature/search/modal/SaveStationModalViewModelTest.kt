package com.yslee.subwaywhen.feature.search.modal

import app.cash.turbine.test
import com.google.firebase.analytics.FirebaseAnalytics
import com.yslee.subwaywhen.data.model.SaveSetting
import com.yslee.subwaywhen.data.model.SaveStation
import com.yslee.subwaywhen.data.model.SaveStationGroup
import com.yslee.subwaywhen.data.remote.dto.stationSearch.SearchStationInfo
import com.yslee.subwaywhen.data.repository.LocalDataRepository
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class SaveStationModalViewModelTest : FunSpec({

    val testDispatcher = UnconfinedTestDispatcher()

    val testStation = SearchStationInfo(
        stationName = "강남",
        line = "02호선",
        stationCode = "222",
    )

    beforeEach { Dispatchers.setMain(testDispatcher) }
    afterEach { Dispatchers.resetMain() }

    val fakeAnalytics = mockk<FirebaseAnalytics>(relaxed = true)

    fun fakeRepository(
        stations: List<SaveStation> = emptyList(),
        overlapAlert: Boolean = true,
    ): FakeLocalDataRepository = FakeLocalDataRepository(stations, overlapAlert)

    // ── Test 1. 신규 저장 성공 ──────────────────────────────────────
    test("UpButtonTapped → SaveCompleted Effect 방출 및 saveStations에 추가") {
        val repo = fakeRepository()
        val vm = SaveStationModalViewModel(repo, fakeAnalytics)
        vm.initStation(testStation)

        runTest(testDispatcher) {
            vm.effect.test {
                vm.onIntent(SaveStationModalIntent.UpButtonTapped)
                awaitItem() shouldBe SaveStationModalEffect.SaveCompleted
            }
            repo.savedStations.size shouldBe 1
            repo.savedStations[0].stationName shouldBe "강남"
            repo.savedStations[0].updnLine shouldBe "내선" // 02호선 isUp=true
        }
    }

    // ── Test 2. 중복 저장 차단 ──────────────────────────────────────
    test("중복 역 저장 시 AlreadyExists Effect 방출, updateSaveStations 미호출") {
        val existing = SaveStation(
            id = "1",
            stationName = "강남",
            updnLine = "내선",
            line = "02호선",
            lineCode = "1002",
        )
        val repo = fakeRepository(stations = listOf(existing), overlapAlert = true)
        val vm = SaveStationModalViewModel(repo, fakeAnalytics)
        vm.initStation(testStation)

        runTest(testDispatcher) {
            vm.effect.test {
                vm.onIntent(SaveStationModalIntent.UpButtonTapped)
                awaitItem() shouldBe SaveStationModalEffect.AlreadyExists
            }
            repo.savedStations.size shouldBe 1 // 추가 없음
        }
    }

    // ── Test 3. 중복 허용 (searchOverlapAlert=false) ─────────────────
    test("overlapAlert=false 이면 중복 역도 SaveCompleted 방출") {
        val existing = SaveStation(
            id = "1",
            stationName = "강남",
            updnLine = "내선",
            line = "02호선",
            lineCode = "1002",
        )
        val repo = fakeRepository(stations = listOf(existing), overlapAlert = false)
        val vm = SaveStationModalViewModel(repo, fakeAnalytics)
        vm.initStation(testStation)

        runTest(testDispatcher) {
            vm.effect.test {
                vm.onIntent(SaveStationModalIntent.UpButtonTapped)
                awaitItem() shouldBe SaveStationModalEffect.SaveCompleted
            }
            repo.savedStations.size shouldBe 2
        }
    }

    // ── Test 4. 그룹 토글 ──────────────────────────────────────────
    test("GroupToggled → ONE→TWO, 재호출 시 TWO→ONE") {
        val repo = fakeRepository()
        val vm = SaveStationModalViewModel(repo, fakeAnalytics)

        vm.uiState.value.group shouldBe SaveStationGroup.ONE
        vm.onIntent(SaveStationModalIntent.GroupToggled)
        vm.uiState.value.group shouldBe SaveStationGroup.TWO
        vm.onIntent(SaveStationModalIntent.GroupToggled)
        vm.uiState.value.group shouldBe SaveStationGroup.ONE
    }

    // ── Test 5. 중간 종착역 입력 ────────────────────────────────────
    test("ExceptionChanged 후 저장 시 exceptionLastStation 에 반영") {
        val repo = fakeRepository()
        val vm = SaveStationModalViewModel(repo, fakeAnalytics)
        vm.initStation(testStation)

        vm.onIntent(SaveStationModalIntent.ExceptionChanged("잠실,선릉"))

        runTest(testDispatcher) {
            vm.effect.test {
                vm.onIntent(SaveStationModalIntent.UpButtonTapped)
                awaitItem() shouldBe SaveStationModalEffect.SaveCompleted
            }
            repo.savedStations[0].exceptionLastStation shouldBe "잠실,선릉"
        }
    }
})

// ── Fake Repository ────────────────────────────────────────────────
private class FakeLocalDataRepository(
    initialStations: List<SaveStation>,
    overlapAlert: Boolean,
) : LocalDataRepository {

    val savedStations: MutableList<SaveStation> = initialStations.toMutableList()

    override val saveSetting = MutableStateFlow(SaveSetting(searchOverlapAlert = overlapAlert))
    override val saveStations = MutableStateFlow(savedStations.toList())
    override val isInitialized = MutableStateFlow(true)

    override suspend fun updateSaveSetting(setting: SaveSetting) {
        saveSetting.value = setting
    }

    override suspend fun updateSaveStations(stations: List<SaveStation>) {
        savedStations.clear()
        savedStations.addAll(stations)
        saveStations.value = savedStations.toList()
    }
}
