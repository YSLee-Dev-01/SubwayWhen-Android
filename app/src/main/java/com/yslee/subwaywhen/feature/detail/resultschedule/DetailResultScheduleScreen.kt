package com.yslee.subwaywhen.feature.detail.resultschedule

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yslee.subwaywhen.feature.detail.resultschedule.component.DetailResultScheduleCell
import com.yslee.subwaywhen.feature.detail.resultschedule.component.DetailResultScheduleHourHeader
import com.yslee.subwaywhen.ui.common.AnimatedTapBox
import com.yslee.subwaywhen.ui.common.AnimatedTapBoxAlignment
import com.yslee.subwaywhen.ui.common.CommonTopBar
import com.yslee.subwaywhen.ui.common.PrimaryButton
import com.yslee.subwaywhen.ui.common.modal.CommonModalBottomSheet
import com.yslee.subwaywhen.ui.theme.Dimens
import com.yslee.subwaywhen.ui.theme.SubwayWhenTheme

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

    LaunchedEffect(Unit) {
        viewModel.onIntent(DetailResultScheduleIntent.OnAppear)
    }

    LaunchedEffect(uiState.hourSections) {
        if (uiState.hourSections.isNotEmpty()) {
            listState.animateScrollToItem(uiState.currentHourIndex)
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

    Column(modifier = Modifier.fillMaxSize()) {
        CommonTopBar(
            title = "시간표",
            isSubTitleVisible = true,
            onBack = { viewModel.onIntent(DetailResultScheduleIntent.Back) },
        )

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
        ) {
            uiState.hourSections.forEachIndexed { sectionIndex, section ->
                item(key = "header_${section.hour}") {
                    DetailResultScheduleHourHeader(
                        label = section.label,
                        isCurrent = sectionIndex == uiState.currentHourIndex,
                    )
                }
                items(section.items, key = { "${section.hour}_${it.timeLabel}_${it.destination}" }) { item ->
                    DetailResultScheduleCell(item = item)
                }
            }

            item {
                AnimatedTapBox(
                    bgColor = MaterialTheme.colorScheme.errorContainer,
                    pressedColor = MaterialTheme.colorScheme.error,
                    alignment = AnimatedTapBoxAlignment.Center,
                    horizontalPadding = Dimens.paddingLR,
                    verticalPadding = 12.dp,
                    onClick = {
                        val firstDestination = uiState.hourSections.firstOrNull()?.items?.firstOrNull()?.destination ?: return@AnimatedTapBox
                        viewModel.onIntent(DetailResultScheduleIntent.ExceptionButtonTap(firstDestination))
                    },
                    modifier = Modifier.padding(Dimens.paddingLR).padding(bottom = Dimens.tabBarBottomPadding),
                ) {
                    Text(
                        text = "제외 행 설정",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
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

@Preview(name = "DetailResultScheduleScreen - Light", showBackground = true)
@Composable
private fun DetailResultScheduleScreenLightPreview() {
    SubwayWhenTheme(darkTheme = false) {
        Column {
            CommonTopBar(title = "시간표", isSubTitleVisible = true, onBack = {})
        }
    }
}
