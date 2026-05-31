package com.yslee.subwaywhen.feature.search.vicinity.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yslee.subwaywhen.data.remote.dto.liveArrival.RealtimeStationArrival
import com.yslee.subwaywhen.data.remote.dto.vicinityStation.VicinityTransformData
import com.yslee.subwaywhen.ui.common.StationLineCircle
import com.yslee.subwaywhen.ui.common.subwayLineColor
import com.yslee.subwaywhen.ui.common.subwayLineUpDownText
import com.yslee.subwaywhen.ui.theme.Dimens
import com.yslee.subwaywhen.ui.theme.SubwayWhenTheme

/**
 * iOS SearchVicinityView 역 선택 후 상세 도착 정보 카드 대응.
 *
 * 구조:
 * - [A] 트랙 라인: 왼쪽(상행) Column + 중앙 역명 원 + 오른쪽(하행) Column
 *   - 왼쪽: 15dp 상단 여백 → 트랙 바 → 역명 → weight Spacer (트랙이 중앙보다 위)
 *   - 오른쪽: weight Spacer → 트랙 바 → 역명 (트랙이 중앙보다 아래)
 *   - 열차 아이콘: code 기반 위치 결정 (iOS 동일)
 * - [B] 도착 정보: 상행·하행 각 2줄 고정 (로딩 중에도 높이 유지)
 * - [C] 오른쪽 정렬 액션 아이콘 5개
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
    val lineColor = subwayLineColor(station.lineColorName) ?: MaterialTheme.colorScheme.primary

    // iOS: .scaleEffect(y: loading ? 0.2 : 1) / .opacity(loading ? 0 : 1) 대응
    // .animation(.easeInOut(duration: 0.3), value: nowLiveDataLoading)
    val upBarScaleY by animateFloatAsState(
        targetValue = if (liveLoading.first) 0.2f else 1f,
        animationSpec = tween(durationMillis = 300),
        label = "upBarScaleY",
    )
    val upCircleAlpha by animateFloatAsState(
        targetValue = if (liveLoading.first) 0f else 1f,
        animationSpec = tween(durationMillis = 300),
        label = "upCircleAlpha",
    )
    val downBarScaleY by animateFloatAsState(
        targetValue = if (liveLoading.second) 0.2f else 1f,
        animationSpec = tween(durationMillis = 300),
        label = "downBarScaleY",
    )
    val downCircleAlpha by animateFloatAsState(
        targetValue = if (liveLoading.second) 0f else 1f,
        animationSpec = tween(durationMillis = 300),
        label = "downCircleAlpha",
    )

    Column(modifier = modifier.fillMaxWidth()) {
        // ── 회색 둥근 카드 ───────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Dimens.cornerRadius))
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
                .padding(horizontal = 7.5.dp, vertical = 15.dp),
        ) {
            Column {
                // [A] 트랙 라인 섹션 ────────────────────────────────────
                // padding top=10 은 offset 클립 방지, bottom=25 는 도착정보와의 간격
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 25.dp)
                        .height(Dimens.vicinityStationCircleSizeLarge),  // 65dp = center circle
                ) {
                    // 왼쪽: 상행 트랙 (중앙 원보다 위에 배치)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.Start,
                    ) {
                        Spacer(modifier = Modifier.height(15.dp))  // iOS: Spacer().frame(height:15)

                        // 트랙 바 + 빈 원 + 열차 아이콘 오버레이
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                // 빈 원: 로딩 중 fadeout (iOS: .opacity(0))
                                Box(
                                    modifier = Modifier
                                        .size(12.5.dp)
                                        .border(1.5.dp, lineColor, CircleShape)
                                        .graphicsLayer { alpha = upCircleAlpha },
                                )
                                // 트랙 바: 로딩 중 vertically flatten (iOS: .scaleEffect(y: 0.2))
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(5.dp)
                                        .background(lineColor)
                                        .graphicsLayer { scaleY = upBarScaleY },
                                )
                            }
                            // 열차 아이콘 (로딩 중 숨김, code 기반 위치)
                            if (!liveLoading.first) {
                                val code = upArrival.firstOrNull()?.code ?: "99"
                                if (code != "99" && code.isNotEmpty()) {
                                    TrainIcon(
                                        code = code,
                                        isUp = true,
                                        modifier = Modifier.matchParentSize(),
                                    )
                                }
                            }
                        }

                        // 인접역 이름
                        Text(
                            text = parseAdjacentStationName(
                                previousStation = upArrival.firstOrNull()?.previousStation,
                                isLoading = liveLoading.first,
                            ),
                            fontSize = Dimens.fontSizeSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )

                        Spacer(modifier = Modifier.weight(1f))  // 남은 공간 → 트랙이 위에 위치
                    }

                    // 중앙: 역명 원
                    StationLineCircle(
                        title = station.name,
                        lineColor = lineColor,
                        size = Dimens.vicinityStationCircleSizeLarge,
                        isFilled = true,
                        fontSize = Dimens.fontSizeSmall,
                    )

                    // 오른쪽: 하행 트랙 (중앙 원보다 아래에 배치)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.End,
                    ) {
                        Spacer(modifier = Modifier.weight(1f))  // 남은 공간 → 트랙이 아래에 위치

                        Box(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                // 트랙 바: 로딩 중 vertically flatten
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(5.dp)
                                        .background(lineColor)
                                        .graphicsLayer { scaleY = downBarScaleY },
                                )
                                // 빈 원: 로딩 중 fadeout
                                Box(
                                    modifier = Modifier
                                        .size(12.5.dp)
                                        .border(1.5.dp, lineColor, CircleShape)
                                        .graphicsLayer { alpha = downCircleAlpha },
                                )
                            }
                            if (!liveLoading.second) {
                                val code = downArrival.firstOrNull()?.code ?: "99"
                                if (code != "99" && code.isNotEmpty()) {
                                    TrainIcon(
                                        code = code,
                                        isUp = false,
                                        modifier = Modifier.matchParentSize(),
                                    )
                                }
                            }
                        }

                        Text(
                            text = parseAdjacentStationName(
                                previousStation = downArrival.firstOrNull()?.previousStation,
                                isLoading = liveLoading.second,
                            ),
                            fontSize = Dimens.fontSizeSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.End,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                // [B] 도착 정보 텍스트 ────────────────────────────────────
                // 항상 2줄(방향 + 상태) 고정 → 로딩 중에도 카드 높이 변화 없음
                // IntrinsicSize.Max: 텍스트 높이 결정 후 중앙 세로선이 fillMaxHeight로 동일 높이 채움
                Column(
                    modifier = Modifier.padding(start = 10.dp, end = 10.dp, bottom = 15.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max),
                    ) {
                        // 상행/내선
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            val upData = upArrival.firstOrNull()
                            val upDirection = when {
                                liveLoading.first || upData == null -> "-"
                                else -> "${if (upData.isFast == "급행") "(급)" else ""}${upData.lastStation}행 (${subwayLineUpDownText(station.lineColorName, isUp = true)})"
                            }
                            val upStatus = when {
                                liveLoading.first -> "🔄 로딩 중"
                                upData == null || upData.subPrevious.isEmpty() -> "⚠️ 정보없음"
                                else -> upData.subPrevious
                            }
                            Text(
                                text = upDirection,
                                fontSize = Dimens.fontSizeSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = upStatus,
                                fontSize = Dimens.fontSizeMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }

                        // 중앙 세로선 (iOS: RoundedRectangle lineColor 1.5dp × fullHeight)
                        Box(
                            modifier = Modifier
                                .width(1.5.dp)
                                .fillMaxHeight()
                                .background(lineColor, RoundedCornerShape(15.dp)),
                        )

                        // 하행/외선
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            val downData = downArrival.firstOrNull()
                            val downDirection = when {
                                liveLoading.second || downData == null -> "-"
                                else -> "${if (downData.isFast == "급행") "(급)" else ""}${downData.lastStation}행 (${subwayLineUpDownText(station.lineColorName, isUp = false)})"
                            }
                            val downStatus = when {
                                liveLoading.second -> "🔄 로딩 중"
                                downData == null || downData.subPrevious.isEmpty() -> "⚠️ 정보없음"
                                else -> downData.subPrevious
                            }
                            Text(
                                text = downDirection,
                                fontSize = Dimens.fontSizeSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.End,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = downStatus,
                                fontSize = Dimens.fontSizeMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.End,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }

        // ── [C] 오른쪽 정렬 액션 아이콘 ──────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 10.dp, top = 6.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(15.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ActionIcon(icon = Icons.Default.Close,   tint = Color.Gray, onClick = onClose)
            ActionIcon(icon = Icons.Default.Refresh, tint = Color.Gray, onClick = onRefresh)
            ActionIcon(icon = Icons.Default.Info,    tint = Color.Gray, onClick = { /* TODO: 임시보기 */ })
            ActionIcon(icon = Icons.Default.Add,     tint = Color.Gray, onClick = onAddStation)
            ActionIcon(icon = Icons.Default.Warning, tint = Color.Gray, onClick = { /* TODO: 신고하기 */ })
        }
    }
}

