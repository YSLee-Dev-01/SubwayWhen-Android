package com.yslee.subwaywhen.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import com.yslee.subwaywhen.data.repository.SearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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
    private val _internal = MutableStateFlow(SearchUiState(recommendStations = SearchRepository.DEFAULT_RECOMMEND))

    val uiState: StateFlow<SearchUiState> = _internal.asStateFlow()
    private val _effect = MutableSharedFlow<SearchEffect>()
    val effect: SharedFlow<SearchEffect> = _effect.asSharedFlow()

    init {
        viewModelScope.launch {
            _internal.update { it.copy(recommendStations = repository.recommendStations()) }
        }
        viewModelScope.launch {
            _internal.update { it.copy(nowQueryRecommendList = repository.searchQueryRecommendList()) }
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
                _internal.update { it.copy(isSearchMode = true) }

            is SearchIntent.ExitSearchMode -> {
                _searchQuery.value = ""
                _internal.update {
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
                _internal.update {
                    it.copy(
                        searchQuery = intent.text,
                        isSearchLoading = intent.text.isNotEmpty(),
                        searchResult = emptyList(),
                        filteredQueryRecommendList = emptyList()
                    )
                }
            }

            is SearchIntent.RecommendStationTapped -> {
                _internal.update {
                    it.copy(isSearchMode = true, searchQuery = intent.name, isSearchLoading = true)
                }
                _searchQuery.value = intent.name
            }

            is SearchIntent.QueryRecommendStationTapped -> {
                _internal.update {
                    it.copy(
                        isSearchMode = true,
                        searchQuery = intent.item.stationName,
                        isSearchLoading = true,
                    )
                }
                _searchQuery.value = intent.item.stationName
            }

            is SearchIntent.ResultStationTapped -> {
                _internal.update { it.copy(selectedStation = intent.item) }
            }

            is SearchIntent.ModalDismissed -> {
                _internal.update { it.copy(selectedStation = null) }
            }

            is SearchIntent.SaveCompleted -> {
                _internal.update { it.copy(selectedStation = null, isSaveCompletedModalVisible = true) }
            }

            is SearchIntent.SaveCompletedDismissed -> {
                _internal.update { it.copy(isSaveCompletedModalVisible = false) }
            }

            is SearchIntent.VicinityStationSelected -> {
                _internal.update {
                    it.copy(
                        isSearchMode = true,
                        isSearchLoading = true,
                        vicinityAutoOpen = true,
                    )
                }
                _searchQuery.value = intent.stationName
            }
        }
    }

    private suspend fun performSearch(query: String) {
        if (query.isEmpty()) {
            _internal.update {
                it.copy(searchResult = emptyList(), isSearchLoading = false, filteredQueryRecommendList = emptyList())
            }
            return
        }

        analytics.logEvent("SerachVC_Search") {
            param("Search_Station", query)
        }
        val filtered = _internal.value.nowQueryRecommendList.filter { it.queryName == query }
        val result = repository.searchStations(query)
        val autoOpen = _internal.value.vicinityAutoOpen
        _internal.update {
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
