package com.yslee.subwaywhen.feature.edit.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yslee.subwaywhen.data.model.SaveStation
import com.yslee.subwaywhen.data.model.SaveStationGroup
import com.yslee.subwaywhen.ui.common.StationLineCircle
import com.yslee.subwaywhen.ui.common.subwayLineColor
import com.yslee.subwaywhen.ui.common.subwayLineDisplayName
import com.yslee.subwaywhen.ui.theme.Dimens
import com.yslee.subwaywhen.ui.theme.MainColorDark
import com.yslee.subwaywhen.ui.theme.MainColorLight
import com.yslee.subwaywhen.ui.theme.SubwayWhenTheme
import sh.calvin.reorderable.ReorderableCollectionItemScope

/**
 * 편집 화면 역 목록 행.
 * iOS TableViewCellCustom.mainBG 패턴 적용:
 *  - outer Row: -버튼(좌) + mainBG 영역(중, weight=1f) + 드래그핸들(우)
 *  - mainBG: Gray 0.1f 배경 + cornerRadius, 역 정보만 감쌈
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
    // outer Row: -버튼 / mainBG / 드래그핸들
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = Dimens.paddingTB),
    ) {
        // 좌측: 빨간 원형 "−" 삭제 버튼 (mainBG 밖)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(22.dp)
                .background(MaterialTheme.colorScheme.error, CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDelete,
                ),
        ) {
            Text(
                text = "−",
                color = MaterialTheme.colorScheme.onError,
                fontSize = Dimens.fontSizeMedium,
                fontWeight = FontWeight.Bold,
                lineHeight = Dimens.fontSizeMedium,
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // 중앙: mainBG 역할 — 역 정보 영역만 배경색으로 감쌈
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .background(
                    color = if (isSystemInDarkTheme()) MainColorDark else MainColorLight,
                    shape = RoundedCornerShape(Dimens.cornerRadius),
                )
                .padding(horizontal = Dimens.paddingInner, vertical = 10.dp),
        ) {
            // 호선 원형 뱃지
            StationLineCircle(
                title = subwayLineDisplayName(station.line),
                lineColor = subwayLineColor(station.line),
                size = Dimens.stationLineCircleSize,
                isFilled = true,
                fontSize = Dimens.fontSizeSmall,
            )

            Spacer(modifier = Modifier.width(Dimens.paddingInner))

            // 역명
            Text(
                text = station.stationName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.weight(1f))

            // 상하행 정보
            Text(
                text = station.updnLine,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // 우측: 드래그 핸들 (mainBG 밖)
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
