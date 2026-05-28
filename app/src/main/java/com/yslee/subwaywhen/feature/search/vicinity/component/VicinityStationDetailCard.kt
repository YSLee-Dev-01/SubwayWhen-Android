package com.yslee.subwaywhen.feature.search.vicinity.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yslee.subwaywhen.data.remote.dto.liveArrival.RealtimeStationArrival
import com.yslee.subwaywhen.data.remote.dto.vicinityStation.VicinityTransformData
import com.yslee.subwaywhen.ui.common.StationLineCircle
import com.yslee.subwaywhen.ui.common.subwayLineColor
import com.yslee.subwaywhen.ui.common.subwayLineDisplayName
import com.yslee.subwaywhen.ui.common.subwayLineUpDownText
import com.yslee.subwaywhen.ui.theme.Dimens
import com.yslee.subwaywhen.ui.theme.SubwayWhenTheme

/**
 * iOS SearchVicinityView 역 선택 후 상세 도착 정보 카드 대응.
 * 상행/하행 실시간 도착 정보 + 5개 액션 버튼.
 */
@Composable
fun VicinityStationDetailCard(
    station: VicinityTransformData,
    upArrival: List<RealtimeStationArrival>,
    downArrival: List<RealtimeStationArrival>,
    liveLoading: Pair<Boolean, Boolean>,
    onClose: () -> Unit,
    onRefresh: () -> Unit,
    onAddStation: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // 상단: 좌·중앙·우 3분할 도착 정보
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
        ) {
            // 좌측 (weight 1f): 상행/내선 도착 정보
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.TopStart,
            ) {
                if (liveLoading.first) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.Center),
                        strokeWidth = 2.dp,
                        color = subwayLineColor(station.lineColorName) ?: MaterialTheme.colorScheme.primary,
                    )
                } else {
                    Column(
                        verticalArrangement = Arrangement.Top,
                        modifier = Modifier.padding(top = 4.dp, end = 8.dp),
                    ) {
                        upArrival.take(2).forEach { arrival ->
                            ArrivalInfoItem(
                                arrival = arrival,
                                directionSuffix = subwayLineUpDownText(station.lineColorName, isUp = true),
                                textAlign = TextAlign.Start,
                            )
                        }
                    }
                }
            }

            // 중앙: StationLineCircle 65dp
            StationLineCircle(
                title = subwayLineDisplayName(station.lineColorName),
                lineColor = subwayLineColor(station.lineColorName),
                size = Dimens.vicinityStationCircleSizeLarge,
                isFilled = true,
                fontSize = Dimens.fontSizeSmall,
            )

            // 우측 (weight 1f): 하행/외선 도착 정보
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.BottomEnd,
            ) {
                if (liveLoading.second) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.Center),
                        strokeWidth = 2.dp,
                        color = subwayLineColor(station.lineColorName) ?: MaterialTheme.colorScheme.primary,
                    )
                } else {
                    Column(
                        verticalArrangement = Arrangement.Bottom,
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier
                            .padding(top = 4.dp, start = 8.dp)
                            .align(Alignment.BottomEnd),
                    ) {
                        downArrival.take(2).forEach { arrival ->
                            ArrivalInfoItem(
                                arrival = arrival,
                                directionSuffix = subwayLineUpDownText(station.lineColorName, isUp = false),
                                textAlign = TextAlign.End,
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 하단: 5개 아이콘 버튼 Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            // 1. 닫기
            ActionIconButton(
                icon = { Icon(Icons.Default.Close, contentDescription = null) },
                label = "닫기",
                onClick = onClose,
            )
            // 2. 새로고침
            ActionIconButton(
                icon = { Icon(Icons.Default.Refresh, contentDescription = null) },
                label = "새로고침",
                onClick = onRefresh,
            )
            // 3. 임시보기 (TODO: 상세 화면 이동)
            ActionIconButton(
                icon = { Icon(Icons.Default.Info, contentDescription = null) },
                label = "임시보기",
                onClick = { /* TODO: 임시보기 구현 */ },
            )
            // 4. 추가하기
            ActionIconButton(
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                label = "추가하기",
                onClick = onAddStation,
            )
            // 5. 신고하기 (TODO: 신고 화면 이동)
            ActionIconButton(
                icon = { Icon(Icons.Default.Warning, contentDescription = null) },
                label = "신고하기",
                onClick = { /* TODO: 신고하기 구현 */ },
            )
        }
    }
}

@Composable
private fun ArrivalInfoItem(
    arrival: RealtimeStationArrival,
    directionSuffix: String,
    textAlign: TextAlign,
) {
    val direction = if (arrival.isFast == "급행") {
        "(급)${arrival.lastStation}행 ($directionSuffix)"
    } else {
        "${arrival.lastStation}행 ($directionSuffix)"
    }

    Text(
        text = direction,
        fontSize = Dimens.fontSizeSmall,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = textAlign,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
    Text(
        text = arrival.subPrevious,
        fontSize = Dimens.fontSizeMedium,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = textAlign,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun ActionIconButton(
    icon: @Composable () -> Unit,
    label: String,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        IconButton(onClick = onClick) {
            icon()
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Preview(name = "VicinityStationDetailCard - Light", showBackground = true)
@Composable
private fun VicinityStationDetailCardLightPreview() {
    SubwayWhenTheme(darkTheme = false) {
        VicinityStationDetailCard(
            station = VicinityTransformData(id = "1", name = "강남", line = "2호선", distance = "150m"),
            upArrival = listOf(
                RealtimeStationArrival(
                    upDown = "상행",
                    arrivalTime = "120",
                    subPrevious = "2분 후",
                    code = "4",
                    subWayId = "1002",
                    stationName = "강남",
                    lastStation = "성수",
                    isFast = null,
                    backStationId = "1002000222",
                    nextStationId = "1002000224",
                    trainCode = "2345",
                ),
            ),
            downArrival = listOf(
                RealtimeStationArrival(
                    upDown = "하행",
                    arrivalTime = "60",
                    subPrevious = "1분 후",
                    code = "4",
                    subWayId = "1002",
                    stationName = "강남",
                    lastStation = "신도림",
                    isFast = "급행",
                    backStationId = "1002000224",
                    nextStationId = "1002000222",
                    trainCode = "2346",
                ),
            ),
            liveLoading = Pair(false, false),
            onClose = {},
            onRefresh = {},
            onAddStation = {},
        )
    }
}

@Preview(name = "VicinityStationDetailCard - Dark", showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun VicinityStationDetailCardDarkPreview() {
    SubwayWhenTheme(darkTheme = true) {
        VicinityStationDetailCard(
            station = VicinityTransformData(id = "1", name = "강남", line = "2호선", distance = "150m"),
            upArrival = listOf(
                RealtimeStationArrival(
                    upDown = "상행",
                    arrivalTime = "120",
                    subPrevious = "2분 후",
                    code = "4",
                    subWayId = "1002",
                    stationName = "강남",
                    lastStation = "성수",
                    isFast = null,
                    backStationId = "1002000222",
                    nextStationId = "1002000224",
                    trainCode = "2345",
                ),
            ),
            downArrival = listOf(
                RealtimeStationArrival(
                    upDown = "하행",
                    arrivalTime = "60",
                    subPrevious = "1분 후",
                    code = "4",
                    subWayId = "1002",
                    stationName = "강남",
                    lastStation = "신도림",
                    isFast = "급행",
                    backStationId = "1002000224",
                    nextStationId = "1002000222",
                    trainCode = "2346",
                ),
            ),
            liveLoading = Pair(false, false),
            onClose = {},
            onRefresh = {},
            onAddStation = {},
        )
    }
}

@Preview(name = "VicinityStationDetailCard - Loading", showBackground = true)
@Composable
private fun VicinityStationDetailCardLoadingPreview() {
    SubwayWhenTheme(darkTheme = false) {
        VicinityStationDetailCard(
            station = VicinityTransformData(id = "1", name = "강남", line = "2호선", distance = "150m"),
            upArrival = emptyList(),
            downArrival = emptyList(),
            liveLoading = Pair(true, true),
            onClose = {},
            onRefresh = {},
            onAddStation = {},
        )
    }
}
