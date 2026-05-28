package com.yslee.subwaywhen.feature.search.vicinity.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yslee.subwaywhen.data.remote.dto.vicinityStation.VicinityTransformData
import com.yslee.subwaywhen.ui.common.AnimatedTapBox
import com.yslee.subwaywhen.ui.common.AnimatedTapBoxAlignment
import com.yslee.subwaywhen.ui.common.StationLineCircle
import com.yslee.subwaywhen.ui.common.subwayLineColor
import com.yslee.subwaywhen.ui.common.subwayLineDisplayName
import com.yslee.subwaywhen.ui.theme.Dimens
import com.yslee.subwaywhen.ui.theme.SubwayWhenTheme

/**
 * iOS SearchVicinityView LazyHStack 미선택 항목 대응.
 * 역 선택 전 노선 원형 + 역명 버튼.
 */
@Composable
fun VicinityStationRow(
    station: VicinityTransformData,
    onClick: () -> Unit,
) {
    AnimatedTapBox(
        bgColor = Color.Transparent,
        pressedColor = Color.Gray.copy(alpha = 0.1f),
        alignment = AnimatedTapBoxAlignment.Center,
        horizontalPadding = 8.dp,
        onClick = onClick,
    ) {
        StationLineCircle(
            title = subwayLineDisplayName(station.lineColorName),
            lineColor = subwayLineColor(station.lineColorName),
            size = Dimens.vicinityStationCircleSize,
            isFilled = true,
            fontSize = Dimens.fontSizeSmall,
        )
    }
}

@Preview(name = "VicinityStationRow - Light", showBackground = true)
@Composable
private fun VicinityStationRowLightPreview() {
    SubwayWhenTheme(darkTheme = false) {
        VicinityStationRow(
            station = VicinityTransformData(id = "1", name = "강남", line = "2호선", distance = "150m"),
            onClick = {},
        )
    }
}

@Preview(name = "VicinityStationRow - Dark", showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun VicinityStationRowDarkPreview() {
    SubwayWhenTheme(darkTheme = true) {
        VicinityStationRow(
            station = VicinityTransformData(id = "1", name = "강남", line = "2호선", distance = "150m"),
            onClick = {},
        )
    }
}
