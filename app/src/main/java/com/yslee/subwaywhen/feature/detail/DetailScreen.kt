package com.yslee.subwaywhen.feature.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yslee.subwaywhen.feature.detail.component.DetailArrivalSection
import com.yslee.subwaywhen.feature.detail.component.DetailScheduleSection
import com.yslee.subwaywhen.feature.detail.component.DetailStationHeaderView
import com.yslee.subwaywhen.feature.detail.component.DetailTrainPositionView
import com.yslee.subwaywhen.ui.common.CommonTopBar
import com.yslee.subwaywhen.ui.common.CommonTopBarLazyScreen
import com.yslee.subwaywhen.ui.common.UpDownExceptionRow
import com.yslee.subwaywhen.ui.theme.Dimens
import com.yslee.subwaywhen.ui.theme.SubwayWhenTheme

@Composable
fun DetailScreen(
    sendModel: DetailSendModel,
    onBack: () -> Unit,
    onScheduleMoreTap: (List<DetailScheduleItem>) -> Unit,
    onTabBarVisibilityChange: (Boolean) -> Unit,
    viewModel: DetailViewModel = hiltViewModel(),
) {
    SideEffect { onTabBarVisibilityChange(false) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onIntent(DetailIntent.OnAppear)
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.onIntent(DetailIntent.OnDisappear) }
    }

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is DetailEffect.NavigateToResultSchedule -> onScheduleMoreTap(effect.scheduleItems)
                DetailEffect.NavigateBack -> onBack()
            }
        }
    }

    CommonTopBarLazyScreen(
        title = "${sendModel.lineNumber} ${sendModel.stationName}",
        isLargeTitleHidden = true,
        onBack = { viewModel.onIntent(DetailIntent.Back) },
        bottomPadding = Dimens.tabBarBottomPadding,
    ) {
        item {
            DetailStationHeaderView(
                prevStationName = uiState.prevStationName,
                stationName = sendModel.stationName,
                nextStationName = uiState.nextStationName,
                lineNumber = sendModel.lineNumber,
            )
            Spacer(modifier = Modifier.height(Dimens.paddingTB))
        }

        item {
            UpDownExceptionRow(
                upDownText = sendModel.upDown,
                exceptionText = uiState.sendModel.exceptionLastStation,
                onExceptionClick = { viewModel.onIntent(DetailIntent.ExceptionRowTap) },
            )
            Spacer(modifier = Modifier.height(Dimens.paddingTB))
        }

        item {
            val first = uiState.firstArrival
            if (first != null) {
                DetailTrainPositionView(
                    prevStationName = first.prevStationName,
                    currentStationName = sendModel.stationName,
                    nextStationName = first.nextStationName,
                    statusCode = first.statusCode,
                    isFast = first.isFast,
                    lineNumber = sendModel.lineNumber,
                )
                Spacer(modifier = Modifier.height(Dimens.paddingTB))
            }
        }

        item {
            DetailArrivalSection(
                exceptionLastStation = uiState.sendModel.exceptionLastStation,
                firstArrival = uiState.firstArrival,
                secondArrival = uiState.secondArrival,
                arrivalError = uiState.arrivalError,
                lineNumber = sendModel.lineNumber,
                timerCount = uiState.timerCount,
                isRefreshCooldown = uiState.isRefreshCooldown,
                onRealtimeTap = { viewModel.onIntent(DetailIntent.RealtimeTap) },
                onRefresh = { viewModel.onIntent(DetailIntent.Refresh) },
            )
            Spacer(modifier = Modifier.height(Dimens.paddingTB))
        }

        item {
            DetailScheduleSection(
                scheduleItems = uiState.scheduleItems,
                isUnowned = uiState.isUnowned,
                scheduleError = uiState.scheduleError,
                onScheduleMoreTap = { viewModel.onIntent(DetailIntent.ScheduleMoreTap) },
            )
        }
    }
}

@Preview(name = "DetailScreen - Light", showBackground = true)
@Composable
private fun DetailScreenLightPreview() {
    SubwayWhenTheme(darkTheme = false) {
        val model = DetailSendModel(
            upDown = "상행",
            stationName = "불광",
            lineNumber = "03호선",
            stationCode = "341",
            lineCode = "1003",
            exceptionLastStation = "",
            korailCode = "",
        )
        Column {
            CommonTopBar(title = "${model.lineNumber} ${model.stationName}", isSubTitleVisible = true, onBack = {})
        }
    }
}
