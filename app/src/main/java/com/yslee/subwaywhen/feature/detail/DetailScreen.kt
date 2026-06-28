package com.yslee.subwaywhen.feature.detail

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yslee.subwaywhen.feature.detail.resultschedule.DetailResultScheduleSendModel
import com.yslee.subwaywhen.feature.detail.component.DetailArrivalSection
import com.yslee.subwaywhen.feature.detail.component.DetailScheduleSection
import com.yslee.subwaywhen.feature.detail.component.DetailStationHeaderView
import com.yslee.subwaywhen.feature.detail.component.DetailTrainPositionView
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import com.yslee.subwaywhen.ui.common.CommonTopBar
import com.yslee.subwaywhen.ui.common.CommonTopBarLazyScreen
import com.yslee.subwaywhen.ui.common.UpDownExceptionRow
import com.yslee.subwaywhen.ui.common.subwayLineTitle
import com.yslee.subwaywhen.ui.common.modal.ModalSubButton
import com.yslee.subwaywhen.ui.theme.Dimens
import com.yslee.subwaywhen.ui.theme.MainColorDark
import com.yslee.subwaywhen.ui.theme.MainColorLight
import com.yslee.subwaywhen.ui.theme.SubwayWhenTheme

@Composable
fun DetailScreen(
    sendModel: DetailSendModel,
    onBack: () -> Unit,
    onScheduleMoreTap: (DetailResultScheduleSendModel) -> Unit,
    onTabBarVisibilityChange: (Boolean) -> Unit,
    isDisposable: Boolean = false,
    viewModel: DetailViewModel = hiltViewModel(),
) {
    SideEffect { onTabBarVisibilityChange(false) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onIntent(DetailIntent.OnAppear)
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.onIntent(DetailIntent.OnDisappear)
        }
    }

    BackHandler { viewModel.onIntent(DetailIntent.Back) }

    if (uiState.showExceptionReloadDialog) {
        val exceptionStation = uiState.sendModel.exceptionLastStation
        AlertDialog(
            onDismissRequest = { viewModel.onIntent(DetailIntent.DialogDismissed) },
            text = {
                Text("${exceptionStation}행을 포함해서 재로딩 하시겠어요?\n재로딩은 일회성으로, 저장하지 않아요")
            },
            confirmButton = {
                TextButton(onClick = { viewModel.onIntent(DetailIntent.ExceptionReloadConfirmed) }) {
                    Text("재로딩")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onIntent(DetailIntent.DialogDismissed) }) {
                    Text("취소")
                }
            },
        )
    }

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is DetailEffect.NavigateToResultSchedule -> onScheduleMoreTap(effect.sendModel)
                DetailEffect.NavigateBack -> {
                    onTabBarVisibilityChange(true)
                    onBack()
                }
            }
        }
    }

    CommonTopBarLazyScreen(
        title = "${subwayLineTitle(sendModel.lineNumber)} ${sendModel.stationName}",
        isLargeTitleHidden = true,
        onBack = { viewModel.onIntent(DetailIntent.Back) },
        backIcon = if (isDisposable) Icons.Default.Close else null,
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
            Text(
                text = "실시간 현황",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                    trainIcon = uiState.trainIcon,
                    isLoading = uiState.isArrivalLoading,
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
                isArrivalLoading = uiState.isArrivalLoading,
                isAutoReloadEnabled = uiState.detailAutoReload,
                onRealtimeTap = { viewModel.onIntent(DetailIntent.RealtimeTap) },
                onRefresh = { viewModel.onIntent(DetailIntent.Refresh) },
            )
            Spacer(modifier = Modifier.height(Dimens.paddingTB))
        }

        item {
            DetailScheduleSection(
                scheduleItems = uiState.scheduleItems,
                lineNumber = sendModel.lineNumber,
                isUnowned = uiState.isUnowned,
                scheduleError = uiState.scheduleError,
                isScheduleLoading = uiState.isScheduleLoading,
                onScheduleMoreTap = { viewModel.onIntent(DetailIntent.ScheduleMoreTap) },
            )
            Spacer(modifier = Modifier.height(Dimens.paddingTB))
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(Dimens.paddingTB),
            ) {
                Text(
                    text = "기타",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                ModalSubButton(
                    text = "${subwayLineTitle(sendModel.lineNumber)} 민원접수",
                    bgColor = if (isSystemInDarkTheme()) MainColorDark else MainColorLight,
                    textColor = Color.Red,
                    onClick = { viewModel.onIntent(DetailIntent.ReportTap) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
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
