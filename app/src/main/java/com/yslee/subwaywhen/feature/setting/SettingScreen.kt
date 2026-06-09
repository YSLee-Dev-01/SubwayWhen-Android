package com.yslee.subwaywhen.feature.setting

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
import com.yslee.subwaywhen.ui.common.CommonTopBar
import com.yslee.subwaywhen.ui.common.MainBgCard
import com.yslee.subwaywhen.ui.theme.Dimens
import com.yslee.subwaywhen.ui.theme.MainColorDark
import com.yslee.subwaywhen.ui.theme.MainColorLight
import com.yslee.subwaywhen.ui.theme.SubwayWhenTheme

@Composable
fun SettingScreen(
    viewModel: SettingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SettingScreenContent(
        uiState = uiState,
        onIntent = viewModel::onIntent,
    )
}

@Composable
private fun SettingScreenContent(
    uiState: SettingUiState,
    onIntent: (SettingIntent) -> Unit,
) {
    val isDark = isSystemInDarkTheme()
    val cardBg = if (isDark) MainColorDark else MainColorLight

    Column(modifier = Modifier.fillMaxSize()) {
        CommonTopBar(title = stringResource(R.string.setting_title), isSubTitleVisible = true)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.paddingLR),
        ) {
            Spacer(Modifier.height(12.dp))

            // 섹션 1. 홈 화면
            SettingSectionHeader(title = stringResource(R.string.setting_section_home))
            Spacer(Modifier.height(4.dp))
            SettingTimeRow(
                workTime = uiState.saveSetting.mainGroupOneTime,
                leaveTime = uiState.saveSetting.mainGroupTwoTime,
                expandedGroup = uiState.expandedTimeGroup,
                onGroupTapped = { onIntent(SettingIntent.TimeGroupTapped(it)) },
                onSave = { group, time -> onIntent(SettingIntent.TimeSaved(group, time)) },
                modifier = Modifier.padding(bottom = 8.dp),
            )
            MainBgCard(modifier = Modifier.fillMaxWidth()) {
                SettingArrowRow(
                    title = stringResource(R.string.setting_work_alarm),
                    onTap = { onIntent(SettingIntent.WorkAlarmTapped) },
                )
            }
            Spacer(Modifier.height(8.dp))
            MainBgCard(modifier = Modifier.fillMaxWidth()) {
                SettingTextFieldRow(
                    title = stringResource(R.string.setting_congestion_label),
                    value = uiState.saveSetting.mainCongestionLabel,
                    onValueChange = { onIntent(SettingIntent.CongestionLabelChanged(it)) },
                    onFocusLost = { onIntent(SettingIntent.CongestionLabelFocusLost) },
                )
            }

            Spacer(Modifier.height(16.dp))

            // 섹션 2. 상세 화면
            SettingSectionHeader(title = stringResource(R.string.setting_section_detail))
            Spacer(Modifier.height(4.dp))
            MainBgCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    SettingToggleRow(
                        title = stringResource(R.string.setting_auto_reload),
                        checked = uiState.saveSetting.detailAutoReload,
                        onToggle = { onIntent(SettingIntent.ToggleChanged(SettingToggleField.AutoReload)) },
                    )
                    SettingToggleRow(
                        title = stringResource(R.string.setting_schedule_auto_time),
                        checked = uiState.saveSetting.detailScheduleAutoTime,
                        onToggle = { onIntent(SettingIntent.ToggleChanged(SettingToggleField.ScheduleAutoTime)) },
                    )
                    SettingArrowRow(
                        title = stringResource(R.string.setting_train_icon),
                        onTap = { onIntent(SettingIntent.TrainIconTapped) },
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // 섹션 3. 검색 화면
            SettingSectionHeader(title = stringResource(R.string.setting_section_search))
            Spacer(Modifier.height(4.dp))
            MainBgCard(modifier = Modifier.fillMaxWidth()) {
                SettingToggleRow(
                    title = stringResource(R.string.setting_search_overlap),
                    checked = uiState.saveSetting.searchOverlapAlert,
                    onToggle = { onIntent(SettingIntent.ToggleChanged(SettingToggleField.SearchOverlap)) },
                )
            }

            Spacer(Modifier.height(16.dp))

            // 섹션 4. 기타
            SettingSectionHeader(title = stringResource(R.string.setting_section_etc))
            Spacer(Modifier.height(4.dp))
            MainBgCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    SettingArrowRow(
                        title = stringResource(R.string.setting_license),
                        onTap = { onIntent(SettingIntent.LicenseTapped) },
                    )
                    SettingArrowRow(
                        title = stringResource(R.string.setting_contents),
                        onTap = { onIntent(SettingIntent.ContentsTapped) },
                    )
                }
            }

            Spacer(Modifier.height(Dimens.tabBarBottomPadding))
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
