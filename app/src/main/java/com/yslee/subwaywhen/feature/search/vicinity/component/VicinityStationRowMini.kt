package com.yslee.subwaywhen.feature.search.vicinity.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yslee.subwaywhen.data.remote.dto.vicinityStation.VicinityTransformData
import com.yslee.subwaywhen.ui.common.AnimatedTapBox
import com.yslee.subwaywhen.ui.common.AnimatedTapBoxAlignment
import com.yslee.subwaywhen.ui.common.subwayLineColor
import com.yslee.subwaywhen.ui.theme.Dimens
import com.yslee.subwaywhen.ui.theme.SubwayWhenTheme

/**
 * iOS SearchVicinityView LazyHStack 비선택 축소 항목 대응.
 * 역 선택 후 선택되지 않은 항목에 표시하는 컴팩트 뷰.
 * 노선 색상 라운드 바 + 역명 텍스트를 세로로 배치한다.
 */
@Composable
fun VicinityStationRowMini(
    station: VicinityTransformData,
    onClick: () -> Unit,
) {
    val lineColor = subwayLineColor(station.lineColorName) ?: Color.Gray

    AnimatedTapBox(
        bgColor = Color.Transparent,
        pressedColor = Color.Gray.copy(alpha = 0.1f),
        alignment = AnimatedTapBoxAlignment.Center,
        horizontalPadding = 8.dp,
        onClick = onClick,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(
                modifier = Modifier
                    .width(Dimens.vicinityMiniBarWidth)
                    .height(Dimens.vicinityMiniBarHeight)
                    .background(
                        color = lineColor,
                        shape = RoundedCornerShape(3.dp),
                    ),
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = station.name,
                fontSize = Dimens.fontSizeSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Preview(name = "VicinityStationRowMini - Light", showBackground = true)
@Composable
private fun VicinityStationRowMiniLightPreview() {
    SubwayWhenTheme(darkTheme = false) {
        VicinityStationRowMini(
            station = VicinityTransformData(id = "1", name = "강남", line = "2호선", distance = "150m"),
            onClick = {},
        )
    }
}

@Preview(name = "VicinityStationRowMini - Dark", showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun VicinityStationRowMiniDarkPreview() {
    SubwayWhenTheme(darkTheme = true) {
        VicinityStationRowMini(
            station = VicinityTransformData(id = "1", name = "강남", line = "2호선", distance = "150m"),
            onClick = {},
        )
    }
}
