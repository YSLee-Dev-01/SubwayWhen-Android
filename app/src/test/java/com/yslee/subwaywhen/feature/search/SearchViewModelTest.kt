package com.yslee.subwaywhen.feature.search

import app.cash.turbine.test
import com.yslee.subwaywhen.data.remote.dto.stationSearch.SearchQueryRecommendData
import com.yslee.subwaywhen.data.remote.dto.stationSearch.SearchStationInfo
import com.google.firebase.analytics.FirebaseAnalytics
import com.yslee.subwaywhen.data.repository.SearchRepository
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest : FunSpec({

    val testDispatcher = UnconfinedTestDispatcher()

    beforeEach {
        Dispatchers.setMain(testDispatcher)
    }

    afterEach {
        Dispatchers.resetMain()
    }

    fun createViewModel(
        fakeStations: List<SearchStationInfo> = emptyList(),
        fakeRecommendStations: List<String> = SearchRepository.DEFAULT_RECOMMEND,
        fakeQueryRecommendList: List<SearchQueryRecommendData> = emptyList()
    ): SearchViewModel {
        val fakeRepository = object : SearchRepository {
            override suspend fun searchStations(query: String) = fakeStations
            override suspend fun recommendStations() = fakeRecommendStations
            override suspend fun searchQueryRecommendList() = fakeQueryRecommendList
        }
        return SearchViewModel(fakeRepository, mockk(relaxed = true))
    }

    // ── 1. 초기 상태 ────────────────────────────────────────────────────────

    test("초기 상태에서 recommendStations에 DEFAULT_RECOMMEND 12개 노출") {
        runTest(testDispatcher) {
            val viewModel = createViewModel()

            viewModel.uiState.value.recommendStations shouldBe SearchRepository.DEFAULT_RECOMMEND
            viewModel.uiState.value.recommendStations.size shouldBe 12
        }
    }

    // ── 2. EnterSearchMode ───────────────────────────────────────────────────

    test("EnterSearchMode 후 isSearchMode == true") {
        runTest(testDispatcher) {
            val viewModel = createViewModel()

            viewModel.uiState.test {
                awaitItem() // 초기값 소비
                viewModel.onIntent(SearchIntent.EnterSearchMode)
                awaitItem().isSearchMode shouldBe true
            }
        }
    }

    // ── 3. QueryChanged + 700ms debounce ─────────────────────────────────────

    test("QueryChanged('강남') 후 700ms advanceTimeBy → searchResult에 fake 결과 + filteredQueryRecommendList 필터링 결과") {
        val fakeResult = listOf(
            SearchStationInfo(stationName = "강남", line = "02호선", stationCode = "222")
        )
        val fakeQueryRecommendList = listOf(
            SearchQueryRecommendData(queryName = "강남", stationName = "강남", line = "02호선"),
            SearchQueryRecommendData(queryName = "홍대입구", stationName = "홍대입구", line = "02호선")
        )

        runTest(testDispatcher) {
            val viewModel = createViewModel(
                fakeStations = fakeResult,
                fakeQueryRecommendList = fakeQueryRecommendList
            )

            viewModel.onIntent(SearchIntent.QueryChanged("강남"))
            advanceTimeBy(700L)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            state.searchResult shouldBe fakeResult
            state.filteredQueryRecommendList shouldBe listOf(fakeQueryRecommendList[0])
        }
    }

    // ── 4. QueryChanged("") ──────────────────────────────────────────────────

    test("QueryChanged('') → isSearchLoading = false, searchResult 비워짐, filteredQueryRecommendList 비워짐") {
        val fakeResult = listOf(
            SearchStationInfo(stationName = "강남", line = "02호선", stationCode = "222")
        )

        runTest(testDispatcher) {
            val viewModel = createViewModel(fakeStations = fakeResult)

            // 먼저 결과를 채운 뒤
            viewModel.onIntent(SearchIntent.QueryChanged("강남"))
            advanceTimeBy(700L)
            advanceUntilIdle()

            // 빈 쿼리로 초기화
            viewModel.onIntent(SearchIntent.QueryChanged(""))
            advanceTimeBy(700L)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            state.isSearchLoading shouldBe false
            state.searchResult shouldBe emptyList()
            state.filteredQueryRecommendList shouldBe emptyList()
        }
    }

    // ── 5. RecommendStationTapped ────────────────────────────────────────────

    test("RecommendStationTapped('교대') → isSearchMode = true, searchQuery = '교대'") {
        runTest(testDispatcher) {
            val viewModel = createViewModel()

            viewModel.onIntent(SearchIntent.RecommendStationTapped("교대"))

            val state = viewModel.uiState.value
            state.isSearchMode shouldBe true
            state.searchQuery shouldBe "교대"
        }
    }

    // ── 6. QueryRecommendStationTapped ───────────────────────────────────────

    test("QueryRecommendStationTapped(item) → item.stationName으로 검색 모드 진입") {
        val item = SearchQueryRecommendData(
            queryName = "강남역",
            stationName = "강남",
            line = "02호선"
        )

        runTest(testDispatcher) {
            val viewModel = createViewModel()

            viewModel.onIntent(SearchIntent.QueryRecommendStationTapped(item))

            val state = viewModel.uiState.value
            state.isSearchMode shouldBe true
            state.searchQuery shouldBe "강남"
        }
    }
})
