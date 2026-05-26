package com.yslee.subwaywhen.data.repository

import com.yslee.subwaywhen.data.remote.dto.stationSearch.SearchQueryRecommendData
import com.yslee.subwaywhen.data.remote.dto.stationSearch.SearchStationInfo

interface SearchRepository {
    suspend fun searchStations(query: String): List<SearchStationInfo>
    suspend fun recommendStations(): List<String>
    suspend fun searchQueryRecommendList(): List<SearchQueryRecommendData>

    companion object {
        val DEFAULT_RECOMMEND = listOf(
            "강남", "교대", "선릉", "삼성", "을지로3가",
            "종각", "홍대입구", "잠실", "명동", "여의도",
            "가산디지털단지", "판교"
        )
    }
}
