package com.yslee.subwaywhen.feature.setting

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yslee.subwaywhen.R
import com.yslee.subwaywhen.feature.setting.component.SettingArrowRow
import com.yslee.subwaywhen.feature.setting.component.SettingSectionHeader
import com.yslee.subwaywhen.feature.setting.component.SettingTextFieldRow
import com.yslee.subwaywhen.feature.setting.component.SettingTimeRow
import com.yslee.subwaywhen.feature.setting.component.SettingToggleRow
import com.yslee.subwaywhen.feature.setting.modal.ContentsModal
import com.yslee.subwaywhen.feature.setting.modal.LicenseModal
import com.yslee.subwaywhen.feature.setting.modal.TrainIconModal
import com.yslee.subwaywhen.feature.setting.modal.WorkAlarmModal
import com.yslee.subwaywhen.ui.common.CommonTopBarScreen
import com.yslee.subwaywhen.ui.common.MainBgCard
import com.yslee.subwaywhen.ui.theme.Dimens
import com.yslee.subwaywhen.ui.theme.SubwayWhenTheme

@Composable
fun SettingScreen(
    onTabBarVisibilityChange: (Boolean) -> Unit = {},
    viewModel: SettingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SettingScreenContent(
        uiState = uiState,
        onIntent = viewModel::onIntent,
        onTabBarVisibilityChange = onTabBarVisibilityChange,
    )
}

private fun checkNotificationPermission(context: android.content.Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
}

