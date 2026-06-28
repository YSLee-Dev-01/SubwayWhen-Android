package com.yslee.subwaywhen.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import com.yslee.subwaywhen.data.repository.SearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: SearchRepository,
    private val analytics: FirebaseAnalytics,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _uiState = MutableStateFlow(SearchUiState(recommendStations = SearchRepository.DEFAULT_RECOMMEND))

    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(recommendStations = repository.recommendStations()) }
        }
        viewModelScope.launch {
            _uiState.update { it.copy(nowQueryRecommendList = repository.searchQueryRecommendList()) }
        }
        _searchQuery
            .debounce(700L)
            .distinctUntilChanged()
            .flatMapLatest { query ->
                flow<Unit> { performSearch(query) }
            }
            .launchIn(viewModelScope)
    }

    fun onIntent(intent: SearchIntent) {
        when (intent) {
            is SearchIntent.OnAppear -> Unit

            is SearchIntent.EnterSearchMode ->
                _uiState.update { it.copy(isSearchMode = true) }

            is SearchIntent.ExitSearchMode -> {
                _searchQuery.value = ""
                _uiState.update {
                    it.copy(
                        isSearchMode = false,
                        searchQuery = "",
                        searchResult = emptyList(),
                        isSearchLoading = false,
                        filteredQueryRecommendList = emptyList()
                    )
                }
            }

            is SearchIntent.QueryChanged -> {
                _searchQuery.value = intent.text
                _uiState.update {
                    it.copy(
                        searchQuery = intent.text,
                        isSearchLoading = intent.text.isNotEmpty(),
                        searchResult = emptyList(),
                        filteredQueryRecommendList = emptyList()
                    )
                }
            }

            is SearchIntent.RecommendStationTapped -> {
                _uiState.update {
                    it.copy(isSearchMode = true, searchQuery = intent.name, isSearchLoading = true)
                }
                _searchQuery.value = intent.name
            }

            is SearchIntent.QueryRecommendStationTapped -> {
                _uiState.update {
                    it.copy(
                        isSearchMode = true,
                        searchQuery = intent.item.stationName,
                        isSearchLoading = true,
                    )
                }
                _searchQuery.value = intent.item.stationName
            }

            is SearchIntent.ResultStationTapped -> {
                _uiState.update { it.copy(selectedStation = intent.item) }
            }

            is SearchIntent.ModalDismissed -> {
                _uiState.update { it.copy(selectedStation = null) }
            }

            is SearchIntent.SaveCompleted -> {
                _uiState.update { it.copy(selectedStation = null, isSaveCompletedModalVisible = true) }
            }

            is SearchIntent.SaveCompletedDismissed -> {
                _uiState.update { it.copy(isSaveCompletedModalVisible = false) }
            }

            is SearchIntent.VicinityStationSelected -> {
                _uiState.update {
                    it.copy(
                        isSearchMode = true,
                        isSearchLoading = true,
                        vicinityAutoOpen = true,
                        searchQuery = intent.stationName,
                    )
                }
                _searchQuery.value = intent.stationName
            }

            is SearchIntent.VicinityListStationSelected -> {
                _uiState.update {
                    it.copy(
                        isSearchMode = true,
                        vicinityAutoOpen = false,
                        isSearchLoading = true,
                        searchQuery = intent.stationName,
                    )
                }
                _searchQuery.value = intent.stationName
            }
        }
    }

    private suspend fun performSearch(query: String) {
        if (query.isEmpty()) {
            _uiState.update {
                it.copy(searchResult = emptyList(), isSearchLoading = false, filteredQueryRecommendList = emptyList())
            }
            return
        }

        analytics.logEvent("SearchVC_Search") {
            param("Search_Station", query)
        }
        val filtered = _uiState.value.nowQueryRecommendList.filter { it.queryName == query }
        val result = repository.searchStations(query)
        val autoOpen = _uiState.value.vicinityAutoOpen
        _uiState.update {
            it.copy(
                searchResult = result,
                isSearchLoading = false,
                filteredQueryRecommendList = filtered,
                selectedStation = if (autoOpen) result.firstOrNull() else it.selectedStation,
                vicinityAutoOpen = false,
            )
        }
    }
}
