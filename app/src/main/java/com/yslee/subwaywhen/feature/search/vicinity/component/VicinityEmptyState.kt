package com.yslee.subwaywhen.feature.search.vicinity.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yslee.subwaywhen.R
import com.yslee.subwaywhen.ui.theme.Dimens
import com.yslee.subwaywhen.ui.theme.SubwayWhenTheme

/**
 * iOS SearchVicinityView 빈 목록 상태 대응.
 * 가까운 역이 없을 때 안내 텍스트를 중앙에 표시한다.
 */
@Composable
fun VicinityEmptyState(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 15.dp),
    ) {
        Text(
            text = stringResource(R.string.vicinity_empty),
            fontSize = Dimens.fontSizeMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Preview(name = "VicinityEmptyState - Light", showBackground = true)
@Composable
private fun VicinityEmptyStateLightPreview() {
    SubwayWhenTheme(darkTheme = false) {
        VicinityEmptyState()
    }
}

@Preview(name = "VicinityEmptyState - Dark", showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun VicinityEmptyStateDarkPreview() {
    SubwayWhenTheme(darkTheme = true) {
        VicinityEmptyState()
    }
}