// ── Private composables ───────────────────────────────────────────────

/**
 * 열차 아이콘. iOS: code 기반 위치 결정
 * - code 0/1/2 (역 근처): 트랙 바의 중앙 원 방향(상행=오른쪽, 하행=왼쪽)
 * - code 3/4/5 (역 멀리): 트랙 바의 반대 방향
 * - 상행(isUp=true): iOS scaleEffect(x: -1) 대응으로 좌우 반전
 */
@Composable
private fun TrainIcon(
    code: String,
    isUp: Boolean,
    modifier: Modifier = Modifier,
) {
    val isNearStation = code == "0" || code == "1" || code == "2"
    // 상행 트랙(왼쪽): 역 근처 → 오른쪽(End), 멀리 → 왼쪽(Start)
    // 하행 트랙(오른쪽): 역 근처 → 왼쪽(Start), 멀리 → 오른쪽(End)
    val alignment = when {
        isUp && isNearStation -> Alignment.CenterEnd
        isUp -> Alignment.CenterStart
        isNearStation -> Alignment.CenterStart
        else -> Alignment.CenterEnd
    }

    Box(
        contentAlignment = alignment,
        modifier = modifier,
    ) {
        Text(
            text = "🚃",
            modifier = Modifier
                .offset(y = (-12).dp)               // iOS: .padding(.bottom, 20) 대응, 트랙 위로 부상
                .then(
                    if (isUp) Modifier.graphicsLayer(scaleX = -1f) else Modifier,
                ),
        )
    }
}

