package com.yslee.subwaywhen.feature.detail.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yslee.subwaywhen.ui.common.subwayLineColor
import com.yslee.subwaywhen.ui.theme.Dimens
import com.yslee.subwaywhen.ui.theme.SubwayWhenTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DetailTrainPositionView(
    prevStationName: String,
    currentStationName: String,
    nextStationName: String,
    statusCode: String,
    isFast: Boolean,
    lineNumber: String,
    trainIcon: String,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val fallbackColor = MaterialTheme.colorScheme.onSurfaceVariant
    val lineColor = subwayLineColor(lineNumber) ?: fallbackColor
    val density = LocalDensity.current

    // 마커 애니메이션 (iOS backStationPostion, nextStationPostion)
    val backOffsetX = remember { Animatable(-100f) }
    val backAlpha = remember { Animatable(0f) }
    val nextOffsetX = remember { Animatable(100f) }
    val nextAlpha = remember { Animatable(0f) }

    // 열차 아이콘: 700ms 딜레이 후 표시 (iOS asyncAfter 0.7s 패턴)
    var isTrainVisible by remember { mutableStateOf(false) }

    // 열차 위치: 마지막 로드 완료 시 statusCode 기준 (iOS trainPostion)
    var displayedStatusCode by remember { mutableStateOf(statusCode) }
    // iOS trainPostion 기준: 초기 0에서 목표 위치로 easeInOut(0.4s)
    var trainTargetFraction by remember { mutableFloatStateOf(0f) }
    val animatedTrainFraction by animateFloatAsState(
        targetValue = trainTargetFraction,
        animationSpec = tween(durationMillis = 400),
        label = "trainPosition",
    )

    // 트랙 바: iOS borderPostion(offset) + borderSize(scale) 재현
    val trackOffset = remember { Animatable(0f) }
    val trackScale = remember { Animatable(1f) }

    LaunchedEffect(isLoading) {
        if (isLoading) {
            // 로딩 시작: 마커 slide out + train 숨김
            isTrainVisible = false
            launch { nextOffsetX.animateTo(100f, tween(Dimens.animationDurationMs)) }
            launch { nextAlpha.animateTo(0f, tween(Dimens.animationDurationMs)) }
            launch { backOffsetX.animateTo(-100f, tween(Dimens.animationDurationMs)) }
            launch { backAlpha.animateTo(0f, tween(Dimens.animationDurationMs)) }
        } else {
            // 로딩 완료
            displayedStatusCode = statusCode

            // 마커 초기 위치 snap
            nextOffsetX.snapTo(100f)
            nextAlpha.snapTo(0f)
            backOffsetX.snapTo(-100f)
            backAlpha.snapTo(0f)

            // 트랙 바 초기값 (iOS: code "99" → borderPostion=35/borderSize=1.2)
            if (statusCode == "99") {
                trackOffset.snapTo(35f)
                trackScale.snapTo(1.2f)
            } else {
                trackOffset.snapTo(12f)
                trackScale.snapTo(1f)
            }

            // 400ms 후 spring으로 마커 복귀 + 트랙 바 애니메이션
            delay(400)

            launch {
                nextOffsetX.animateTo(0f, spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMediumLow))
            }
            launch { nextAlpha.animateTo(1f, tween(300)) }
            launch {
                val targetBack = if (statusCode == "99") 1f else 0f
                backAlpha.animateTo(targetBack, tween(300))
            }
            if (statusCode == "99") {
                launch {
                    backOffsetX.animateTo(0f, spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMediumLow))
                }
                // 99: 35/1.2 → 0/1.0 with easeInOut
                launch { trackOffset.animateTo(0f, tween(700)) }
                launch { trackScale.animateTo(1f, tween(700)) }
            } else {
                // 비99: 12 → 35/1.2 → (후 0.7s) 0/1.0
                launch {
                    trackOffset.animateTo(35f, tween(700))
                    trackScale.animateTo(1.2f, tween(700))
                }
            }

            // 700ms 총 딜레이 후 열차 표시 (400ms 후 추가 300ms)
            delay(300)

            if (statusCode.toIntOrNull() != null) {
                trainTargetFraction = 1f
                isTrainVisible = true
            }

            // 비99 트랙 바 리셋
            if (statusCode != "99") {
                trackOffset.snapTo(0f)
                trackScale.snapTo(1f)
            }
        }
    }

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth().height(73.dp),
    ) {
        val maxWidthPx = with(density) { maxWidth.toPx() }
        val trackStartPx = with(density) { 27.5.dp.toPx() }
        val trackEndPx = maxWidthPx - trackStartPx
        val trackWidthPx = trackEndPx - trackStartPx
        val iconHalfPx = with(density) { 12.dp.toPx() }

        // iOS trainIconMoveValue 매핑 → Compose 좌측 기준 오프셋
        // iOS: 열차가 우측(position=0)에서 목표 위치로 easeInOut(0.4s) 이동
        val targetOffsetPx = trainPositionPx(displayedStatusCode, trackStartPx, trackEndPx, trackWidthPx, iconHalfPx)
        val animatedOffsetPx = trackEndPx + (targetOffsetPx - trackEndPx) * animatedTrainFraction
        val animatedOffsetDp = with(density) { animatedOffsetPx.toDp() }

        // 트랙 바 (iOS: full-width bar + offset + scale)
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .padding(horizontal = Dimens.paddingLR)
                .height(5.dp)
                .offset(x = trackOffset.value.dp, y = 27.dp)
                .scale(scaleX = trackScale.value, scaleY = 1f)
                .background(lineColor, RoundedCornerShape(50)),
        )

        // 역 마커 Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.paddingLR)
                .padding(top = 22.dp),
            horizontalArrangement = Arrangement.spacedBy(30.dp),
            verticalAlignment = Alignment.Top,
        ) {
            StationMarkerColumn(
                name = currentStationName,
                lineColor = lineColor,
                align = Alignment.CenterHorizontally,
            )
            if (prevStationName.isNotEmpty()) {
                StationMarkerColumn(
                    name = prevStationName,
                    lineColor = lineColor,
                    align = Alignment.CenterHorizontally,
                    alpha = backAlpha.value,
                    offsetX = backOffsetX.value,
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            val isArrivingStatus = displayedStatusCode == "0" || displayedStatusCode == "1" || displayedStatusCode == "2"
            val rightMarkerName = if (isArrivingStatus) prevStationName else nextStationName
            if (rightMarkerName.isNotEmpty()) {
                StationMarkerColumn(
                    name = rightMarkerName,
                    lineColor = lineColor,
                    align = Alignment.End,
                    alpha = nextAlpha.value,
                    offsetX = nextOffsetX.value,
                )
            }
        }

        // 열차 아이콘 (700ms 후 표시 + easeInOut 400ms 이동)
        if (isTrainVisible) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = animatedOffsetDp, y = 12.dp),
            ) {
                Text(
                    text = trainIcon,
                    style = MaterialTheme.typography.bodyLarge,
                )
                if (isFast) {
                    Text(
                        text = "💨",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 8.dp, y = 4.dp),
                    )
                }
            }
        }
    }
}

