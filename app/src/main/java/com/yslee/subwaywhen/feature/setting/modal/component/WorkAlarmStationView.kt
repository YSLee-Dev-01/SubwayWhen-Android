package com.yslee.subwaywhen.feature.setting.modal.component

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yslee.subwaywhen.data.model.SaveStation
import com.yslee.subwaywhen.feature.setting.TimeGroup
import com.yslee.subwaywhen.ui.common.AnimatedTapBox
import com.yslee.subwaywhen.ui.common.StationRow
import com.yslee.subwaywhen.ui.theme.Dimens
import com.yslee.subwaywhen.ui.theme.AppIconColor
import com.yslee.subwaywhen.ui.theme.MainColorDark
import com.yslee.subwaywhen.ui.theme.MainColorLight

@Composable
fun WorkAlarmStationView(
    isWeekendIncluded: Boolean,
    groupOneStation: SaveStation?,
    groupTwoStation: SaveStation?,
    onWeekendToggled: () -> Unit,
    onStationTapped: (TimeGroup) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) MainColorDark else MainColorLight
    val pressedColor = if (isDark) MainColorDark.copy(alpha = 0.7f) else MainColorLight.copy(alpha = 0.7f)

    Column(modifier = modifier.padding(vertical = 8.dp)) {
        // 주말 포함 토글 행
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "주말포함",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.width(Dimens.paddingLR))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .height(60.dp)
                    .background(bgColor, RoundedCornerShape(Dimens.cornerRadius))
                    .padding(horizontal = 15.dp),
            ) {
                Text(
                    text = if (isWeekendIncluded) "월 화 수 목 금 토 일" else "월 화 수 목 금",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                Switch(
                    checked = isWeekendIncluded,
                    onCheckedChange = { onWeekendToggled() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.surface,
                        checkedTrackColor = AppIconColor,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surface,
                    ),
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 출근/퇴근 헤더 + 역 선택 버튼
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.paddingLR),
        ) {
            // 출근 열
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "출근시간",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = Dimens.paddingTB),
                )
                AnimatedTapBox(bgColor = bgColor, pressedColor = pressedColor, onClick = { onStationTapped(TimeGroup.Work) }) {
                    StationRow(
                        station = groupOneStation,
                        modifier = Modifier.fillMaxWidth(),
                        showUpDown = false,
                        circleSize = Dimens.stationCircleSizeSmall,
                        circleFontSize = Dimens.stationCircleSmallFontSize,
                    )
                }
            }

            // 퇴근 열
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "퇴근시간",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = Dimens.paddingTB),
                )
                AnimatedTapBox(bgColor = bgColor, pressedColor = pressedColor, onClick = { onStationTapped(TimeGroup.Leave) }) {
                    StationRow(
                        station = groupTwoStation,
                        modifier = Modifier.fillMaxWidth(),
                        showUpDown = false,
                        circleSize = Dimens.stationCircleSizeSmall,
                        circleFontSize = Dimens.stationCircleSmallFontSize,
                    )
                }
            }
        }
    }
}
