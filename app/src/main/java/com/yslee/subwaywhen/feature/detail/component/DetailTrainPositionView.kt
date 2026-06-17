package com.yslee.subwaywhen.feature.detail.component

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yslee.subwaywhen.ui.common.subwayLineColor
import com.yslee.subwaywhen.ui.theme.Dimens
import com.yslee.subwaywhen.ui.theme.SubwayWhenTheme

private fun statusCodeToPosition(code: String): Float = when (code) {
    "0", "4" -> 0.0f
    "1", "5" -> 0.5f
    else -> 1.0f
}

@Composable
fun DetailTrainPositionView(
    prevStationName: String,
    currentStationName: String,
    nextStationName: String,
    statusCode: String,
    isFast: Boolean,
    lineNumber: String,
    modifier: Modifier = Modifier,
) {
    val lineColor = subwayLineColor(lineNumber) ?: Color.Gray
    val targetPosition = statusCodeToPosition(statusCode)
    val animatedPosition by animateFloatAsState(
        targetValue = targetPosition,
        animationSpec = tween(durationMillis = Dimens.animationDurationMs),
        label = "trainPosition",
    )
    val prevAlpha by animateFloatAsState(
        targetValue = if (statusCode == "99") 1f else 0f,
        animationSpec = tween(durationMillis = Dimens.animationDurationMs),
        label = "prevAlpha",
    )

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth().height(73.dp),
    ) {
        val offsetX = with(LocalDensity.current) { (maxWidth.toPx() * animatedPosition).toDp() }

        // 호선색 트랙 바 (원형 수직 중심에 맞춤)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(5.dp)
                .align(Alignment.TopCenter)
                .offset(y = 27.dp)
                .background(lineColor, RoundedCornerShape(50)),
        )

        // 역 마커 Row (트랙 바보다 나중에 그려 위에 표시)
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
                StationMarkerColumn(name = prevStationName, lineColor = lineColor, align = Alignment.CenterHorizontally, alpha = prevAlpha)
            }

            Spacer(modifier = Modifier.weight(1f))

            if (nextStationName.isNotEmpty()) {
                StationMarkerColumn(name = nextStationName, lineColor = lineColor, align = Alignment.End)
            }
        }

        // 열차 이모지 오버레이 (트랙 바 중심 위에 위치)
        Text(
            text = if (isFast) "🚄" else "🚇",
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(
                    x = (offsetX - 12.dp).coerceAtLeast(0.dp),
                    y = 12.dp,
                ),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun StationMarkerColumn(
    name: String,
    lineColor: Color,
    align: Alignment.Horizontal,
    alpha: Float = 1f,
) {
    Column(
        modifier = Modifier.alpha(alpha),
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
            modifier = Modifier.padding(16.dp),
        )
    }
}
