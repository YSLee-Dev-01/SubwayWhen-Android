package com.yslee.subwaywhen.feature.setting.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.yslee.subwaywhen.ui.theme.Dimens
import com.yslee.subwaywhen.ui.theme.SubwayWhenTheme

@Composable
fun SettingSectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.bodySmall.copy(
            fontSize = Dimens.fontSizeSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.paddingLR, vertical = Dimens.paddingTB),
    )
}

@Preview(showBackground = true)
@Composable
private fun SettingSectionHeaderPreview() {
    SubwayWhenTheme {
        SettingSectionHeader(title = "홈 화면")
    }
}
