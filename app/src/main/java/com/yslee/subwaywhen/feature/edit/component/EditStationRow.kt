package com.yslee.subwaywhen.feature.edit.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yslee.subwaywhen.data.model.SaveStation
import com.yslee.subwaywhen.data.model.SaveStationGroup
import com.yslee.subwaywhen.ui.common.AnimatedTapBox
import com.yslee.subwaywhen.ui.common.AnimatedTapBoxAlignment
import com.yslee.subwaywhen.ui.common.StationLineCircle
import com.yslee.subwaywhen.ui.common.subwayLineColor
import com.yslee.subwaywhen.ui.common.subwayLineDisplayName
import com.yslee.subwaywhen.ui.theme.Dimens
import com.yslee.subwaywhen.ui.theme.SubwayWhenTheme
import sh.calvin.reorderable.ReorderableCollectionItemScope

/**
 * 편집 화면 역 목록 행.
 * ReorderableCollectionItemScope receiver로 드래그 핸들에 draggableHandle() modifier를 적용한다.
 */
@Composable
fun ReorderableCollectionItemScope.EditStationRow(
    station: SaveStation,
    onDelete: () -> Unit,
) {
    EditStationRowContent(
        station = station,
        onDelete = onDelete,
        dragHandleModifier = Modifier.draggableHandle(),
    )
}

@Composable
private fun EditStationRowContent(
    station: SaveStation,
    onDelete: () -> Unit,
    dragHandleModifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(Dimens.paddingInner),
    ) {
        // 좌측: 빨간 원형 "−" 삭제 버튼
        AnimatedTapBox(
            bgColor = MaterialTheme.colorScheme.error,
            pressedColor = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
            alignment = AnimatedTapBoxAlignment.Center,
            verticalPadding = 6.dp,
            horizontalPadding = 6.dp,
            onClick = onDelete,
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(16.dp),
            ) {
                Text(
                    text = "−",
                    color = MaterialTheme.colorScheme.onError,
                    fontSize = Dimens.fontSizeLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Spacer(modifier = Modifier.width(Dimens.paddingInner))

        // 본문: StationLineCircle + 역명 + 상하행
        StationLineCircle(
            title = subwayLineDisplayName(station.line),
            lineColor = subwayLineColor(station.line),
            size = 40.dp,
            isFilled = true,
            fontSize = Dimens.fontSizeMediumSmall,
        )

        Spacer(modifier = Modifier.width(Dimens.paddingInner))

        Text(
            text = station.stationName,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
        )

        Text(
            text = station.updnLine,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.width(Dimens.paddingInner))

        // 우측: 드래그 핸들
        Icon(
            imageVector = Icons.Default.DragHandle,
            contentDescription = "순서 변경",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = dragHandleModifier,
        )
    }
}

@Preview(name = "EditStationRow - Light", showBackground = true)
@Composable
private fun EditStationRowLightPreview() {
    SubwayWhenTheme(darkTheme = false) {
        EditStationRowContent(
            station = SaveStation(
                id = "1",
                stationName = "강남",
                stationCode = "222",
                updnLine = "상행",
                line = "02호선",
                lineCode = "1002",
                group = SaveStationGroup.ONE,
            ),
            onDelete = {},
        )
    }
}

@Preview(name = "EditStationRow - Dark", showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun EditStationRowDarkPreview() {
    SubwayWhenTheme(darkTheme = true) {
        EditStationRowContent(
            station = SaveStation(
                id = "2",
                stationName = "서울역",
                stationCode = "150",
                updnLine = "하행",
                line = "01호선",
                lineCode = "1001",
                group = SaveStationGroup.TWO,
            ),
            onDelete = {},
        )
    }
}
