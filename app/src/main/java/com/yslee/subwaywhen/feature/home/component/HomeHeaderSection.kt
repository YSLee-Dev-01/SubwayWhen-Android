package com.yslee.subwaywhen.feature.home.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yslee.subwaywhen.R
import com.yslee.subwaywhen.ui.theme.Dimens
import com.yslee.subwaywhen.ui.theme.SubwayWhenTheme

/**
 * iOS MainTableHeaderView 대응.
 * 혼잡도 카드 + 민원/편집 버튼 행 + "실시간 현황" 라벨을 묶은 헤더 섹션.
 * GroupTabBar는 화면에서 별도 아이템으로 배치된다.
 */
@Composable
fun HomeHeaderSection(
    congestionEmoji: String,
    onCongestionTap: () -> Unit,
    onReportTap: () -> Unit,
    onEditTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // 혼잡도 카드
        HomeCongestionCard(
            congestionEmoji = congestionEmoji,
            onTap = onCongestionTap,
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 민원 / 편집 버튼 행 (각 50%, 사이 간격 10dp)
        Row(modifier = Modifier.fillMaxWidth()) {
            HomeHeaderActionButton(
                label = stringResource(R.string.home_header_report),
                lottieRes = R.raw.report,
                onTap = onReportTap,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 5.dp),
            )
            HomeHeaderActionButton(
                label = stringResource(R.string.home_header_edit),
                lottieRes = R.raw.list,
                onTap = onEditTap,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 5.dp),
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // "실시간 현황" 라벨
        Text(
            text = stringResource(R.string.home_live_status_title),
            fontSize = Dimens.fontSizeSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        )
    }
}

@Preview(name = "HomeHeaderSection - Light", showBackground = true)
@Composable
private fun HomeHeaderSectionLightPreview() {
    SubwayWhenTheme(darkTheme = false) {
        HomeHeaderSection(
            congestionEmoji = "🫥🫥🫥🫥🫥🫥🫥🫥🫥🫥",
            onCongestionTap = {},
            onReportTap = {},
            onEditTap = {},
            modifier = Modifier.padding(horizontal = Dimens.paddingLR, vertical = 10.dp),
        )
    }
}

@Preview(name = "HomeHeaderSection - Dark", showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun HomeHeaderSectionDarkPreview() {
    SubwayWhenTheme(darkTheme = true) {
        HomeHeaderSection(
            congestionEmoji = "🫥🫥🫥🫥🫥🫥🫥🫥🫥🫥",
            onCongestionTap = {},
            onReportTap = {},
            onEditTap = {},
            modifier = Modifier.padding(horizontal = Dimens.paddingLR, vertical = 10.dp),
        )
    }
}
