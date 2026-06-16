package com.yslee.subwaywhen.feature.detail.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yslee.subwaywhen.ui.common.AnimatedTapBox
import com.yslee.subwaywhen.ui.common.AnimatedTapBoxAlignment
import com.yslee.subwaywhen.ui.theme.Dimens
import com.yslee.subwaywhen.ui.theme.SubwayWhenTheme

@Composable
fun DetailTimerRefreshView(
    timerCount: Int,
    isRefreshCooldown: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "${timerCount}초 후 갱신",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        AnimatedTapBox(
            bgColor = MaterialTheme.colorScheme.surfaceVariant,
            pressedColor = MaterialTheme.colorScheme.outline,
            alignment = AnimatedTapBoxAlignment.Center,
            horizontalPadding = 16.dp,
            verticalPadding = 8.dp,
            onClick = { if (!isRefreshCooldown) onRefresh() },
            modifier = Modifier.alpha(if (isRefreshCooldown) 0.4f else 1f),
        ) {
            Text(
                text = "새로고침",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Preview(name = "DetailTimerRefreshView - Light", showBackground = true)
@Composable
private fun DetailTimerRefreshViewLightPreview() {
    SubwayWhenTheme(darkTheme = false) {
        DetailTimerRefreshView(
            timerCount = 10,
            isRefreshCooldown = false,
            onRefresh = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "DetailTimerRefreshView - Cooldown", showBackground = true)
@Composable
private fun DetailTimerRefreshViewCooldownPreview() {
    SubwayWhenTheme(darkTheme = false) {
        DetailTimerRefreshView(
            timerCount = 15,
            isRefreshCooldown = true,
            onRefresh = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
