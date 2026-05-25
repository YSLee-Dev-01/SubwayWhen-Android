package com.yslee.subwaywhen.feature.search.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yslee.subwaywhen.R
import com.yslee.subwaywhen.ui.common.AnimatedTapBox
import com.yslee.subwaywhen.ui.common.AnimatedTapBoxAlignment
import com.yslee.subwaywhen.ui.common.MainBgCard
import com.yslee.subwaywhen.ui.theme.Dimens

/**
 * 자주 검색되는 지하철역 섹션. iOS SearchWordRecommendView 대응.
 * LazyVerticalGrid 사용 금지 — 부모 verticalScroll 내부 무한 높이 측정 에러 방지.
 * stations.chunked(2) + Row로 2열 구성.
 */
@Composable
fun SearchWordRecommendSection(
    stations: List<String>,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    MainBgCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Dimens.paddingInner)) {
            Text(
                text = stringResource(R.string.search_word_recommend_title),
                fontSize = Dimens.fontSizeLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 15.dp),
            )

            stations.chunked(2).forEach { pair ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp),
                ) {
                    pair.forEach { name ->
                        Box(modifier = Modifier.weight(1f)) {
                            AnimatedTapBox(
                                bgColor = Color.Gray.copy(alpha = 0.1f),
                                pressedColor = Color.Gray.copy(alpha = 0.01f),
                                alignment = AnimatedTapBoxAlignment.Center,
                                verticalPadding = 15.dp,
                                onClick = { onItemClick(name) },
                            ) {
                                Text(
                                    text = name,
                                    fontSize = Dimens.fontSizeSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                    // 홀수 마지막 아이템인 경우 빈 공간 채우기
                    if (pair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
