package com.yslee.subwaywhen.feature.detail.resultschedule.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.yslee.subwaywhen.feature.detail.DetailScheduleItem
import com.yslee.subwaywhen.ui.theme.Dimens
import com.yslee.subwaywhen.ui.theme.SubwayWhenTheme

@Composable
fun DetailResultScheduleCell(
    item: DetailScheduleItem,
    modifier: Modifier = Modifier,
) {
    val fastLabel = if (item.isFast) " (급)" else ""
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.paddingLR, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = item.timeLabel,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "${item.destination}행$fastLabel",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DetailResultScheduleCellPreview() {
    SubwayWhenTheme {
        DetailResultScheduleCell(
            item = DetailScheduleItem(5, "10:23", "구파발", true),
        )
    }
}
