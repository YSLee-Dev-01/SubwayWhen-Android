package com.yslee.subwaywhen.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.yslee.subwaywhen.data.model.SaveStation
import com.yslee.subwaywhen.ui.theme.Dimens
import com.yslee.subwaywhen.ui.theme.MainColorDark
import com.yslee.subwaywhen.ui.theme.MainColorLight

/**
 * 역 정보 카드(mainBG) 공통 Row.
 * station이 null이면 gray "역 선택" 폴백 상태로 렌더링.
 */
@Composable
fun StationRow(
    station: SaveStation?,
    modifier: Modifier = Modifier,
    showUpDown: Boolean = true,
    circleSize: Dp = Dimens.stationLineCircleSize,
    circleFontSize: TextUnit = Dimens.fontSizeSmall,
    trailingContent: @Composable (() -> Unit)? = null,
) {
    val bgColor = if (isSystemInDarkTheme()) MainColorDark else MainColorLight
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(Dimens.cornerRadius))
            .background(color = bgColor)
            .padding(horizontal = Dimens.paddingInner, vertical = 11.dp),
    ) {
        StationLineCircle(
            title = if (station != null) subwayLineDisplayName(station.line) else "?",
            lineColor = if (station != null) subwayLineColor(station.line) else MaterialTheme.colorScheme.onSurfaceVariant,
            size = circleSize,
            isFilled = true,
            fontSize = circleFontSize,
        )

        Spacer(modifier = Modifier.width(Dimens.paddingInner))

        Text(
            text = station?.stationName ?: "역 선택",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = if (station != null) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.weight(1f))

        if (station != null && showUpDown) {
            Text(
                text = station.updnLine,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (trailingContent != null) {
            Spacer(modifier = Modifier.width(8.dp))
            trailingContent()
        }
    }
}
