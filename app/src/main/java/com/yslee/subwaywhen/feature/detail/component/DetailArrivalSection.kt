package com.yslee.subwaywhen.feature.detail.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yslee.subwaywhen.feature.detail.DetailArrivalItem
import com.yslee.subwaywhen.ui.common.MainBgCard
import com.yslee.subwaywhen.ui.common.subwayLineColor
import com.yslee.subwaywhen.ui.theme.AppIconColor
import com.yslee.subwaywhen.ui.theme.Dimens
import com.yslee.subwaywhen.ui.theme.SubwayWhenTheme

@Composable
fun DetailArrivalSection(
    exceptionLastStation: String,
    firstArrival: DetailArrivalItem?,
    secondArrival: DetailArrivalItem?,
    arrivalError: Boolean,
    lineNumber: String,
    timerCount: Int,
    isRefreshCooldown: Boolean,
    isArrivalLoading: Boolean,
    isAutoReloadEnabled: Boolean,
    onRealtimeTap: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val titleText = when {
        isArrivalLoading -> "📡 열차 정보를 가져오고 있어요."
        arrivalError || firstArrival == null -> "⚠️ 실시간 정보가 없어요."
        firstArrival.subPrevious.isEmpty() -> "⚠️ 실시간 정보가 없어요."
        else -> firstArrival.subPrevious
    }
    val contentAlpha by animateFloatAsState(
        targetValue = if (isArrivalLoading) 0f else 1f,
        animationSpec = tween(325),
        label = "contentAlpha",
    )

    MainBgCard(modifier = modifier.fillMaxWidth()) {
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
                        text = titleText,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f).alpha(contentAlpha),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        var rotationOffset by remember { mutableFloatStateOf(0f) }
                        val rotation by animateFloatAsState(
                            targetValue = rotationOffset,
                            animationSpec = tween(500),
                            label = "refresh_rotation",
                        )
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .alpha(if (isRefreshCooldown) 0.4f else 1f)
                                .clickable {
                                    if (!isRefreshCooldown) {
                                        rotationOffset += 360f
                                        onRefresh()
                                    }
                                },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .size(22.dp)
                                    .rotate(rotation),
                            )
                            if (isAutoReloadEnabled) {
                                Text(
                                    text = "$timerCount",
                                    fontSize = Dimens.fontSizeSuperSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        Text(
                            text = "···",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.clickable { onRealtimeTap() },
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .alpha(contentAlpha),
                    contentAlignment = Alignment.Center,
                ) {
                    when {
                        isArrivalLoading -> {
                            CircularProgressIndicator(
                                color = AppIconColor,
                                modifier = Modifier.size(28.dp),
                            )
                        }
                        arrivalError -> {
                            ArrivalEmptyText("실시간 정보를 불러오지 못했어요.")
                        }
                        firstArrival == null && secondArrival == null -> {
                            ArrivalEmptyText("현재 운행 중인 열차가 없어요.")
                        }
                        else -> {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                firstArrival?.let { ArrivalBubble(it, isFirst = true, lineNumber = lineNumber) }
                                if (exceptionLastStation.isNotEmpty()) {
                                    ArrivalBubble(
                                        item = null,
                                        isFirst = false,
                                        lineNumber = lineNumber,
                                        bgColorOverride = Color.Gray,
                                        overrideText = "⛔ 제외 행을 설정하면\n두 번째 열차를 볼 수 없어요.",
                                    )
                                } else {
                                    secondArrival?.let { ArrivalBubble(it, isFirst = false, lineNumber = lineNumber) }
                                }
                            }
                        }
                    }
                }
            }
        }
}

@Composable
private fun ArrivalBubble(
    item: DetailArrivalItem?,
    isFirst: Boolean,
    lineNumber: String,
    bgColorOverride: Color? = null,
    overrideText: String? = null,
) {
    val bgColor = bgColorOverride ?: (subwayLineColor(lineNumber) ?: MaterialTheme.colorScheme.primary)
    val bubbleText = overrideText ?: run {
        if (item != null && item.subPrevious.isNotEmpty() && item.statusCode.isNotEmpty()) {
            "🚇 ${item.trainNo} 열차(${if (item.isFast) "(급)" else ""}${item.destination}행)\n${item.subPrevious}"
        } else {
            "⚠️ 실시간 정보없음"
        }
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isFirst) Arrangement.Start else Arrangement.End,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.65f)
                .background(color = bgColor, shape = RoundedCornerShape(15.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
            Text(
                text = bubbleText,
                color = Color.White,
                fontSize = Dimens.fontSizeSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ArrivalEmptyText(message: String) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Preview(name = "DetailArrivalSection - Light", showBackground = true)
@Composable
private fun DetailArrivalSectionLightPreview() {
    SubwayWhenTheme(darkTheme = false) {
        DetailArrivalSection(
            exceptionLastStation = "",
            firstArrival = DetailArrivalItem("3분", "전역 출발", "전역 출발", "구파발", "1234", false, "3"),
            secondArrival = DetailArrivalItem("7분", "전역 진입", "전역 진입", "구파발", "5678", true, "4"),
            arrivalError = false,
            lineNumber = "03호선",
            timerCount = 10,
            isRefreshCooldown = false,
            isArrivalLoading = false,
            isAutoReloadEnabled = true,
            onRealtimeTap = {},
            onRefresh = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "DetailArrivalSection - Loading", showBackground = true)
@Composable
private fun DetailArrivalSectionLoadingPreview() {
    SubwayWhenTheme(darkTheme = false) {
        DetailArrivalSection(
            exceptionLastStation = "",
            firstArrival = null,
            secondArrival = null,
            arrivalError = false,
            lineNumber = "03호선",
            timerCount = 15,
            isRefreshCooldown = false,
            isArrivalLoading = true,
            isAutoReloadEnabled = true,
            onRealtimeTap = {},
            onRefresh = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "DetailArrivalSection - Dark", showBackground = true, backgroundColor = 0xFF222222)
@Composable
private fun DetailArrivalSectionDarkPreview() {
    SubwayWhenTheme(darkTheme = true) {
        DetailArrivalSection(
            exceptionLastStation = "노원",
            firstArrival = DetailArrivalItem("1분", "도착", "노원역 도착", "노원", "9999", false, "1"),
            secondArrival = null,
            arrivalError = false,
            lineNumber = "07호선",
            timerCount = 5,
            isRefreshCooldown = true,
            isArrivalLoading = false,
            isAutoReloadEnabled = false,
            onRealtimeTap = {},
            onRefresh = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
