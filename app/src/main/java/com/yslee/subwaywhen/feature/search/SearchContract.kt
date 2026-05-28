package com.yslee.subwaywhen.feature.search

import com.yslee.subwaywhen.data.remote.dto.stationSearch.SearchQueryRecommendData
import com.yslee.subwaywhen.data.remote.dto.stationSearch.SearchStationInfo

data class SearchUiState(
    val isSearchMode: Boolean = false,
    val searchQuery: String = "",
    val isSearchLoading: Boolean = false,
    val searchResult: List<SearchStationInfo> = emptyList(),
    val recommendStations: List<String> = emptyList(),
    val nowQueryRecommendList: List<SearchQueryRecommendData> = emptyList(),
    val filteredQueryRecommendList: List<SearchQueryRecommendData> = emptyList(),
    val selectedStation: SearchStationInfo? = null,
    val isSaveCompletedModalVisible: Boolean = false,
    val vicinityAutoOpen: Boolean = false,
)

sealed interface SearchIntent {
    data object OnAppear : SearchIntent
    data object EnterSearchMode : SearchIntent
    data object ExitSearchMode : SearchIntent
    data class QueryChanged(val text: String) : SearchIntent
    data class RecommendStationTapped(val name: String) : SearchIntent
    data class QueryRecommendStationTapped(val item: SearchQueryRecommendData) : SearchIntent
    data class ResultStationTapped(val item: SearchStationInfo) : SearchIntent
    data object ModalDismissed : SearchIntent
    data object SaveCompleted : SearchIntent
    data object SaveCompletedDismissed : SearchIntent
    data class VicinityStationSelected(val stationName: String) : SearchIntent
}

sealed interface SearchEffect
