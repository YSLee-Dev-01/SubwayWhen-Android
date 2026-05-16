package com.yslee.subwaywhen.data.local

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import app.cash.turbine.test
import com.yslee.subwaywhen.data.model.SaveSetting
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class SettingLocalDataSourceTest : FunSpec({

    val testDispatcher = UnconfinedTestDispatcher()
    val testScope = TestScope(testDispatcher)
    val tmpFolder = TemporaryFolder()

    lateinit var dataSource: SettingLocalDataSource

    beforeEach {
        tmpFolder.create()
        val dataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { tmpFolder.newFile("test_setting.preferences_pb") }
        )
        dataSource = SettingLocalDataSource(dataStore)
    }

    afterEach {
        tmpFolder.delete()
    }

    test("getSaveSetting — 저장된 값 없을 때 기본값 반환") {
        dataSource.getSaveSetting().test {
            val setting = awaitItem()
            setting shouldBe SaveSetting()
            cancelAndIgnoreRemainingEvents()
        }
    }

    test("updateSaveSetting → getSaveSetting 라운드트립 — 저장한 SaveSetting이 그대로 읽힘") {
        val expected = SaveSetting(
            mainCongestionLabel = "😊",
            mainGroupOneTime = 8,
            mainGroupTwoTime = 18,
            detailAutoReload = false,
            detailScheduleAutoTime = false,
            searchOverlapAlert = false,
            tutorialSuccess = true,
            detailVcTrainIcon = "🚄",
            isWeekendNotificationEnabled = false,
            mainCongestionBaseStation = "홍대입구",
        )

        dataSource.updateSaveSetting(expected)

        dataSource.getSaveSetting().test {
            awaitItem() shouldBe expected
            cancelAndIgnoreRemainingEvents()
        }
    }

    test("updateTutorialSeen(true) → getSaveSetting().tutorialSuccess == true") {
        dataSource.updateTutorialSeen(true)

        dataSource.getSaveSetting().test {
            awaitItem().tutorialSuccess shouldBe true
            cancelAndIgnoreRemainingEvents()
        }
    }
})
