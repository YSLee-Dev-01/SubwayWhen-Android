package com.yslee.subwaywhen.feature.search.vicinity.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yslee.subwaywhen.ui.theme.SubwayWhenTheme

/**
 * iOS SearchVicinityView ProgressView 대응.
 * 위치 기반 역 로딩 중 상태를 표시한다.
 */
@Composable
fun VicinityLoading(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 30.dp),
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Preview(name = "VicinityLoading", showBackground = true)
@Composable
private fun VicinityLoadingPreview() {
    SubwayWhenTheme(darkTheme = false) {
        VicinityLoading()
    }
}
