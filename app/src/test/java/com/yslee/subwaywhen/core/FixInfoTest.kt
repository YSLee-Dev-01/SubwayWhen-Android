package com.yslee.subwaywhen.core

import com.yslee.subwaywhen.data.local.SettingLocalDataSource
import com.yslee.subwaywhen.data.local.StationLocalDataSource
import com.yslee.subwaywhen.data.model.SaveSetting
import com.yslee.subwaywhen.data.model.SaveStation
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope

class FixInfoTest : FunSpec({

    val settingDataSource = mockk<SettingLocalDataSource>()
    val stationDataSource = mockk<StationLocalDataSource>()
    val testScope = TestScope()

    lateinit var fixInfo: FixInfo

    beforeEach {
        fixInfo = FixInfo(settingDataSource, stationDataSource, testScope)
    }

    test("initialize() 호출 전 — isInitialized == false") {
        fixInfo.isInitialized.value shouldBe false
    }

    test("initialize() 호출 후 — isInitialized == true") {
        coEvery { settingDataSource.getSaveSetting() } returns flowOf(SaveSetting())
        coEvery { stationDataSource.getSaveStations() } returns flowOf(emptyList())

        fixInfo.initialize()

        fixInfo.isInitialized.value shouldBe true
    }

    test("initialize() — saveSetting StateFlow에 DataSource 반환값이 적재된다") {
        val expected = SaveSetting(tutorialSuccess = true, mainGroupOneTime = 8)
        coEvery { settingDataSource.getSaveSetting() } returns flowOf(expected)
        coEvery { stationDataSource.getSaveStations() } returns flowOf(emptyList())

        fixInfo.initialize()

        fixInfo.saveSetting.value shouldBe expected
    }

    test("initialize() — saveStations StateFlow에 DataSource 반환값이 적재된다") {
        val expected = listOf(SaveStation(id = "s1", stationName = "강남"))
        coEvery { settingDataSource.getSaveSetting() } returns flowOf(SaveSetting())
        coEvery { stationDataSource.getSaveStations() } returns flowOf(expected)

        fixInfo.initialize()

        fixInfo.saveStations.value shouldBe expected
    }

    test("updateSaveSetting() — StateFlow 갱신 및 settingDataSource.updateSaveSetting() 호출") {
        val newSetting = SaveSetting(mainGroupOneTime = 9, tutorialSuccess = true)
        coEvery { settingDataSource.updateSaveSetting(newSetting) } returns Unit

        fixInfo.updateSaveSetting(newSetting)

        fixInfo.saveSetting.value shouldBe newSetting
        coVerify(exactly = 1) { settingDataSource.updateSaveSetting(newSetting) }
    }

    test("updateSaveStations() — StateFlow 갱신 및 stationDataSource.updateSaveStations() 호출") {
        val list = listOf(SaveStation(id = "s2", stationName = "홍대입구"))
        coEvery { stationDataSource.updateSaveStations(list) } returns Unit

        fixInfo.updateSaveStations(list)

        fixInfo.saveStations.value shouldBe list
        coVerify(exactly = 1) { stationDataSource.updateSaveStations(list) }
    }
})
