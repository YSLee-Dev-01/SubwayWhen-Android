package com.yslee.subwaywhen.feature.home

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yslee.subwaywhen.R
import com.yslee.subwaywhen.feature.home.component.HomeEmptyStationCard
import com.yslee.subwaywhen.feature.home.component.HomeGroupTabBar
import com.yslee.subwaywhen.feature.home.component.HomeHeaderSection
import com.yslee.subwaywhen.feature.home.component.HomeMainTitleHeader
import com.yslee.subwaywhen.feature.home.component.HomeStationCard
import com.yslee.subwaywhen.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToSearch: () -> Unit = {},
    onNavigateToDetail: (HomeCellData) -> Unit = {},
    onCongestionTap: () -> Unit = {},
    onReportTap: () -> Unit = {},
    onEditTap: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // 요일별 타이틀 메시지 랜덤 선택
    val dayOfWeek = remember { todayDayOfWeek() }
    val titleArrayRes = when (dayOfWeek) {
        1 -> R.array.home_main_title_sunday
        2 -> R.array.home_main_title_monday
        3 -> R.array.home_main_title_tuesday
        4 -> R.array.home_main_title_wednesday
        5 -> R.array.home_main_title_thursday
        6 -> R.array.home_main_title_friday
        else -> R.array.home_main_title_saturday
    }
    val messages = stringArrayResource(titleArrayRes)
    val mainTitle = remember(dayOfWeek) { messages.random() }

    // SideEffect 처리 + 화면 진입 시 1회 데이터 로드
    LaunchedEffect(Unit) {
        viewModel.onIntent(HomeIntent.OnAppear)
        viewModel.effect.collect { effect ->
            when (effect) {
                HomeEffect.NavigateToSearch -> onNavigateToSearch()
                is HomeEffect.NavigateToDetail -> onNavigateToDetail(effect.cell)
                HomeEffect.NavigateToCongestion -> onCongestionTap()
                HomeEffect.NavigateToReport -> onReportTap()
                HomeEffect.NavigateToEdit -> onEditTap()
            }
        }
    }

    PullToRefreshBox(
        isRefreshing = uiState.isRefreshing,
        onRefresh = { viewModel.onIntent(HomeIntent.Refresh) },
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Dimens.paddingLR),
        ) {
            // 요일 타이틀
            item {
                Spacer(modifier = Modifier.height(Dimens.paddingTB))
                HomeMainTitleHeader(title = mainTitle)
                Spacer(modifier = Modifier.height(10.dp))
            }

            // 헤더 섹션 (혼잡도 + 민원/편집 버튼 + "실시간 현황" 라벨)
            item {
                HomeHeaderSection(
                    congestionEmoji = uiState.congestionEmoji,
                    onCongestionTap = { viewModel.onIntent(HomeIntent.CongestionTap) },
                    onReportTap = { viewModel.onIntent(HomeIntent.ReportTap) },
                    onEditTap = { viewModel.onIntent(HomeIntent.EditTap) },
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // 그룹 탭 (출근 / 퇴근)
            item {
                HomeGroupTabBar(
                    currentGroup = uiState.currentGroup,
                    onGroupTap = { viewModel.onIntent(HomeIntent.GroupTap(it)) },
                )
                Spacer(modifier = Modifier.height(Dimens.paddingTB))
            }

            // 역 카드 목록 또는 빈 상태
            if (uiState.cells.isEmpty()) {
                item {
                    HomeEmptyStationCard(
                        onTap = { viewModel.onIntent(HomeIntent.EmptyAddTap) },
                    )
                }
            } else {
                itemsIndexed(
                    items = uiState.cells,
                    key = { index, _ -> index },
                ) { _, cell ->
                    HomeStationCard(
                        cell = cell,
                        onCardTap = { viewModel.onIntent(HomeIntent.StationTap(cell)) },
                        onScheduleTap = { viewModel.onIntent(HomeIntent.ScheduleTap(cell)) },
                    )
                    Spacer(modifier = Modifier.height(Dimens.paddingTB))
                }
            }

            // 탭바 하단 여백
            item { Spacer(modifier = Modifier.height(Dimens.tabBarBottomPadding)) }
        }
    }
}
