package com.yslee.subwaywhen.feature.home.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yslee.subwaywhen.ui.theme.Dimens
import com.yslee.subwaywhen.ui.theme.SubwayWhenTheme

/**
 * iOS MainVC 메인 타이틀 영역 대응.
 * 요일별 타이틀 메시지를 큰 볼드 텍스트 2줄로 표시한다.
 */
@Composable
fun HomeMainTitleHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        modifier = modifier,
        style = MaterialTheme.typography.bodyLarge.copy(
            fontSize = Dimens.fontSizeBigTitle,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = Dimens.fontSizeBigTitle * 1.4f,
            color = MaterialTheme.colorScheme.onBackground,
        ),
    )
}

@Preview(name = "Light", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun HomeMainTitleHeaderLightPreview() {
    SubwayWhenTheme(darkTheme = false) {
        HomeMainTitleHeader(
            title = "월요일,\n한 주도 화이팅해봐요!",
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Dark", showBackground = true, backgroundColor = 0xFF1C1C1E)
@Composable
private fun HomeMainTitleHeaderDarkPreview() {
    SubwayWhenTheme(darkTheme = true) {
        HomeMainTitleHeader(
            title = "월요일,\n한 주도 화이팅해봐요!",
            modifier = Modifier.padding(16.dp),
        )
    }
}
