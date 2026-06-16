package com.yslee.subwaywhen.feature.detail.resultschedule.component

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

@Composable
fun DetailResultScheduleHourHeader(
    label: String,
    isCurrent: Boolean,
    modifier: Modifier = Modifier,
) {
    Text(
        text = label,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Medium,
        color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(vertical = 8.dp, horizontal = Dimens.paddingLR),
    )
}

@Preview(showBackground = true)
@Composable
private fun DetailResultScheduleHourHeaderPreview() {
    SubwayWhenTheme {
        DetailResultScheduleHourHeader(label = "10시", isCurrent = true)
    }
}
