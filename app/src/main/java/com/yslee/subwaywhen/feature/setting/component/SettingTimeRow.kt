package com.yslee.subwaywhen.feature.setting.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yslee.subwaywhen.feature.setting.TimeGroup
import com.yslee.subwaywhen.ui.common.AnimatedTapBox
import com.yslee.subwaywhen.ui.common.AnimatedTapBoxAlignment
import com.yslee.subwaywhen.ui.common.MainBgCard
import com.yslee.subwaywhen.ui.common.PrimaryButton
import com.yslee.subwaywhen.ui.theme.AppIconColor
import com.yslee.subwaywhen.ui.theme.Dimens
import com.yslee.subwaywhen.ui.theme.MainColorDark
import com.yslee.subwaywhen.ui.theme.MainColorLight
import com.yslee.subwaywhen.ui.theme.SubwayWhenTheme

@Composable
fun SettingTimeRow(
    workTime: Int,
    leaveTime: Int,
    expandedGroup: TimeGroup?,
    workHasAlert: Boolean,
    leaveHasAlert: Boolean,
    onGroupTapped: (TimeGroup) -> Unit,
    onSave: (TimeGroup, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) MainColorDark else MainColorLight
    val pressedColor = if (isDark) MainColorDark.copy(alpha = 0.7f) else MainColorLight.copy(alpha = 0.7f)

    val initialTime = when (expandedGroup) {
        TimeGroup.Work -> workTime
        TimeGroup.Leave -> leaveTime
        null -> 0
    }
    var stepperValue by remember(expandedGroup, initialTime) { mutableIntStateOf(initialTime) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            AnimatedTapBox(
                bgColor = bgColor,
                pressedColor = pressedColor,
                alignment = AnimatedTapBoxAlignment.Fill,
                verticalPadding = 0.dp,
                onClick = { onGroupTapped(TimeGroup.Work) },
                modifier = Modifier.weight(1f),
            ) {
                TimeGroupButton(
                    label = "출근",
                    time = if (expandedGroup == TimeGroup.Work) stepperValue else workTime,
                    hasAlert = workHasAlert,
                    modifier = Modifier.padding(horizontal = Dimens.paddingInner, vertical = 20.dp),
                )
            }
            AnimatedTapBox(
                bgColor = bgColor,
                pressedColor = pressedColor,
                alignment = AnimatedTapBoxAlignment.Fill,
                verticalPadding = 0.dp,
                onClick = { onGroupTapped(TimeGroup.Leave) },
                modifier = Modifier.weight(1f),
            ) {
                TimeGroupButton(
                    label = "퇴근",
                    time = if (expandedGroup == TimeGroup.Leave) stepperValue else leaveTime,
                    hasAlert = leaveHasAlert,
                    modifier = Modifier.padding(horizontal = Dimens.paddingInner, vertical = 20.dp),
                )
            }
        }

        AnimatedVisibility(
            visible = expandedGroup != null,
            enter = expandVertically(animationSpec = tween(Dimens.animationDurationMs)),
            exit = shrinkVertically(animationSpec = tween(Dimens.animationDurationMs)),
        ) {
            if (expandedGroup != null) {
                MainBgCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Dimens.paddingInner, vertical = 12.dp),
                    ) {
                        if (expandedGroup == TimeGroup.Work) {
                            StepperControl(value = stepperValue, onValueChange = { stepperValue = it })
                            Spacer(Modifier.weight(1f))
                            PrimaryButton(
                                text = "저장",
                                containerColor = AppIconColor,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                onClick = { onSave(expandedGroup, stepperValue) },
                                modifier = Modifier.width(80.dp),
                            )
                        } else {
                            PrimaryButton(
                                text = "저장",
                                containerColor = AppIconColor,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                onClick = { onSave(expandedGroup, stepperValue) },
                                modifier = Modifier.width(80.dp),
                            )
                            Spacer(Modifier.weight(1f))
                            StepperControl(value = stepperValue, onValueChange = { stepperValue = it })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeGroupButton(label: String, time: Int, hasAlert: Boolean, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = Dimens.fontSizeMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            ),
            modifier = Modifier.padding(bottom = Dimens.paddingTB + 5.dp),
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = if (time == 0) "-" else "${time}시",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = Dimens.fontSizeBigTitle,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                ),
                modifier = Modifier.padding(bottom = Dimens.paddingTB + 5.dp),
            )
            if (hasAlert) {
                Spacer(Modifier.width(4.dp))
                if (time == 0) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .padding(bottom = Dimens.paddingTB + 5.dp)
                            .size(15.dp),
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .padding(bottom = Dimens.paddingTB + 5.dp)
                            .size(8.dp)
                            .background(AppIconColor, CircleShape),
                    )
                }
            }
        }
    }
}

@Composable
private fun StepperControl(value: Int, onValueChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { if (value > 0) onValueChange(value - 1) }) {
            Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = "감소",
                tint = AppIconColor,
            )
        }
        IconButton(onClick = { if (value < 23) onValueChange(value + 1) }) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "증가",
                tint = AppIconColor,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingTimeRowPreview() {
    SubwayWhenTheme {
        SettingTimeRow(
            workTime = 9,
            leaveTime = 0,
            expandedGroup = TimeGroup.Work,
            workHasAlert = true,
            leaveHasAlert = true,
            onGroupTapped = {},
            onSave = { _, _ -> },
        )
    }
}