@Composable
private fun SettingScreenContent(
    uiState: SettingUiState,
    onIntent: (SettingIntent) -> Unit,
    onTabBarVisibilityChange: (Boolean) -> Unit = {},
) {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        onIntent(SettingIntent.WorkAlarmOpened(hasPermission = granted))
    }
    LaunchedEffect(uiState.activeModal) {
        onTabBarVisibilityChange(uiState.activeModal == null)
    }
    CommonTopBarScreen(
        title = stringResource(R.string.setting_title),
        bottomPadding = Dimens.tabBarBottomPadding,
    ) {
        // 섹션 1. 홈 화면
        SettingSectionHeader(title = stringResource(R.string.setting_section_home))
        Spacer(Modifier.height(7.5.dp))
        SettingTimeRow(
            workTime = uiState.saveSetting.mainGroupOneTime,
            leaveTime = uiState.saveSetting.mainGroupTwoTime,
            expandedGroup = uiState.expandedTimeGroup,
            workHasAlert = uiState.saveSetting.alertGroupOneId.isNotEmpty(),
            leaveHasAlert = uiState.saveSetting.alertGroupTwoId.isNotEmpty(),
            onGroupTapped = { onIntent(SettingIntent.TimeGroupTapped(it)) },
            onSave = { group, time -> onIntent(SettingIntent.TimeSaved(group, time)) },
        )
        Spacer(Modifier.height(10.dp))
        MainBgCard(modifier = Modifier.fillMaxWidth()) {
            SettingArrowRow(
                title = stringResource(R.string.setting_work_alarm),
                onTap = {
                    if (checkNotificationPermission(context)) {
                        onIntent(SettingIntent.WorkAlarmOpened(hasPermission = true))
                    } else {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                },
            )
        }
        Spacer(Modifier.height(10.dp))
        MainBgCard(modifier = Modifier.fillMaxWidth()) {
            SettingTextFieldRow(
                title = stringResource(R.string.setting_congestion_label),
                value = uiState.saveSetting.mainCongestionLabel,
                onValueChange = { onIntent(SettingIntent.CongestionLabelChanged(it)) },
                onFocusLost = { onIntent(SettingIntent.CongestionLabelFocusLost) },
            )
        }

        Spacer(Modifier.height(15.dp))

        // 섹션 2. 상세 화면
        SettingSectionHeader(title = stringResource(R.string.setting_section_detail))
        Spacer(Modifier.height(7.5.dp))
        MainBgCard(modifier = Modifier.fillMaxWidth()) {
            SettingToggleRow(
                title = stringResource(R.string.setting_auto_reload),
                checked = uiState.saveSetting.detailAutoReload,
                onToggle = { onIntent(SettingIntent.ToggleChanged(SettingToggleField.AutoReload)) },
            )
        }
        Spacer(Modifier.height(10.dp))
        MainBgCard(modifier = Modifier.fillMaxWidth()) {
            SettingToggleRow(
                title = stringResource(R.string.setting_schedule_auto_time),
                checked = uiState.saveSetting.detailScheduleAutoTime,
                onToggle = { onIntent(SettingIntent.ToggleChanged(SettingToggleField.ScheduleAutoTime)) },
            )
        }
        Spacer(Modifier.height(10.dp))
        MainBgCard(modifier = Modifier.fillMaxWidth()) {
            SettingArrowRow(
                title = stringResource(R.string.setting_train_icon),
                onTap = { onIntent(SettingIntent.TrainIconTapped) },
            )
        }

        Spacer(Modifier.height(15.dp))

        // 섹션 3. 검색 화면
        SettingSectionHeader(title = stringResource(R.string.setting_section_search))
        Spacer(Modifier.height(7.5.dp))
        MainBgCard(modifier = Modifier.fillMaxWidth()) {
            SettingToggleRow(
                title = stringResource(R.string.setting_search_overlap),
                checked = uiState.saveSetting.searchOverlapAlert,
                onToggle = { onIntent(SettingIntent.ToggleChanged(SettingToggleField.SearchOverlap)) },
            )
        }

        Spacer(Modifier.height(15.dp))

        // 섹션 4. 기타
        SettingSectionHeader(title = stringResource(R.string.setting_section_etc))
        Spacer(Modifier.height(7.5.dp))
        MainBgCard(modifier = Modifier.fillMaxWidth()) {
            SettingArrowRow(
                title = stringResource(R.string.setting_license),
                onTap = { onIntent(SettingIntent.LicenseTapped) },
            )
        }
        Spacer(Modifier.height(10.dp))
        MainBgCard(modifier = Modifier.fillMaxWidth()) {
            SettingArrowRow(
                title = stringResource(R.string.setting_contents),
                onTap = { onIntent(SettingIntent.ContentsTapped) },
            )
        }
    }

    when (uiState.activeModal) {
        SettingModalType.TrainIcon -> TrainIconModal(
            currentIcon = uiState.saveSetting.detailVcTrainIcon,
            onIconSelected = { onIntent(SettingIntent.TrainIconSelected(it)) },
            onDismiss = { onIntent(SettingIntent.ModalDismissed) },
        )
        SettingModalType.License -> LicenseModal(
            licenses = uiState.modalLicenses,
            isLoading = uiState.isModalLoading,
            onDismiss = { onIntent(SettingIntent.ModalDismissed) },
        )
        SettingModalType.Contents -> ContentsModal(
            contents = uiState.modalContents,
            isLoading = uiState.isModalLoading,
            onDismiss = { onIntent(SettingIntent.ModalDismissed) },
        )
        SettingModalType.WorkAlarm -> WorkAlarmModal(
            hasPermission = uiState.hasNotificationPermission,
            isWeekendIncluded = uiState.isWeekendIncluded,
            groupOneStation = uiState.workAlarmGroupOneStation,
            groupTwoStation = uiState.workAlarmGroupTwoStation,
            groupOneStations = uiState.groupOneStations,
            groupTwoStations = uiState.groupTwoStations,
            selectGroup = uiState.workAlarmSelectGroup,
            onWeekendToggled = { onIntent(SettingIntent.WeekendToggled) },
            onStationTapped = { onIntent(SettingIntent.WorkAlarmStationTapped(it)) },
            onStationSelected = { onIntent(SettingIntent.WorkAlarmStationSelected(it)) },
            onAlarmOff = { onIntent(SettingIntent.WorkAlarmStationReset(it)) },
            onSelectBack = { onIntent(SettingIntent.WorkAlarmSelectPopped) },
            onSave = { onIntent(SettingIntent.WorkAlarmSaved) },
            onDismiss = { onIntent(SettingIntent.ModalDismissed) },
        )
        null -> Unit
    }
}

@Preview(name = "SettingScreen - Light", showBackground = true)
@Composable
private fun SettingScreenLightPreview() {
    SubwayWhenTheme(darkTheme = false) {
        SettingScreenContent(uiState = SettingUiState(), onIntent = {})
    }
}

@Preview(name = "SettingScreen - Dark", showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun SettingScreenDarkPreview() {
    SubwayWhenTheme(darkTheme = true) {
        SettingScreenContent(uiState = SettingUiState(), onIntent = {})
    }
}
