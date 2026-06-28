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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
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
    val fallbackColor = MaterialTheme.colorScheme.onSurfaceVariant
    val lineColor = subwayLineColor(lineNumber) ?: fallbackColor

    var isTrainVisible by remember { mutableStateOf(false) }
    val trainAlpha by animateFloatAsState(
        targetValue = if (isTrainVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 325),
        label = "trainFade",
    )

    LaunchedEffect(isLoading) {
        if (isLoading) {
            isTrainVisible = false
        } else {
            if (statusCode.toIntOrNull() != null) isTrainVisible = true
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxWidth().height(73.dp)) {
        val trackWidth = maxWidth - Dimens.paddingLR * 2
        val iconOffsetX = when (statusCode.toIntOrNull()) {
            0    -> Dimens.paddingLR + 5.dp
            1    -> Dimens.paddingLR
            2    -> maxOf(0.dp, Dimens.paddingLR - 10.dp)
            3    -> Dimens.paddingLR + trackWidth / 2
            4    -> Dimens.paddingLR + trackWidth - 10.dp
            else -> Dimens.paddingLR + trackWidth - 20.dp
        }
        // 트랙 바 (정적)
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .padding(start = Dimens.paddingLR + 7.5.dp, end = Dimens.paddingLR + 7.5.dp)
                .height(5.dp)
                .offset(y = 27.dp)
                .background(lineColor, RoundedCornerShape(50)),
        )

        // 역 마커 Row (정적)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.paddingLR)
                .padding(top = 22.dp),
            horizontalArrangement = Arrangement.spacedBy(30.dp),
            verticalAlignment = Alignment.Top,
        ) {
            StationMarkerColumn(name = currentStationName, lineColor = lineColor, align = Alignment.CenterHorizontally, isBold = true)
            val showSecondMarker = statusCode.toIntOrNull()?.let { it >= 6 } ?: true
            if (prevStationName.isNotEmpty() && showSecondMarker) {
                StationMarkerColumn(name = prevStationName, lineColor = lineColor, align = Alignment.CenterHorizontally)
            }
            Spacer(modifier = Modifier.weight(1f))
            val rightName = if (statusCode in listOf("0", "1", "2")) prevStationName else nextStationName
            if (rightName.isNotEmpty()) {
                StationMarkerColumn(name = rightName, lineColor = lineColor, align = Alignment.End)
            }
        }

        // 열차 아이콘 (페이드만)
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = iconOffsetX, y = 12.dp)
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
    isBold: Boolean = false,
) {
    Column(
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
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
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
