package com.yslee.subwaywhen.feature.home

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yslee.subwaywhen.R
import com.yslee.subwaywhen.feature.home.component.HomeEmptyStationCard
import com.yslee.subwaywhen.feature.home.component.HomeGroupTabBar
import com.yslee.subwaywhen.feature.home.component.HomeHeaderSection
import com.yslee.subwaywhen.feature.home.component.HomeStationCard
import com.yslee.subwaywhen.feature.home.modal.CongestionModal
import com.yslee.subwaywhen.ui.common.CommonTopBarLazyScreen
import com.yslee.subwaywhen.ui.theme.Dimens

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToSearch: () -> Unit = {},
    onNavigateToDetail: (HomeCellData) -> Unit = {},
    onTabBarVisibilityChange: (Boolean) -> Unit = {},
    onReportTap: () -> Unit = {},
    onEditTap: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lazyListState = rememberLazyListState()
    var isCongestionModalVisible by remember { mutableStateOf(false) }

    // 요일별 타이틀 메시지 랜덤 선택 — dayOfWeek 단위로 고정 (앱 세션 내 불변)
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
    // iOS: refreshEvent 시 mainTitleLoad() 재호출 → 새 랜덤 메시지. mainTitleVersion이 바뀔 때마다 재랜덤.
    val mainTitle = remember(dayOfWeek, uiState.mainTitleVersion) { messages.random() }

    // Effect 수집은 Intent 전달보다 먼저 구독을 시작해야 유실되지 않는다.
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                HomeEffect.NavigateToSearch -> onNavigateToSearch()
                is HomeEffect.NavigateToDetail -> onNavigateToDetail(effect.cell)
                HomeEffect.NavigateToCongestion -> {
                    isCongestionModalVisible = true
                    onTabBarVisibilityChange(false)
                }
                HomeEffect.NavigateToReport -> onReportTap()
                HomeEffect.NavigateToEdit -> onEditTap()
            }
        }
    }
    // 화면 진입 시 1회 데이터 로드 — 별도 키로 분리해 effect collect 구독 이후 실행 보장
    LaunchedEffect("load") {
        viewModel.onIntent(HomeIntent.OnAppear)
    }

    CommonTopBarLazyScreen(
        title = "홈",
        largeTitle = mainTitle,
        listState = lazyListState,
        isRefreshing = uiState.isRefreshing,
        onRefresh = { viewModel.onIntent(HomeIntent.Refresh) },
        bottomPadding = Dimens.tabBarBottomPadding,
    ) {
        // 헤더 섹션 (혼잡도 + 민원/편집 버튼 + "실시간 현황" 라벨)
        item {
            Spacer(modifier = Modifier.height(10.dp))
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
                key = { _, cell -> "${cell.stationIndex}_${cell.subIndex}" },
            ) { _, cell ->
                HomeStationCard(
                    cell = cell,
                    onCardTap = { viewModel.onIntent(HomeIntent.StationTap(cell)) },
                    onScheduleTap = { viewModel.onIntent(HomeIntent.ScheduleTap(cell)) },
                )
                Spacer(modifier = Modifier.height(Dimens.paddingTB + 10.dp))
            }
        }
    }

    if (isCongestionModalVisible) {
        CongestionModal(
            onDismiss = {
                isCongestionModalVisible = false
                onTabBarVisibilityChange(true)
            },
        )
    }
}
