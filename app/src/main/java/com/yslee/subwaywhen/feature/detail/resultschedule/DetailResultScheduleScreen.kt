package com.yslee.subwaywhen.feature.detail.resultschedule

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yslee.subwaywhen.feature.detail.resultschedule.component.DetailResultScheduleCell
import com.yslee.subwaywhen.feature.detail.resultschedule.component.DetailResultScheduleHourHeader
import com.yslee.subwaywhen.ui.common.MainBgCard
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
    val density = LocalDensity.current
    val scrollThreshold = remember(density) { with(density) { 25.dp.toPx() }.toInt() }

    val isHeaderExpanded by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > scrollThreshold
        }
    }

    LaunchedEffect(Unit) {
        viewModel.onIntent(DetailResultScheduleIntent.OnAppear)
    }

    LaunchedEffect(uiState.hourSections) {
        if (uiState.hourSections.isNotEmpty()) {
            listState.animateScrollToItem(uiState.currentHourIndex * 2)
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
        StickyScheduleHeader(
            stationName = uiState.stationName,
            upDown = uiState.upDown,
            exceptionLastStation = uiState.exceptionLastStation,
            isExpanded = isHeaderExpanded,
            onBack = { viewModel.onIntent(DetailResultScheduleIntent.Back) },
            onExceptionTap = {
                viewModel.onIntent(DetailResultScheduleIntent.ExceptionButtonTap(uiState.exceptionLastStation))
            },
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(45.dp)
                .padding(horizontal = Dimens.paddingLR),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(24.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
            Text(
                text = stationName,
                fontSize = Dimens.fontSizeLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 1.dp),
            )
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(spring(dampingRatio = Spring.DampingRatioLowBouncy)) + fadeIn(chipSpring),
            exit = shrinkVertically(spring(dampingRatio = Spring.DampingRatioLowBouncy)) + fadeOut(chipSpring),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(45.dp)
                    .padding(horizontal = Dimens.paddingLR, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(Dimens.cornerRadius))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = upDown,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                val hasException = exceptionLastStation.isNotEmpty()
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(Dimens.cornerRadius))
                        .background(MaterialTheme.colorScheme.primary)
                        .then(if (hasException) Modifier.clickable { onExceptionTap() } else Modifier),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (hasException) "${exceptionLastStation}행 제외" else "제외 행 없음",
                        color = if (hasException) Color.Red else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
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
            isExpanded = true,
            onBack = {},
            onExceptionTap = {},
        )
    }
}
