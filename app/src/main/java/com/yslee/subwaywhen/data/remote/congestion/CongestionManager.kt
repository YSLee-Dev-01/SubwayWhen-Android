package com.yslee.subwaywhen.data.remote.congestion

import android.content.Context
import com.yslee.subwaywhen.data.local.SettingLocalDataSource
import com.yslee.subwaywhen.data.remote.dto.congestion.CongestionDataSet
import com.yslee.subwaywhen.data.remote.dto.congestion.CongestionLevel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

enum class DayType { WEEKDAY, SATURDAY, HOLIDAY }

@Singleton
class CongestionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingLocalDataSource: SettingLocalDataSource,
) {
    private val congestionDataSet: CongestionDataSet by lazy {
        val json = context.assets.open("congestion_data.json").bufferedReader().readText()
        Json.decodeFromString<CongestionDataSet>(json)
    }

    fun getAvailableStations(): List<String> =
        congestionDataSet.stations.keys.sorted()

    suspend fun getLevel(station: String, hour: Int): Int? =
        getDayData(station)?.get("$hour")?.level

    private suspend fun getDayData(station: String): Map<String, CongestionLevel>? {
        val stationData = congestionDataSet.stations[station]?.hourlyCongestion ?: return null
        val dayData = when (dayType()) {
            DayType.HOLIDAY -> stationData.sunday
            DayType.SATURDAY -> stationData.saturday
            DayType.WEEKDAY -> stationData.weekday
        }.toMutableMap()

        if (dayData.isEmpty()) return dayData

        for (hour in 1..4) {
            if (dayData["$hour"] == null) {
                dayData["$hour"] = CongestionLevel(percent = 0, level = 0)
            }
        }
        return dayData
    }

    private suspend fun dayType(): DayType {
        val weekday = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        if (weekday == Calendar.SUNDAY) return DayType.HOLIDAY
        if (weekday == Calendar.SATURDAY) return DayType.SATURDAY
        val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val holidayList = settingLocalDataSource.getHolidayData().list
        return if (holidayList.contains(today)) DayType.HOLIDAY else DayType.WEEKDAY
    }
}
