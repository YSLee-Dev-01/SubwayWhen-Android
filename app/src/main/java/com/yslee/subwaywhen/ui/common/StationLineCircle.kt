package com.yslee.subwaywhen.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.yslee.subwaywhen.ui.theme.Dimens
import com.yslee.subwaywhen.ui.theme.SubwayWhenTheme

/**
 * iOS StationTitleViewInSUI 대응.
 * 호선 컬러 원형 + 텍스트 컴포저블.
 *
 * @param title 원 안에 표시할 텍스트 (호선명 등)
 * @param lineColor 호선 컬러. null이면 Color.Gray 폴백
 * @param size 원의 지름
 * @param isFilled true이면 원 내부를 lineColor로 채움, false이면 테두리만 표시
 * @param fontSize 텍스트 크기
 */
@Composable
fun StationLineCircle(
    title: String,
    lineColor: Color?,
    size: Dp,
    isFilled: Boolean,
    fontSize: TextUnit = Dimens.fontSizeMedium,
) {
    val color = lineColor ?: Color.Gray
    val bgColor = if (isFilled) color else MaterialTheme.colorScheme.surface
    val textColor = if (isFilled) Color.White else MaterialTheme.colorScheme.onSurface

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(size)
            .background(color = bgColor, shape = CircleShape)
            .border(width = 1.dp, color = color, shape = CircleShape),
    ) {
        Text(
            text = title,
            color = textColor,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
    }
}

@Preview(name = "StationLineCircle - Light (filled)", showBackground = true)
@Composable
private fun StationLineCircleLightPreview() {
    SubwayWhenTheme(darkTheme = false) {
        StationLineCircle(
            title = "3",
            lineColor = Color(0xFFEF7C1C),
            size = 75.dp,
            isFilled = true,
        )
    }
}

@Preview(name = "StationLineCircle - Dark (stroke)", showBackground = true, backgroundColor = 0xFF222222)
@Composable
private fun StationLineCircleDarkPreview() {
    SubwayWhenTheme(darkTheme = true) {
        StationLineCircle(
            title = "교대",
            lineColor = Color(0xFFEF7C1C),
            size = 75.dp,
            isFilled = false,
        )
    }
}
