package com.yslee.subwaywhen.feature.setting

import com.yslee.subwaywhen.data.model.SaveSetting
import com.yslee.subwaywhen.data.remote.totalload.TotalLoadModel
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
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class SettingViewModelTest : FunSpec({

    val testDispatcher = UnconfinedTestDispatcher()
    beforeEach { Dispatchers.setMain(testDispatcher) }
    afterEach { Dispatchers.resetMain() }

    fun createViewModel(
        initialSetting: SaveSetting = SaveSetting(),
    ): Triple<SettingViewModel, LocalDataRepository, TotalLoadModel> {
        val localRepo = mockk<LocalDataRepository>()
        coEvery { localRepo.saveSetting } returns MutableStateFlow(initialSetting)
        coEvery { localRepo.updateSaveSetting(any()) } returns Unit

        val totalLoadModel = mockk<TotalLoadModel>()
        coEvery { totalLoadModel.getContents() } returns "기타 내용"
        coEvery { totalLoadModel.getLicenses() } returns emptyList()

        return Triple(SettingViewModel(localRepo, totalLoadModel), localRepo, totalLoadModel)
    }

    // ── 1. TimeSaved ──────────────────────────────────────────────────────────

    test("TimeSaved(Work, 7) 시 mainGroupOneTime=7로 저장, expandedTimeGroup=null") {
        runTest(testDispatcher) {
            val (vm, repo, _) = createViewModel()
            vm.onIntent(SettingIntent.TimeGroupTapped(TimeGroup.Work))
            vm.onIntent(SettingIntent.TimeSaved(TimeGroup.Work, 7))
            advanceUntilIdle()

            coVerify { repo.updateSaveSetting(match { it.mainGroupOneTime == 7 }) }
            vm.uiState.value.expandedTimeGroup shouldBe null
        }
    }

    test("TimeSaved(Leave, 19) 시 mainGroupTwoTime=19로 저장") {
        runTest(testDispatcher) {
            val (vm, repo, _) = createViewModel()
            vm.onIntent(SettingIntent.TimeSaved(TimeGroup.Leave, 19))
            advanceUntilIdle()

            coVerify { repo.updateSaveSetting(match { it.mainGroupTwoTime == 19 }) }
        }
    }

    // ── 2. ToggleChanged ──────────────────────────────────────────────────────

    test("ToggleChanged(AutoReload) 시 detailAutoReload 반전 저장") {
        runTest(testDispatcher) {
            val (vm, repo, _) = createViewModel(SaveSetting(detailAutoReload = true))
            vm.onIntent(SettingIntent.ToggleChanged(SettingToggleField.AutoReload))
            advanceUntilIdle()

            coVerify { repo.updateSaveSetting(match { !it.detailAutoReload }) }
        }
    }

    test("ToggleChanged(ScheduleAutoTime) 시 detailScheduleAutoTime 반전 저장") {
        runTest(testDispatcher) {
            val (vm, repo, _) = createViewModel(SaveSetting(detailScheduleAutoTime = false))
            vm.onIntent(SettingIntent.ToggleChanged(SettingToggleField.ScheduleAutoTime))
            advanceUntilIdle()

            coVerify { repo.updateSaveSetting(match { it.detailScheduleAutoTime }) }
        }
    }

    test("ToggleChanged(SearchOverlap) 시 searchOverlapAlert 반전 저장") {
        runTest(testDispatcher) {
            val (vm, repo, _) = createViewModel(SaveSetting(searchOverlapAlert = false))
            vm.onIntent(SettingIntent.ToggleChanged(SettingToggleField.SearchOverlap))
            advanceUntilIdle()

            coVerify { repo.updateSaveSetting(match { it.searchOverlapAlert }) }
        }
    }

    // ── 3. ContentsTapped ─────────────────────────────────────────────────────

    test("ContentsTapped 시 getContents() 결과가 modalContents에 반영됨") {
        runTest(testDispatcher) {
            val (vm, _, totalLoadModel) = createViewModel()
            coEvery { totalLoadModel.getContents() } returns "앱 문의: test@test.com"

            vm.onIntent(SettingIntent.ContentsTapped)
            advanceUntilIdle()

            vm.uiState.value.activeModal shouldBe SettingModalType.Contents
            vm.uiState.value.modalContents shouldBe "앱 문의: test@test.com"
            vm.uiState.value.isModalLoading shouldBe false
        }
    }

    test("ContentsTapped 시 getLicenses()를 호출하지 않음") {
        runTest(testDispatcher) {
            val (vm, _, totalLoadModel) = createViewModel()

            vm.onIntent(SettingIntent.ContentsTapped)
            advanceUntilIdle()

            coVerify(exactly = 0) { totalLoadModel.getLicenses() }
        }
    }

    // ── 4. TrainIconSelected ──────────────────────────────────────────────────

    test("TrainIconSelected 시 detailVcTrainIcon 저장 후 activeModal=null") {
        runTest(testDispatcher) {
            val (vm, repo, _) = createViewModel()
            vm.onIntent(SettingIntent.TrainIconTapped)
            vm.onIntent(SettingIntent.TrainIconSelected("🚇"))
            advanceUntilIdle()

            coVerify { repo.updateSaveSetting(match { it.detailVcTrainIcon == "🚇" }) }
            vm.uiState.value.activeModal shouldBe null
        }
    }

    // ── 5. CongestionLabelChanged ─────────────────────────────────────────────

    test("CongestionLabelChanged - 여러 글자 입력 시 마지막 글자만 유지") {
        runTest(testDispatcher) {
            val (vm, _, _) = createViewModel()
            vm.onIntent(SettingIntent.CongestionLabelChanged("테스트"))
            vm.uiState.value.saveSetting.mainCongestionLabel shouldBe "트"
        }
    }

    test("CongestionLabelChanged - 단일 이모지는 그대로 유지") {
        runTest(testDispatcher) {
            val (vm, _, _) = createViewModel()
            vm.onIntent(SettingIntent.CongestionLabelChanged("😊"))
            vm.uiState.value.saveSetting.mainCongestionLabel shouldBe "😊"
        }
    }

    // ── 6. CongestionLabelFocusLost ───────────────────────────────────────────

    test("CongestionLabelFocusLost - 빈 라벨이면 ☹️로 기본값 저장") {
        runTest(testDispatcher) {
            val (vm, repo, _) = createViewModel(SaveSetting(mainCongestionLabel = ""))
            vm.onIntent(SettingIntent.CongestionLabelFocusLost)
            advanceUntilIdle()

            coVerify { repo.updateSaveSetting(match { it.mainCongestionLabel == "☹️" }) }
        }
    }

    test("CongestionLabelFocusLost - 라벨이 있으면 그대로 저장") {
        runTest(testDispatcher) {
            val (vm, repo, _) = createViewModel(SaveSetting(mainCongestionLabel = "😊"))
            vm.onIntent(SettingIntent.CongestionLabelFocusLost)
            advanceUntilIdle()

            coVerify { repo.updateSaveSetting(match { it.mainCongestionLabel == "😊" }) }
        }
    }

    // ── 7. ModalDismissed ─────────────────────────────────────────────────────

    test("ModalDismissed 시 activeModal, modalContents, isModalLoading 초기화") {
        runTest(testDispatcher) {
            val (vm, _, _) = createViewModel()
            vm.onIntent(SettingIntent.ContentsTapped)
            advanceUntilIdle()

            vm.onIntent(SettingIntent.ModalDismissed)

            vm.uiState.value.activeModal shouldBe null
            vm.uiState.value.modalContents shouldBe ""
            vm.uiState.value.isModalLoading shouldBe false
        }
    }
})
