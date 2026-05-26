package com.yslee.subwaywhen.feature.search.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import com.yslee.subwaywhen.data.remote.dto.stationSearch.SearchQueryRecommendData
import com.yslee.subwaywhen.ui.common.AnimatedTapBox
import com.yslee.subwaywhen.ui.common.subwayLineColor
import com.yslee.subwaywhen.ui.common.subwayLineDisplayName
import com.yslee.subwaywhen.ui.common.AnimatedTapBoxAlignment
import com.yslee.subwaywhen.ui.common.MainBgCard
import com.yslee.subwaywhen.ui.common.StationLineCircle
import com.yslee.subwaywhen.ui.theme.Dimens

/**
 * "혹시 이 역을 찾으셨나요?" 섹션. iOS SearchQueryRecommendView 대응.
 * LazyColumn 사용 금지 — 부모 scroll 안에 있으므로 Column으로 구성.
 */
@Composable
fun SearchQueryRecommendSection(
    items: List<SearchQueryRecommendData>,
    onItemClick: (SearchQueryRecommendData) -> Unit,
    modifier: Modifier = Modifier,
) {
    MainBgCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Dimens.paddingInner)) {
            Text(
                text = stringResource(R.string.search_query_recommend_title),
                fontSize = Dimens.fontSizeLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
            )

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
                            modifier = Modifier.fillMaxWidth(),
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
