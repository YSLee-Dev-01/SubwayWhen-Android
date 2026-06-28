package com.yslee.subwaywhen.feature.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.yslee.subwaywhen.R
import com.yslee.subwaywhen.data.remote.dto.stationSearch.SearchStationInfo
import com.yslee.subwaywhen.feature.search.component.SearchQueryRecommendSection
import com.yslee.subwaywhen.feature.search.component.SearchResultSection
import com.yslee.subwaywhen.feature.search.component.SearchTextField
import com.yslee.subwaywhen.feature.search.component.SearchWordRecommendSection
import com.yslee.subwaywhen.feature.search.vicinity.SearchVicinitySection
import com.yslee.subwaywhen.feature.detail.DetailSendModel
import com.yslee.subwaywhen.feature.search.modal.SaveStationModal
import com.yslee.subwaywhen.feature.search.modal.component.SaveCompletedModal
import com.yslee.subwaywhen.ui.common.CommonTopBarScreen
import com.yslee.subwaywhen.ui.theme.Dimens
import com.yslee.subwaywhen.ui.theme.SubwayWhenTheme

@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    onTabBarVisibilityChange: (Boolean) -> Unit = {},
    onNavigateToDetail: (DetailSendModel) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SearchScreenContent(
        uiState = uiState,
        onIntent = viewModel::onIntent,
        onSaveCompleted = { viewModel.onIntent(SearchIntent.SaveCompleted) },
        onTabBarVisibilityChange = onTabBarVisibilityChange,
        onNavigateToDetail = onNavigateToDetail,
    )
}

@Composable
private fun SearchScreenContent(
    uiState: SearchUiState,
    onIntent: (SearchIntent) -> Unit,
    onSaveCompleted: () -> Unit,
    onTabBarVisibilityChange: (Boolean) -> Unit = {},
    onNavigateToDetail: (DetailSendModel) -> Unit = {},
) {
    // 탭바 숨김: 어떤 모달이 열려 있든 항상 탭바를 숨긴다
    // - selectedStation: SaveStationModal
    // - isSaveCompletedModalVisible: SaveCompletedModal
    // - LocationListModal visibility는 SearchVicinitySection 내부에서 별도 콜백으로 제어
    val keyboardController = LocalSoftwareKeyboardController.current
    val isModalVisible = uiState.selectedStation != null || uiState.isSaveCompletedModalVisible
    LaunchedEffect(isModalVisible) {
        onTabBarVisibilityChange(!isModalVisible)
    }
    DisposableEffect(Unit) {
        onDispose { onTabBarVisibilityChange(true) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        CommonTopBarScreen(title = stringResource(R.string.tab_search), bottomPadding = Dimens.tabBarBottomPadding) {
            Column(
                verticalArrangement = Arrangement.spacedBy(Dimens.searchSectionGap),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = Dimens.searchSectionGap),
            ) {
                Spacer(modifier = Modifier.height(10.dp))
                SearchTextField(
                    isSearchMode = uiState.isSearchMode,
                    query = uiState.searchQuery,
                    onQueryChange = { onIntent(SearchIntent.QueryChanged(it)) },
                    onEnterSearchMode = { onIntent(SearchIntent.EnterSearchMode) },
                    onExitSearchMode = { onIntent(SearchIntent.ExitSearchMode) },
                )

                AnimatedVisibility(
                    visible = uiState.isSearchMode,
                    enter = fadeIn(tween(300)),
                    exit = fadeOut(tween(200)),
                ) {
                    SearchResultSection(
                        query = uiState.searchQuery,
                        isLoading = uiState.isSearchLoading,
                        result = uiState.searchResult,
                        onItemClick = { item ->
                            keyboardController?.hide()
                            onIntent(SearchIntent.ResultStationTapped(item))
                        },
                    )
                }

                AnimatedVisibility(
                    visible = uiState.filteredQueryRecommendList.isNotEmpty() && !uiState.isSearchLoading,
                    enter = fadeIn(tween(300)),
                    exit = fadeOut(tween(200)),
                ) {
                    SearchQueryRecommendSection(
                        items = uiState.filteredQueryRecommendList,
                        onItemClick = { onIntent(SearchIntent.QueryRecommendStationTapped(it)) },
                    )
                }

                AnimatedVisibility(
                    visible = !uiState.isSearchMode,
                    enter = fadeIn(tween(150)),
                    exit = fadeOut(tween(150)),
                ) {
                    SearchVicinitySection(
                        onStationSearch = { name -> onIntent(SearchIntent.VicinityStationSelected(name)) },
                        onTabBarVisibilityChange = onTabBarVisibilityChange,
                        onListStationSearch = { name -> onIntent(SearchIntent.VicinityListStationSelected(name)) },
                        onNavigateToDisposableDetail = onNavigateToDetail,
                    )
                }

                if (uiState.searchQuery.isEmpty()) {
                    SearchWordRecommendSection(
                        stations = uiState.recommendStations,
                        onItemClick = { onIntent(SearchIntent.RecommendStationTapped(it)) },
                    )
                }
            }
        }

        if (uiState.selectedStation != null) {
            SaveStationModal(
                station = uiState.selectedStation,
                onDismiss = { onIntent(SearchIntent.ModalDismissed) },
                onSaveCompleted = onSaveCompleted,
                onNavigateToDetail = { model ->
                    onIntent(SearchIntent.ModalDismissed)
                    onNavigateToDetail(model)
                },
            )
        }

        if (uiState.isSaveCompletedModalVisible) {
            SaveCompletedModal(
                onConfirm = { onIntent(SearchIntent.SaveCompletedDismissed) },
            )

            val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.congratulations))
            val progress by animateLottieCompositionAsState(composition, iterations = 1)
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(10f),
            )
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
            onSaveCompleted = {},
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
            onSaveCompleted = {},
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
            onSaveCompleted = {},
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
            onSaveCompleted = {},
        )
    }
}
