package com.yslee.subwaywhen.data.repository

import com.yslee.subwaywhen.data.remote.dto.stationSearch.SearchQueryRecommendData
import com.yslee.subwaywhen.data.remote.dto.stationSearch.SearchStationInfo
import com.yslee.subwaywhen.data.remote.firebase.FirebaseDataSource
import com.yslee.subwaywhen.data.remote.totalload.TotalLoadModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepositoryImpl @Inject constructor(
    private val totalLoadModel: TotalLoadModel,
    private val firebaseDataSource: FirebaseDataSource
) : SearchRepository {

    override suspend fun searchStations(query: String): List<SearchStationInfo> =
        totalLoadModel.stationSearch(query)

    override suspend fun recommendStations(): List<String> =
        firebaseDataSource.getSearchDefaultList() ?: SearchRepository.DEFAULT_RECOMMEND

    override suspend fun searchQueryRecommendList(): List<SearchQueryRecommendData> =
        firebaseDataSource.getSearchQueryRecommendList() ?: emptyList()
}
