package com.yslee.subwaywhen.feature.detail.resultschedule

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yslee.subwaywhen.feature.detail.resultschedule.component.DetailResultScheduleCell
import com.yslee.subwaywhen.feature.detail.resultschedule.component.DetailResultScheduleHourHeader
import com.yslee.subwaywhen.ui.common.CommonTopBar
import com.yslee.subwaywhen.ui.common.MainBgCard
import com.yslee.subwaywhen.ui.common.PrimaryButton
import com.yslee.subwaywhen.ui.common.UpDownExceptionRow
import com.yslee.subwaywhen.ui.common.modal.CommonModalBottomSheet
import com.yslee.subwaywhen.ui.theme.Dimens
import com.yslee.subwaywhen.ui.theme.SubwayWhenTheme
import kotlinx.coroutines.delay

@Composable
fun DetailResultScheduleScreen(
    onBack: () -> Unit,
    onNavigateBackWithException: (String) -> Unit,
    onTabBarVisibilityChange: (Boolean) -> Unit,
    viewModel: DetailResultScheduleViewModel = hiltViewModel(),
) {
    SideEffect { onTabBarVisibilityChange(false) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val density = LocalDensity.current
    val scrollThreshold = remember(density) { with(density) { 25.dp.toPx() }.toInt() }

    // 큰 타이틀(item 0)이 스크롤되기 시작하면 상단바 소제목 표시
    val isTitleVisible by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > scrollThreshold
        }
    }

    // UpDownExceptionRow(item 1)가 스크롤 아웃되면 sticky 칩 표시
    val isChipsExpanded by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 1 ||
                (listState.firstVisibleItemIndex == 1 && listState.firstVisibleItemScrollOffset > scrollThreshold)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.onIntent(DetailResultScheduleIntent.OnAppear)
    }

    // push 애니메이션 완료 후 스크롤 — 동시 실행 시 버벅거림 방지
    LaunchedEffect(uiState.hourSections) {
        if (uiState.hourSections.isNotEmpty()) {
            delay(350L)
            // large_title(0) + up_down_row(1) 오프셋 2 적용
            listState.animateScrollToItem(2 + uiState.currentHourIndex * 2)
        }
    }

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is DetailResultScheduleEffect.NavigateBackWithException -> onNavigateBackWithException(effect.exceptionLastStation)
                DetailResultScheduleEffect.NavigateBack -> onBack()
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface).statusBarsPadding()) {
        StickyScheduleHeader(
            stationName = uiState.stationName,
            upDown = uiState.upDown,
            exceptionLastStation = uiState.exceptionLastStation,
            isSubTitleVisible = isTitleVisible,
            isExpanded = isChipsExpanded,
            onBack = { viewModel.onIntent(DetailResultScheduleIntent.Back) },
            onExceptionTap = {
                viewModel.onIntent(DetailResultScheduleIntent.ExceptionButtonTap(uiState.exceptionLastStation))
            },
        )

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
        ) {
            item(key = "large_title") {
                Text(
                    text = uiState.stationName,
                    fontSize = Dimens.fontSizeMainTitle,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = Dimens.paddingLR),
                )
                Spacer(modifier = Modifier.height(Dimens.paddingTB))
            }
            item(key = "up_down_row") {
                Box(modifier = Modifier.padding(horizontal = Dimens.paddingLR)) {
                    UpDownExceptionRow(
                        upDownText = uiState.upDown,
                        exceptionText = uiState.exceptionLastStation,
                        onExceptionClick = {
                            viewModel.onIntent(DetailResultScheduleIntent.ExceptionButtonTap(uiState.exceptionLastStation))
                        },
                    )
                }
                Spacer(modifier = Modifier.height(Dimens.paddingTB))
            }
            uiState.hourSections.forEachIndexed { sectionIndex, section ->
                item(key = "header_${section.hour}") {
                    DetailResultScheduleHourHeader(
                        label = section.label,
                        isCurrent = sectionIndex == uiState.currentHourIndex,
                    )
                }
                item(key = "card_${section.hour}") {
                    MainBgCard(
                        modifier = Modifier
                            .padding(horizontal = Dimens.paddingLR)
                            .fillMaxWidth(),
                    ) {
                        Column {
                            section.items.forEach { item ->
                                DetailResultScheduleCell(item = item)
                            }
                        }
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(Dimens.tabBarBottomPadding))
            }
        }
    }

    if (uiState.isExceptionModalVisible) {
        val destination = uiState.selectedDestination ?: ""
        CommonModalBottomSheet(
            mainTitle = "제외 행 설정",
            subTitle = "${destination}행 열차를 제외하시겠어요?",
            onDismiss = { viewModel.onIntent(DetailResultScheduleIntent.ExceptionDismiss) },
            confirmButton = { animatedDismiss ->
                PrimaryButton(
                    text = "확인",
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    onClick = {
                        animatedDismiss()
                        viewModel.onIntent(DetailResultScheduleIntent.ExceptionConfirm)
                    },
                    modifier = Modifier.padding(horizontal = Dimens.paddingLR),
                )
            },
        ) {}
    }
}

@Composable
private fun StickyScheduleHeader(
    stationName: String,
    upDown: String,
    exceptionLastStation: String,
    isSubTitleVisible: Boolean,
    isExpanded: Boolean,
    onBack: () -> Unit,
    onExceptionTap: () -> Unit,
) {
    val chipSpring = spring<Float>(dampingRatio = Spring.DampingRatioLowBouncy)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        CommonTopBar(
            title = stationName,
            isSubTitleVisible = isSubTitleVisible,
            onBack = onBack,
        )

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(spring(dampingRatio = Spring.DampingRatioLowBouncy)) + fadeIn(chipSpring),
            exit = shrinkVertically(spring(dampingRatio = Spring.DampingRatioLowBouncy)) + fadeOut(chipSpring),
        ) {
            Box(modifier = Modifier.padding(horizontal = Dimens.paddingLR, vertical = 6.dp)) {
                UpDownExceptionRow(
                    upDownText = upDown,
                    exceptionText = exceptionLastStation,
                    onExceptionClick = onExceptionTap,
                )
            }
        }
    }
}

@Preview(name = "DetailResultScheduleScreen - Light", showBackground = true)
@Composable
private fun DetailResultScheduleScreenLightPreview() {
    SubwayWhenTheme(darkTheme = false) {
        StickyScheduleHeader(
            stationName = "불광",
            upDown = "상행",
            exceptionLastStation = "노원",
            isSubTitleVisible = true,
            isExpanded = true,
            onBack = {},
            onExceptionTap = {},
        )
    }
}
