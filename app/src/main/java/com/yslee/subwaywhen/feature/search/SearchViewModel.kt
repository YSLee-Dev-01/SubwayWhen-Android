package com.yslee.subwaywhen.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yslee.subwaywhen.data.repository.SearchRepository
import com.yslee.subwaywhen.data.repository.SearchRepositoryImpl
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
    private val repository: SearchRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _internal = MutableStateFlow(SearchUiState(recommendStations = SearchRepositoryImpl.DEFAULT_RECOMMEND))

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
                        isSearchLoading = true
                    )
                }
                _searchQuery.value = intent.item.stationName
            }

            is SearchIntent.ResultStationTapped -> {
                // TODO: 다음 spec에서 상세 화면으로 이동 처리
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

        val filtered = _internal.value.nowQueryRecommendList.filter { it.queryName == query }
        val result = repository.searchStations(query)
        _internal.update {
            it.copy(searchResult = result, isSearchLoading = false, filteredQueryRecommendList = filtered)
        }
    }
}
