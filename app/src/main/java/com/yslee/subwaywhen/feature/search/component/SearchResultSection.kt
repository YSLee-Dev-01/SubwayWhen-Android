package com.yslee.subwaywhen.feature.search.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import com.yslee.subwaywhen.ui.common.subwayLineColor
import com.yslee.subwaywhen.ui.common.subwayLineDisplayName
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yslee.subwaywhen.R
import com.yslee.subwaywhen.data.remote.dto.stationSearch.SearchStationInfo
import com.yslee.subwaywhen.ui.common.AnimatedTapBox
import com.yslee.subwaywhen.ui.common.AnimatedTapBoxAlignment
import com.yslee.subwaywhen.ui.common.MainBgCard
import com.yslee.subwaywhen.ui.common.StationLineCircle
import com.yslee.subwaywhen.ui.theme.Dimens

/**
 * 검색 결과 섹션. iOS SearchStationResultView 대응.
 * - 로딩 중: CircularProgressIndicator 중앙 표시
 * - 결과 없음: 쿼리 상태에 따른 안내 텍스트
 * - 결과 있음: 역명 + 호선 행 목록
 */
@Composable
fun SearchResultSection(
    query: String,
    isLoading: Boolean,
    result: List<SearchStationInfo>,
    onItemClick: (SearchStationInfo) -> Unit,
    modifier: Modifier = Modifier,
) {
    MainBgCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Dimens.paddingInner)) {
            // 헤더 텍스트
            Text(
                text = when {
                    isLoading -> stringResource(R.string.search_result_loading)
                    query.isEmpty() -> stringResource(R.string.search_result_input_required)
                    else -> stringResource(R.string.search_result_count, result.size)
                },
                fontSize = Dimens.fontSizeLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 10.dp),
            )

            // 본문
            AnimatedContent(
                targetState = isLoading to result,
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
                label = "searchResultBody",
            ) { (loading, items) ->
                if (loading) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 7.5.dp),
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                } else if (items.isEmpty()) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 15.dp),
                    ) {
                        Text(
                            text = if (query.isEmpty()) {
                                stringResource(R.string.search_result_empty_query)
                            } else {
                                stringResource(R.string.search_result_no_match)
                            },
                            fontSize = Dimens.fontSizeMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items.forEach { item ->
                            AnimatedTapBox(
                                bgColor = Color.Gray.copy(alpha = 0.1f),
                                pressedColor = Color.Gray.copy(alpha = 0.01f),
                                alignment = AnimatedTapBoxAlignment.Leading,
                                horizontalPadding = 10.dp,
                                onClick = { onItemClick(item) },
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 5.dp),
                                ) {
                                    StationLineCircle(
                                        title = subwayLineDisplayName(item.line),
                                        lineColor = subwayLineColor(item.line),
                                        size = Dimens.stationLineCircleSize,
                                        isFilled = true,
                                        fontSize = Dimens.fontSizeSmall,
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = item.stationName,
                                        fontSize = Dimens.fontSizeMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
