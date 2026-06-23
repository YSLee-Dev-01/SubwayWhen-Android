package com.yslee.subwaywhen.feature.detail.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yslee.subwaywhen.feature.detail.DetailScheduleItem
import com.yslee.subwaywhen.ui.common.MainBgCard
import com.yslee.subwaywhen.ui.common.subwayLineColor
import com.yslee.subwaywhen.ui.theme.Dimens
import com.yslee.subwaywhen.ui.theme.SubwayWhenTheme

@Composable
fun DetailScheduleSection(
    scheduleItems: List<DetailScheduleItem>,
    lineNumber: String,
    isUnowned: Boolean,
    scheduleError: Boolean,
    isScheduleLoading: Boolean,
    onScheduleMoreTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentAlpha by animateFloatAsState(
        targetValue = if (isScheduleLoading) 0f else 1f,
        animationSpec = tween(325),
        label = "contentAlpha",
    )

    val scheduleTitle = when {
        isScheduleLoading -> "📡 시간표를 가져오고 있어요."
        isUnowned -> "ℹ️ 시간표를 지원하지 않는 노선이에요."
        scheduleError || scheduleItems.isEmpty() -> "⚠️ 시간표를 불러올 수 없어요."
        else -> "${scheduleItems.first().destination}행 ${scheduleItems.first().timeLabel}"
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.paddingTB),
    ) {
        Text(
            text = "시간표",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

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
                        text = scheduleTitle,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                    )
                    if (!isScheduleLoading && !isUnowned && scheduleItems.isNotEmpty()) {
                        Text(
                            text = "···",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.clickable { onScheduleMoreTap() },
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .alpha(contentAlpha),
                    contentAlignment = Alignment.Center,
                ) {
                    when {
                        isScheduleLoading -> {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp),
                            )
                        }
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
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(scheduleItems.chunked(2)) { rowItems ->
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                    ) {
                                        rowItems.forEach { item ->
                                            Box(modifier = Modifier.weight(1f)) {
                                                ScheduleItemCell(item, lineNumber)
                                            }
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
}

@Composable
private fun ScheduleItemCell(item: DetailScheduleItem, lineNumber: String) {
    val bgColor = subwayLineColor(lineNumber) ?: MaterialTheme.colorScheme.primary
    val fastLabel = if (item.isFast) "(급)" else ""
    val title = "⏱️ $fastLabel${item.destination}행 ${item.timeLabel}"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(bgColor, RoundedCornerShape(Dimens.cornerRadius))
            .padding(start = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = Dimens.fontSizeSmall,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ScheduleEmptyText(message: String) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
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
            lineNumber = "03호선",
            isUnowned = false,
            scheduleError = false,
            isScheduleLoading = false,
            onScheduleMoreTap = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "DetailScheduleSection - Loading", showBackground = true)
@Composable
private fun DetailScheduleSectionLoadingPreview() {
    SubwayWhenTheme(darkTheme = false) {
        DetailScheduleSection(
            scheduleItems = emptyList(),
            lineNumber = "03호선",
            isUnowned = false,
            scheduleError = false,
            isScheduleLoading = true,
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
            lineNumber = "03호선",
            isUnowned = true,
            scheduleError = false,
            isScheduleLoading = false,
            onScheduleMoreTap = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
