package com.yslee.subwaywhen.feature.setting.modal

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.yslee.subwaywhen.R
import com.yslee.subwaywhen.data.model.SaveStation
import com.yslee.subwaywhen.feature.setting.TimeGroup
import com.yslee.subwaywhen.feature.setting.modal.component.WorkAlarmSelectView
import com.yslee.subwaywhen.feature.setting.modal.component.WorkAlarmStationView
import com.yslee.subwaywhen.ui.common.PrimaryButton
import com.yslee.subwaywhen.ui.common.modal.CommonModalBottomSheet
import com.yslee.subwaywhen.ui.theme.Dimens

@Composable
fun WorkAlarmModal(
    hasPermission: Boolean,
    isWeekendIncluded: Boolean,
    groupOneStation: SaveStation?,
    groupTwoStation: SaveStation?,
    groupOneStations: List<SaveStation>,
    groupTwoStations: List<SaveStation>,
    selectGroup: TimeGroup?,
    onWeekendToggled: () -> Unit,
    onStationTapped: (TimeGroup) -> Unit,
    onStationSelected: (SaveStation) -> Unit,
    onAlarmOff: (TimeGroup) -> Unit,
    onSelectBack: () -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    CommonModalBottomSheet(
        mainTitle = "출퇴근 지하철역",
        subTitle = if (hasPermission) "출퇴근 시간에 맞게 정해놓은 지하철역으로 알림을 주는 기능이에요."
                   else "알림 권한이 설정되어 있지 않아요.",
        onDismiss = onDismiss,
        topDecoration = if (hasPermission) {
            {
                ExplanationBanner()
            }
        } else null,
        confirmButton = { animatedDismiss ->
            PrimaryButton(
                text = if (hasPermission) "저장" else "닫기",
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                onClick = {
                    if (hasPermission) onSave()
                    animatedDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
            )
        },
    ) {
        AnimatedContent(
            targetState = selectGroup,
            transitionSpec = {
                if (targetState != null) {
                    slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                } else {
                    slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
                }
            },
            label = "work_alarm_slide",
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = Dimens.modalContentMaxHeight),
        ) { group ->
            if (group == null) {
                if (hasPermission) {
                    WorkAlarmStationView(
                        isWeekendIncluded = isWeekendIncluded,
                        groupOneStation = groupOneStation,
                        groupTwoStation = groupTwoStation,
                        onWeekendToggled = onWeekendToggled,
                        onStationTapped = onStationTapped,
                    )
                } else {
                    NoPermissionContent()
                }
            } else {
                val stations = if (group == TimeGroup.Work) groupOneStations else groupTwoStations
                val selected = if (group == TimeGroup.Work) groupOneStation else groupTwoStation
                val title = if (group == TimeGroup.Work) "출근" else "퇴근"
                WorkAlarmSelectView(
                    title = title,
                    stations = stations,
                    selectedStation = selected,
                    onBack = onSelectBack,
                    onStationSelected = onStationSelected,
                    onAlarmOff = { onAlarmOff(group) },
                )
            }
        }
    }
}

@Composable
private fun ExplanationBanner() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.paddingInner)
            .background(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                shape = RoundedCornerShape(Dimens.cornerRadius),
            )
            .height(40.dp),
    ) {
        Text(
            text = "출근/퇴근 시간이 0시로 설정되어 있으면 알림이 울리지 않아요.",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun NoPermissionContent() {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.report))
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
    ) {
        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever,
        )
    }
}
