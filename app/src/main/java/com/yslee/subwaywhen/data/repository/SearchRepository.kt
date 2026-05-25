package com.yslee.subwaywhen.data.repository

import com.yslee.subwaywhen.data.remote.dto.stationSearch.SearchQueryRecommendData
import com.yslee.subwaywhen.data.remote.dto.stationSearch.SearchStationInfo

interface SearchRepository {
    suspend fun searchStations(query: String): List<SearchStationInfo>
    suspend fun recommendStations(): List<String>
    suspend fun searchQueryRecommendList(): List<SearchQueryRecommendData>
}
