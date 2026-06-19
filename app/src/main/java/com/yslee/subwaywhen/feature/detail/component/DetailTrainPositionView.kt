package com.yslee.subwaywhen.feature.detail.component

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yslee.subwaywhen.ui.common.subwayLineColor
import com.yslee.subwaywhen.ui.theme.Dimens
import com.yslee.subwaywhen.ui.theme.SubwayWhenTheme

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
    val lineColor = subwayLineColor(lineNumber) ?: Color.Gray
    val density = LocalDensity.current

    // 4a: 트랙 바 offset/scale — statusCode별 동적 조절 (iOS borderPostion / borderSize 포팅)
    val trackOffsetX by animateFloatAsState(
        targetValue = when {
            isLoading -> 0f
            statusCode == "99" -> 35f
            statusCode.toIntOrNull() != null -> 12f
            else -> 0f
        },
        animationSpec = tween(durationMillis = Dimens.animationDurationMs),
        label = "trackOffsetX",
    )
    val trackScaleX by animateFloatAsState(
        targetValue = if (!isLoading && statusCode == "99") 1.2f else 1.0f,
        animationSpec = tween(durationMillis = Dimens.animationDurationMs),
        label = "trackScaleX",
    )

    // 4b: back 마커 — 로딩 시 왼쪽(-100dp) 슬라이드 + alpha0; 완료 시 spring 복귀
    //      statusCode "99"일 때만 표시 (iOS backStationPostion 로직)
    val backMarkerAlpha by animateFloatAsState(
        targetValue = if (!isLoading && statusCode == "99") 1f else 0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMediumLow),
        label = "backMarkerAlpha",
    )
    val backMarkerOffsetX by animateFloatAsState(
        targetValue = if (isLoading) -100f else 0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMediumLow),
        label = "backMarkerOffsetX",
    )

    // 4b: next 마커 — 로딩 시 오른쪽(+100dp) 슬라이드 + alpha0; 완료 시 spring 복귀
    val nextMarkerAlpha by animateFloatAsState(
        targetValue = if (isLoading) 0f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMediumLow),
        label = "nextMarkerAlpha",
    )
    val nextMarkerOffsetX by animateFloatAsState(
        targetValue = if (isLoading) 100f else 0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMediumLow),
        label = "nextMarkerOffsetX",
    )

    // 4b: 열차 아이콘 — 로딩 시 fade out, 완료 후 700ms 동안 fade in
    val trainAlpha by animateFloatAsState(
        targetValue = if (isLoading) 0f else 1f,
        animationSpec = tween(durationMillis = 700),
        label = "trainAlpha",
    )

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth().height(73.dp),
    ) {
        val maxWidthPx = with(density) { maxWidth.toPx() }
        val trackStartPx = with(density) { 27.5.dp.toPx() }
        val trackEndPx = maxWidthPx - trackStartPx
        val trackWidthPx = trackEndPx - trackStartPx
        val iconHalfPx = with(density) { 12.dp.toPx() }

        val targetOffsetPx = when (statusCode) {
            "0" -> trackStartPx + with(density) { 17.5.dp.toPx() } - iconHalfPx
            "1" -> trackStartPx - iconHalfPx
            "2" -> trackStartPx - with(density) { 10.dp.toPx() } - iconHalfPx
            "3" -> trackStartPx + trackWidthPx * 0.5f - iconHalfPx
            "4" -> trackEndPx - with(density) { 20.dp.toPx() } - iconHalfPx
            else -> trackEndPx - iconHalfPx  // "5", "99"
        }.coerceAtLeast(0f)

        val animatedOffsetPx by animateFloatAsState(
            targetValue = targetOffsetPx,
            animationSpec = tween(durationMillis = Dimens.animationDurationMs),
            label = "trainPosition",
        )
        val animatedOffsetDp = with(density) { animatedOffsetPx.toDp() }

        // 트랙 바 — 좌측 원형 중심(27.5dp)에서 열차 아이콘 중심까지
        val trackWidthDp = maxOf(0.dp, animatedOffsetDp + 12.dp - 27.5.dp)
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 27.5.dp)
                .width(trackWidthDp)
                .height(5.dp)
                .offset(y = 27.dp)
                .graphicsLayer { scaleX = trackScaleX }
                .background(lineColor, RoundedCornerShape(50)),
        )

        // 역 마커 Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 22.dp),
            horizontalArrangement = Arrangement.spacedBy(30.dp),
            verticalAlignment = Alignment.Top,
        ) {
            StationMarkerColumn(name = currentStationName, lineColor = lineColor, align = Alignment.CenterHorizontally)
            if (prevStationName.isNotEmpty()) {
                StationMarkerColumn(
                    name = prevStationName,
                    lineColor = lineColor,
                    align = Alignment.CenterHorizontally,
                    alpha = backMarkerAlpha,
                    offsetX = backMarkerOffsetX,
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            if (nextStationName.isNotEmpty()) {
                StationMarkerColumn(
                    name = nextStationName,
                    lineColor = lineColor,
                    align = Alignment.End,
                    alpha = nextMarkerAlpha,
                    offsetX = nextMarkerOffsetX,
                )
            }
        }

        // 열차 아이콘 — 4b: trainAlpha로 fade
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = animatedOffsetDp, y = 12.dp)
                .alpha(trainAlpha),
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
                .background(Color.White, CircleShape)
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
