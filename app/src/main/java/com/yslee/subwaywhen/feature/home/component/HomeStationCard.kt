package com.yslee.subwaywhen.feature.home.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yslee.subwaywhen.feature.home.HomeCellData
import com.yslee.subwaywhen.feature.home.HomeCellType
import com.yslee.subwaywhen.ui.common.StationLineCircle
import com.yslee.subwaywhen.ui.common.subwayLineColor
import com.yslee.subwaywhen.ui.common.subwayLineDisplayName
import com.yslee.subwaywhen.ui.theme.AppIconColor
import com.yslee.subwaywhen.ui.theme.Dimens
import com.yslee.subwaywhen.ui.theme.MainColorDark
import com.yslee.subwaywhen.ui.theme.MainColorLight
import com.yslee.subwaywhen.ui.theme.SubwayWhenTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * iOS MainTableViewArrivalCell 대응.
 * 역 1건의 실시간 / 시간표 / 로딩 카드.
 *
 * 레이아웃 (iOS 원본 기준):
 * [circle]  [역명 | 종착역행]
 *           [상태메시지]
 * [───────────────────── [🔄]]   ← 선이 버튼 수직 중앙 통과
 *                           [시간]  or [로딩]
 */
@Composable
fun HomeStationCard(
    cell: HomeCellData,
    onCardTap: () -> Unit,
    onScheduleTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bgColor = if (isSystemInDarkTheme()) MainColorDark else MainColorLight
    val pressedColor = if (isSystemInDarkTheme())
        MainColorDark.copy(alpha = 0.7f) else MainColorLight.copy(alpha = 0.7f)

    val lineColor = subwayLineColor(cell.line) ?: MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    val lineDisplayName = subwayLineDisplayName(cell.line)

    val statusText = buildString {
        if (cell.type == HomeCellType.Schedule) append("⏱️")
        append(cell.useFast)
        append(cell.stateMSG)
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scope = rememberCoroutineScope()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) Dimens.animationScale else 1f,
        animationSpec = tween(durationMillis = Dimens.animationDurationMs),
        label = "stationCardScale",
    )
    val backgroundColor by animateColorAsState(
        targetValue = if (isPressed) pressedColor else bgColor,
        animationSpec = tween(durationMillis = Dimens.animationDurationMs),
        label = "stationCardBg",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .background(color = backgroundColor, shape = RoundedCornerShape(Dimens.cornerRadius))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { scope.launch { delay(100L); onCardTap() } },
            )
            .padding(horizontal = 15.dp, vertical = 24.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // 상단: 호선 원형 뱃지 + 역명/상태
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                StationLineCircle(
                    title = lineDisplayName,
                    lineColor = lineColor,
                    size = 60.dp,
                    isFilled = true,
                    fontSize = Dimens.fontSizeSmall,
                )

                Spacer(modifier = Modifier.width(15.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (cell.lastStation.isNotEmpty()) "${cell.stationName} | ${cell.lastStation}"
                               else cell.stationName,
                        fontSize = Dimens.fontSizeSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (statusText.isNotEmpty()) {
                        Text(
                            text = statusText,
                            fontSize = Dimens.fontSizeMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(15.dp))

            // 중단: 구분선(좌) + 시간표 버튼(우)
            // border.top = changeBtn.bottom - 15 (= changeBtn.center.y) → 선이 버튼 중앙 통과
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp),
            ) {
                // 구분선 — circle.leading 에서 button.leading 까지 (버튼 80dp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 80.dp)
                        .height(1.dp)
                        .background(lineColor.copy(alpha = 0.4f))
                        .align(Alignment.CenterStart),
                )
                // 시간표 버튼 — 우측 끝, 호선 색상 배경
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(30.dp)
                        .background(lineColor, RoundedCornerShape(Dimens.cornerRadius))
                        .align(Alignment.CenterEnd),
                    contentAlignment = Alignment.Center,
                ) {
                    IconButton(
                        onClick = onScheduleTap,
                        modifier = Modifier.size(30.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "시간표",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(15.dp))

            // 하단: 도착 시간 or 로딩 인디케이터 (우측 정렬)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                if (cell.type == HomeCellType.Loading) {
                    CircularProgressIndicator(
                        color = AppIconColor,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(
                        text = cell.useTime,
                        fontSize = Dimens.fontSizeLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

private fun sampleCell(type: HomeCellType) = HomeCellData(
    stationIndex = 0, type = type,
    stationName = "강남", updnLine = "상행",
    lastStation = "성수", exceptionLastStation = "",
    stateMSG = "잠실나루 방면", arrivalTime = "120",
    subPrevious = "3분", code = "0",
    isFast = "급행", line = "2호선",
    lineCode = "1002", korailCode = "",
    stationCode = "222",
)

@Preview(name = "HomeStationCard - Real - Light", showBackground = true)
@Composable
private fun HomeStationCardRealLightPreview() {
    SubwayWhenTheme(darkTheme = false) {
        HomeStationCard(
            cell = sampleCell(HomeCellType.Real),
            onCardTap = {}, onScheduleTap = {},
            modifier = Modifier.padding(Dimens.paddingLR),
        )
    }
}

@Preview(name = "HomeStationCard - Loading - Light", showBackground = true)
@Composable
private fun HomeStationCardLoadingLightPreview() {
    SubwayWhenTheme(darkTheme = false) {
        HomeStationCard(
            cell = sampleCell(HomeCellType.Loading),
            onCardTap = {}, onScheduleTap = {},
            modifier = Modifier.padding(Dimens.paddingLR),
        )
    }
}

@Preview(name = "HomeStationCard - Schedule - Light", showBackground = true)
@Composable
private fun HomeStationCardScheduleLightPreview() {
    SubwayWhenTheme(darkTheme = false) {
        HomeStationCard(
            cell = sampleCell(HomeCellType.Schedule).copy(subPrevious = "10:30"),
            onCardTap = {}, onScheduleTap = {},
            modifier = Modifier.padding(Dimens.paddingLR),
        )
    }
}

@Preview(name = "HomeStationCard - Real - Dark", showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun HomeStationCardRealDarkPreview() {
    SubwayWhenTheme(darkTheme = true) {
        HomeStationCard(
            cell = sampleCell(HomeCellType.Real),
            onCardTap = {}, onScheduleTap = {},
            modifier = Modifier.padding(Dimens.paddingLR),
        )
    }
}
