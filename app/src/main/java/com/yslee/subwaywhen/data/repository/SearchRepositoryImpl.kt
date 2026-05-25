package com.yslee.subwaywhen.data.repository

import com.yslee.subwaywhen.data.network.NetworkResult
import com.yslee.subwaywhen.data.remote.dto.stationSearch.SearchQueryRecommendData
import com.yslee.subwaywhen.data.remote.dto.stationSearch.SearchStationInfo
import com.yslee.subwaywhen.data.remote.firebase.FirebaseDataSource
import com.yslee.subwaywhen.data.remote.loadmodel.LoadModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepositoryImpl @Inject constructor(
    private val loadModel: LoadModel,
    private val firebaseDataSource: FirebaseDataSource
) : SearchRepository {

    override suspend fun searchStations(query: String): List<SearchStationInfo> {
        return when (val result = loadModel.stationSearch(query)) {
            is NetworkResult.Success -> result.data.SearchInfoBySubwayNameService.row
            is NetworkResult.Failure -> emptyList()
        }
    }

    override suspend fun recommendStations(): List<String> {
        return firebaseDataSource.getSearchDefaultList() ?: DEFAULT_RECOMMEND
    }

    override suspend fun searchQueryRecommendList(): List<SearchQueryRecommendData> {
        return firebaseDataSource.getSearchQueryRecommendList() ?: emptyList()
    }

    companion object {
        val DEFAULT_RECOMMEND = listOf(
            "강남", "교대", "선릉", "삼성", "을지로3가",
            "종각", "홍대입구", "잠실", "명동", "여의도",
            "가산디지털단지", "판교"
        )
    }
}
