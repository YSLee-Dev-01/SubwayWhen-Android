package com.yslee.subwaywhen.data.local

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import app.cash.turbine.test
import com.yslee.subwaywhen.data.model.SaveStation
import com.yslee.subwaywhen.data.model.SaveStationGroup
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class StationLocalDataSourceTest : FunSpec({

    val testDispatcher = UnconfinedTestDispatcher()
    val testScope = TestScope(testDispatcher)
    val tmpFolder = TemporaryFolder()

    lateinit var dataSource: StationLocalDataSource

    beforeEach {
        tmpFolder.create()
        val dataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { tmpFolder.newFile("test_station.preferences_pb") }
        )
        dataSource = StationLocalDataSource(dataStore)
    }

    afterEach {
        tmpFolder.delete()
    }

    test("getSaveStations — 저장된 값 없을 때 빈 리스트 반환") {
        dataSource.getSaveStations().test {
            awaitItem() shouldBe emptyList()
            cancelAndIgnoreRemainingEvents()
        }
    }

    test("updateSaveStations → getSaveStations JSON 라운드트립 — 저장한 리스트가 동일하게 읽힘 (모든 9개 필드 포함)") {
        val expected = listOf(
            SaveStation(
                id = "station-001",
                stationName = "홍대입구",
                stationCode = "1002000240",
                updnLine = "상행",
                line = "2호선",
                lineCode = "1002",
                group = SaveStationGroup.ONE,
                exceptionLastStation = "성수",
                korailCode = "",
            ),
            SaveStation(
                id = "station-002",
                stationName = "강남",
                stationCode = "1002000222",
                updnLine = "하행",
                line = "2호선",
                lineCode = "1002",
                group = SaveStationGroup.TWO,
                exceptionLastStation = "",
                korailCode = "K123",
            ),
        )

        dataSource.updateSaveStations(expected)

        dataSource.getSaveStations().test {
            awaitItem() shouldBe expected
            cancelAndIgnoreRemainingEvents()
        }
    }

    test("깨진 JSON이 DataStore에 있을 때 빈 리스트로 fallback") {
        val dataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { tmpFolder.newFile("test_station_broken.preferences_pb") }
        )
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.SAVE_STATIONS_JSON] = "{ broken json !!!"
        }
        val dataSourceWithBrokenJson = StationLocalDataSource(dataStore)

        dataSourceWithBrokenJson.getSaveStations().test {
            awaitItem() shouldBe emptyList()
            cancelAndIgnoreRemainingEvents()
        }
    }
})
