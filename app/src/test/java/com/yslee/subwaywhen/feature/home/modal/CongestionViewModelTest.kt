package com.yslee.subwaywhen.feature.home.modal

import app.cash.turbine.test
import com.yslee.subwaywhen.data.model.SaveSetting
import com.yslee.subwaywhen.data.remote.congestion.CongestionManager
import com.yslee.subwaywhen.data.repository.LocalDataRepository
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class CongestionViewModelTest : FunSpec({

    val testDispatcher = StandardTestDispatcher()

    beforeEach { Dispatchers.setMain(testDispatcher) }
    afterEach { Dispatchers.resetMain() }

    val congestionManager = mockk<CongestionManager>()
    val localDataRepository = mockk<LocalDataRepository>()

    val defaultSetting = SaveSetting(mainCongestionBaseStation = "강남")
    val settingFlow = MutableStateFlow(defaultSetting)

    val sampleData = listOf(
        HourlyCongestion(hour = 9, percent = 80, level = 7),
        HourlyCongestion(hour = 18, percent = 120, level = 9),
    )

    beforeEach {
        every { congestionManager.getAvailableStations() } returns listOf("강남", "홍대입구")
        coEvery { congestionManager.getCongestions(any()) } returns sampleData
        every { localDataRepository.saveSetting } returns settingFlow
        coEvery { localDataRepository.updateSaveSetting(any()) } returns Unit
    }

    test("OnAppear: availableStations, selectedStation, congestionData, nowHour 상태 갱신") {
        runTest(testDispatcher) {
            val vm = CongestionViewModel(congestionManager, localDataRepository)
            vm.uiState.test {
                awaitItem() // initial

                vm.onIntent(CongestionIntent.OnAppear)
                testDispatcher.scheduler.advanceUntilIdle()

                val state = awaitItem()
                state.availableStations shouldBe listOf("강남", "홍대입구")
                state.selectedStation shouldBe "강남"
                state.congestionData shouldBe sampleData
            }
        }
    }

    test("StationTap(newStation): selectedStation 변경 + updateSaveSetting 호출") {
        runTest(testDispatcher) {
            val vm = CongestionViewModel(congestionManager, localDataRepository)
            vm.onIntent(CongestionIntent.OnAppear)
            testDispatcher.scheduler.advanceUntilIdle()

            vm.onIntent(CongestionIntent.StationTap("홍대입구"))
            testDispatcher.scheduler.advanceUntilIdle()

            vm.uiState.value.selectedStation shouldBe "홍대입구"
            coVerify { localDataRepository.updateSaveSetting(match { it.mainCongestionBaseStation == "홍대입구" }) }
        }
    }

    test("StationTap(sameStation): 상태 변경 없음, updateSaveSetting 미호출") {
        runTest(testDispatcher) {
            val vm = CongestionViewModel(congestionManager, localDataRepository)
            vm.onIntent(CongestionIntent.OnAppear)
            testDispatcher.scheduler.advanceUntilIdle()

            val before = vm.uiState.value
            vm.onIntent(CongestionIntent.StationTap("강남"))
            testDispatcher.scheduler.advanceUntilIdle()

            vm.uiState.value shouldBe before
            coVerify(exactly = 0) { localDataRepository.updateSaveSetting(any()) }
        }
    }
})
