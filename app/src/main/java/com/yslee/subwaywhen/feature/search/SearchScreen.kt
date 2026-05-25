package com.yslee.subwaywhen.feature.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yslee.subwaywhen.R
import com.yslee.subwaywhen.data.remote.dto.stationSearch.SearchStationInfo
import com.yslee.subwaywhen.feature.search.component.SearchQueryRecommendSection
import com.yslee.subwaywhen.feature.search.component.SearchResultSection
import com.yslee.subwaywhen.feature.search.component.SearchTextField
import com.yslee.subwaywhen.feature.search.component.SearchWordRecommendSection
import com.yslee.subwaywhen.ui.common.CommonTopBarScreen
import com.yslee.subwaywhen.ui.theme.Dimens
import com.yslee.subwaywhen.ui.theme.SubwayWhenTheme

@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SearchScreenContent(
        uiState = uiState,
        onIntent = viewModel::onIntent,
    )
}

@Composable
private fun SearchScreenContent(
    uiState: SearchUiState,
    onIntent: (SearchIntent) -> Unit,
) {
    CommonTopBarScreen(title = stringResource(R.string.tab_search)) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimens.searchSectionGap),
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = Dimens.searchSectionGap),
        ) {
            SearchTextField(
                isSearchMode = uiState.isSearchMode,
                query = uiState.searchQuery,
                onQueryChange = { onIntent(SearchIntent.QueryChanged(it)) },
                onEnterSearchMode = { onIntent(SearchIntent.EnterSearchMode) },
                onExitSearchMode = { onIntent(SearchIntent.ExitSearchMode) },
            )

            if (uiState.isSearchMode) {
                SearchResultSection(
                    query = uiState.searchQuery,
                    isLoading = uiState.isSearchLoading,
                    result = uiState.searchResult,
                    onItemClick = { onIntent(SearchIntent.ResultStationTapped(it)) },
                )
            }

            if (uiState.filteredQueryRecommendList.isNotEmpty()) {
                SearchQueryRecommendSection(
                    items = uiState.filteredQueryRecommendList,
                    onItemClick = { onIntent(SearchIntent.QueryRecommendStationTapped(it)) },
                )
            }

            // TODO: SearchVicinitySection — 다음 spec

            if (uiState.searchQuery.isEmpty()) {
                SearchWordRecommendSection(
                    stations = uiState.recommendStations,
                    onItemClick = { onIntent(SearchIntent.RecommendStationTapped(it)) },
                )
            }
        }
    }
}

@Preview(name = "SearchScreen - 비검색 모드 (추천 목록)", showBackground = true)
@Composable
private fun SearchScreenNonSearchModePreview() {
    SubwayWhenTheme(darkTheme = false) {
        SearchScreenContent(
            uiState = SearchUiState(
                isSearchMode = false,
                recommendStations = listOf("강남", "교대", "선릉", "삼성", "을지로3가", "종각", "홍대입구", "잠실", "명동", "여의도", "가산디지털단지", "판교"),
            ),
            onIntent = {},
        )
    }
}

@Preview(name = "SearchScreen - 검색 모드 + 결과 있음", showBackground = true)
@Composable
private fun SearchScreenSearchWithResultPreview() {
    SubwayWhenTheme(darkTheme = false) {
        SearchScreenContent(
            uiState = SearchUiState(
                isSearchMode = true,
                searchQuery = "강남",
                isSearchLoading = false,
                searchResult = listOf(
                    SearchStationInfo(stationName = "강남", line = "02호선", stationCode = "222"),
                    SearchStationInfo(stationName = "강남구청", line = "07호선", stationCode = "730"),
                ),
                recommendStations = listOf("강남", "교대", "선릉", "삼성"),
            ),
            onIntent = {},
        )
    }
}

@Preview(name = "SearchScreen - 검색 모드 + 결과 없음", showBackground = true)
@Composable
private fun SearchScreenSearchNoResultPreview() {
    SubwayWhenTheme(darkTheme = false) {
        SearchScreenContent(
            uiState = SearchUiState(
                isSearchMode = true,
                searchQuery = "ㅁㅁㅁ",
                isSearchLoading = false,
                searchResult = emptyList(),
                recommendStations = listOf("강남", "교대", "선릉", "삼성"),
            ),
            onIntent = {},
        )
    }
}

@Preview(name = "SearchScreen - 검색 모드 + 로딩 중", showBackground = true)
@Composable
private fun SearchScreenSearchLoadingPreview() {
    SubwayWhenTheme(darkTheme = false) {
        SearchScreenContent(
            uiState = SearchUiState(
                isSearchMode = true,
                searchQuery = "강남",
                isSearchLoading = true,
                searchResult = emptyList(),
                recommendStations = listOf("강남", "교대", "선릉", "삼성"),
            ),
            onIntent = {},
        )
    }
}