@Composable
private fun ActionIcon(
    icon: ImageVector,
    tint: Color,
    onClick: () -> Unit,
) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = tint,
        modifier = Modifier
            .size(Dimens.vicinityActionIconSize)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
    )
}

// ── Helpers ───────────────────────────────────────────────────────────

/**
 * iOS backStationName 대응.
 * previousStation (arvlMsg3) 값에서 역 이름을 파싱한다.
 * ex) "역삼역 출발" → "역삼", "선릉역 도착" → "선릉", null → "-"
 */
private fun parseAdjacentStationName(previousStation: String?, isLoading: Boolean): String {
    if (isLoading) return "-"
    if (previousStation.isNullOrEmpty()) return "-"
    return previousStation
        .replace("역 출발", "")
        .replace("역 도착", "")
        .replace(" 출발", "")
        .replace(" 도착", "")
        .replace("역", "")
        .trim()
        .ifEmpty { "-" }
}

// ── Preview ──────────────────────────────────────────────────────────

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
                    previousStation = "역삼역 출발",
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
                    code = "1",
                    subWayId = "1002",
                    stationName = "강남",
                    lastStation = "신도림",
                    isFast = "급행",
                    previousStation = "교대역 출발",
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
                    subPrevious = "강남 도착",
                    code = "0",
                    subWayId = "1002",
                    stationName = "강남",
                    lastStation = "성수",
                    isFast = null,
                    previousStation = "역삼역 출발",
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
                    code = "3",
                    subWayId = "1002",
                    stationName = "강남",
                    lastStation = "신도림",
                    isFast = "급행",
                    previousStation = "교대역 도착",
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

@Preview(name = "VicinityStationDetailCard - Empty", showBackground = true)
@Composable
private fun VicinityStationDetailCardEmptyPreview() {
    SubwayWhenTheme(darkTheme = false) {
        VicinityStationDetailCard(
            station = VicinityTransformData(id = "1", name = "한성대입구", line = "4호선", distance = "500m"),
            upArrival = emptyList(),
            downArrival = emptyList(),
            liveLoading = Pair(false, false),
            onClose = {},
            onRefresh = {},
            onAddStation = {},
        )
    }
}