/** iOS trainIconMoveValue + Compose 좌표계 변환 결과 (px) */
private fun trainPositionPx(
    statusCode: String,
    trackStartPx: Float,
    trackEndPx: Float,
    trackWidthPx: Float,
    iconHalfPx: Float,
): Float = when (statusCode) {
    "0" -> trackStartPx + 17.5f * (trackWidthPx / 300f) - iconHalfPx
    "1" -> trackStartPx - iconHalfPx
    "2" -> trackStartPx - 10f - iconHalfPx
    "3" -> trackStartPx + trackWidthPx * 0.5f - iconHalfPx
    "4" -> trackEndPx - 20f - iconHalfPx
    else -> trackEndPx - iconHalfPx  // "5", "99"
}.coerceAtLeast(-iconHalfPx)

@Composable
private fun StationMarkerColumn(
    name: String,
    lineColor: Color,
    align: Alignment.Horizontal,
    alpha: Float = 1f,
    offsetX: Float = 0f,
) {
    Column(
        modifier = Modifier
            .alpha(alpha)
            .offset(x = offsetX.dp),
        horizontalAlignment = align,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(15.dp)
                .background(MaterialTheme.colorScheme.surface, CircleShape)
                .border(2.dp, lineColor, CircleShape),
        )
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

@Preview(name = "DetailTrainPositionView - Light", showBackground = true)
@Composable
private fun DetailTrainPositionViewLightPreview() {
    SubwayWhenTheme(darkTheme = false) {
        DetailTrainPositionView(
            prevStationName = "연신내",
            currentStationName = "불광",
            nextStationName = "독바위",
            statusCode = "3",
            isFast = false,
            lineNumber = "06호선",
            trainIcon = "🚃",
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "DetailTrainPositionView - Dark", showBackground = true, backgroundColor = 0xFF222222)
@Composable
private fun DetailTrainPositionViewDarkPreview() {
    SubwayWhenTheme(darkTheme = true) {
        DetailTrainPositionView(
            prevStationName = "연신내",
            currentStationName = "불광",
            nextStationName = "독바위",
            statusCode = "1",
            isFast = true,
            lineNumber = "06호선",
            trainIcon = "🚃",
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "DetailTrainPositionView - Arriving (statusCode 1)", showBackground = true)
@Composable
private fun DetailTrainPositionViewArrivingPreview() {
    SubwayWhenTheme(darkTheme = false) {
        DetailTrainPositionView(
            prevStationName = "연신내",
            currentStationName = "불광",
            nextStationName = "독바위",
            statusCode = "1",
            isFast = false,
            lineNumber = "06호선",
            trainIcon = "🚃",
            modifier = Modifier.padding(16.dp),
        )
    }
}
