package com.yslee.subwaywhen.data.remote.congestion

import android.content.Context
import android.content.res.AssetManager
import com.yslee.subwaywhen.data.local.SettingLocalDataSource
import com.yslee.subwaywhen.data.model.HolidayData
import io.kotest.core.spec.style.FunSpec
import kotlinx.coroutines.runBlocking
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import java.io.ByteArrayInputStream

private fun buildJson(stationBlock: String) = """
    {"stations": {$stationBlock}}
""".trimIndent()

private fun weekdayHours(vararg hours: Int): String =
    hours.joinToString(",") { h -> """"$h": {"percent": ${h * 5 + 10}, "level": ${(h / 3) + 1}}""" }

class CongestionManagerTest : FunSpec({

    val assetManager = mockk<AssetManager>()
    val context = mockk<Context>()
    val settingLocalDataSource = mockk<SettingLocalDataSource>()

    every { context.assets } returns assetManager
    coEvery { settingLocalDataSource.getHolidayData() } returns HolidayData(version = 0, list = emptyList())

    fun makeManager(json: String): CongestionManager {
        every { assetManager.open("congestion_data.json") } returns ByteArrayInputStream(json.toByteArray())
        return CongestionManager(context, settingLocalDataSource)
    }

    test("존재하는 역, 평일: 0~23시 24개 반환 및 hour 오름차순 정렬") {
        val allHours = (0..23).toList()
        val hoursJson = weekdayHours(*allHours.toIntArray())
        val json = buildJson(""""강남": {"hourly_congestion": {"weekday": {$hoursJson}, "saturday": {}, "sunday": {}}}""")
        val manager = makeManager(json)

        val result = runBlocking { manager.getCongestions("강남") }

        result shouldHaveSize 24
        result.map { it.hour } shouldBe (0..23).toList()
    }

    test("존재하지 않는 역: emptyList 반환") {
        val json = buildJson(""""강남": {"hourly_congestion": {"weekday": {"5": {"percent": 50, "level": 3}}, "saturday": {}, "sunday": {}}}""")
        val manager = makeManager(json)

        val result = kotlinx.coroutines.runBlocking { manager.getCongestions("없는역") }

        result.shouldBeEmpty()
    }

    test("1~4시 결측 시 percent=0, level=0 채움") {
        val hoursJson = weekdayHours(0, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23)
        val json = buildJson(""""강남": {"hourly_congestion": {"weekday": {$hoursJson}, "saturday": {}, "sunday": {}}}""")
        val manager = makeManager(json)

        val result = runBlocking { manager.getCongestions("강남") }

        result shouldHaveSize 24
        (1..4).forEach { h ->
            val entry = result.first { it.hour == h }
            entry.percent shouldBe 0
            entry.level shouldBe 0
        }
    }

    test("빈 데이터(파싱 실패): emptyList 반환") {
        every { assetManager.open("congestion_data.json") } throws RuntimeException("IO error")
        val manager = CongestionManager(context, settingLocalDataSource)

        val result = runBlocking { manager.getCongestions("강남") }

        result.shouldBeEmpty()
    }
})
