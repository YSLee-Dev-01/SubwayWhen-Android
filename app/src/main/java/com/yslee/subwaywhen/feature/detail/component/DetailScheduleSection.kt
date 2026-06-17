package com.yslee.subwaywhen.feature.detail.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yslee.subwaywhen.feature.detail.DetailScheduleItem
import com.yslee.subwaywhen.ui.common.AnimatedTapBox
import com.yslee.subwaywhen.ui.common.AnimatedTapBoxAlignment
import com.yslee.subwaywhen.ui.common.MainBgCard
import com.yslee.subwaywhen.ui.theme.Dimens
import com.yslee.subwaywhen.ui.theme.SubwayWhenTheme

@Composable
fun DetailScheduleSection(
    scheduleItems: List<DetailScheduleItem>,
    isUnowned: Boolean,
    scheduleError: Boolean,
    onScheduleMoreTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.paddingTB),
    ) {
        MainBgCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(Dimens.paddingInner),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "시간표",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (!isUnowned && scheduleItems.isNotEmpty()) {
                        Text(
                            text = "···",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.clickable { onScheduleMoreTap() },
                        )
                    }
                }

                when {
                    isUnowned -> {
                        ScheduleEmptyText("이 노선은 시간표 정보를 제공하지 않아요.")
                    }
                    scheduleError -> {
                        ScheduleEmptyText("시간표 정보를 불러오지 못했어요.")
                    }
                    scheduleItems.isEmpty() -> {
                        ScheduleEmptyText("운행 중인 열차가 없어요.")
                    }
                    else -> {
                        val rows = scheduleItems.chunked(2)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            rows.forEach { rowItems ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    rowItems.forEach { item ->
                                        Box(modifier = Modifier.weight(1f)) { ScheduleItemCell(item) }
                                    }
                                    if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleItemCell(item: DetailScheduleItem) {
    AnimatedTapBox(
        bgColor = MaterialTheme.colorScheme.surfaceVariant,
        pressedColor = MaterialTheme.colorScheme.outline,
        alignment = AnimatedTapBoxAlignment.Fill,
        horizontalPadding = 8.dp,
        verticalPadding = 6.dp,
        onClick = {},
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            val fastLabel = if (item.isFast) " (급)" else ""
            Text(
                text = "${item.minutesLater}분 후",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "${item.timeLabel} ${item.destination}행$fastLabel",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun ScheduleEmptyText(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(name = "DetailScheduleSection - Light", showBackground = true)
@Composable
private fun DetailScheduleSectionLightPreview() {
    SubwayWhenTheme(darkTheme = false) {
        DetailScheduleSection(
            scheduleItems = listOf(
                DetailScheduleItem(3, "09:15", "구파발", false),
                DetailScheduleItem(8, "09:20", "구파발", true),
                DetailScheduleItem(13, "09:25", "연신내", false),
            ),
            isUnowned = false,
            scheduleError = false,
            onScheduleMoreTap = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "DetailScheduleSection - Unowned", showBackground = true)
@Composable
private fun DetailScheduleSectionUnownedPreview() {
    SubwayWhenTheme(darkTheme = false) {
        DetailScheduleSection(
            scheduleItems = emptyList(),
            isUnowned = true,
            scheduleError = false,
            onScheduleMoreTap = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
