package com.yslee.subwaywhen.data.repository

import com.yslee.subwaywhen.data.network.NetworkResult
import com.yslee.subwaywhen.data.remote.dto.stationSearch.SearchInfoBySubwayNameService
import com.yslee.subwaywhen.data.remote.dto.stationSearch.SearchQueryRecommendData
import com.yslee.subwaywhen.data.remote.dto.stationSearch.SearchStation
import com.yslee.subwaywhen.data.remote.dto.stationSearch.SearchStationInfo
import com.yslee.subwaywhen.data.remote.firebase.FirebaseDataSource
import com.yslee.subwaywhen.data.remote.loadmodel.LoadModel
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk

class SearchRepositoryImplTest : FunSpec({

    lateinit var loadModel: LoadModel
    lateinit var firebaseDataSource: FirebaseDataSource
    lateinit var repository: SearchRepositoryImpl

    beforeEach {
        loadModel = mockk()
        firebaseDataSource = mockk()
        repository = SearchRepositoryImpl(loadModel, firebaseDataSource)
    }

    // ── searchStations ─────────────────────────────────────────────────────

    test("searchStations — 검색 성공 시 결과 리스트 반환") {
        val items = listOf(
            SearchStationInfo(stationName = "강남", line = "02호선", stationCode = "222"),
            SearchStationInfo(stationName = "강남구청", line = "07호선", stationCode = "738")
        )
        val fakeStation = SearchStation(SearchInfoBySubwayNameService(items))
        coEvery { loadModel.stationSearch("강남") } returns NetworkResult.Success(fakeStation)

        val result = repository.searchStations("강남")

        result shouldBe items
    }

    test("searchStations — 검색 실패 시 emptyList() 반환") {
        coEvery { loadModel.stationSearch("강남") } returns NetworkResult.Failure(mockk())

        val result = repository.searchStations("강남")

        result shouldBe emptyList()
    }

    // ── recommendStations ─────────────────────────────────────────────────

    test("recommendStations — getSearchDefaultList() 성공 시 해당 리스트 반환") {
        val firebaseList = listOf("홍대입구", "신촌")
        coEvery { firebaseDataSource.getSearchDefaultList() } returns firebaseList

        val result = repository.recommendStations()

        result shouldBe firebaseList
    }

    test("recommendStations — getSearchDefaultList() null 반환 시 DEFAULT_RECOMMEND 폴백 반환") {
        coEvery { firebaseDataSource.getSearchDefaultList() } returns null

        val result = repository.recommendStations()

        result shouldBe SearchRepositoryImpl.DEFAULT_RECOMMEND
    }

    // ── searchQueryRecommendList ───────────────────────────────────────────

    test("searchQueryRecommendList — getSearchQueryRecommendList() null 반환 시 emptyList() 반환") {
        coEvery { firebaseDataSource.getSearchQueryRecommendList() } returns null

        val result = repository.searchQueryRecommendList()

        result shouldBe emptyList<SearchQueryRecommendData>()
    }
})
